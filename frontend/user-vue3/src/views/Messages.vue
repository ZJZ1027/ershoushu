<template>
  <div class="page msg-page">
    <div class="msg-shell">
      <aside class="msg-side">
        <div class="msg-side-head">最近消息</div>
        <div class="msg-side-list">
          <div v-if="!list.length" class="msg-empty-side">暂无会话</div>
          <button
            v-for="item in list"
            :key="item.id"
            type="button"
            class="msg-item"
            :class="{ active: currentId === item.id }"
            @click="open(item.id)"
          >
            <div
              class="msg-avatar"
              :class="{ clickable: !!item.peerAvatar }"
              :style="avatarStyle(item.peerNickname, item.peerAvatar, item.systemNotice)"
              :title="item.peerAvatar ? '查看头像' : undefined"
              @click.stop="previewPeer(item)"
            >
              <img v-if="item.peerAvatar" :src="fileUrl(item.peerAvatar)" alt="" />
              <span v-else>{{ avatarText(item.peerNickname, item.systemNotice) }}</span>
            </div>
            <div class="msg-item-body">
              <div class="msg-item-top">
                <span class="msg-item-name">{{ item.peerNickname || '用户' }}</span>
                <span class="msg-item-time">{{ formatShortTime(item.lastTime) }}</span>
              </div>
              <div class="msg-item-book">
                {{ item.systemNotice ? '系统通知' : (item.bookTitle ? '最近关于《' + item.bookTitle + '》' : '会话') }}
              </div>
              <div class="msg-item-preview">{{ item.lastMsg || '暂无消息' }}</div>
            </div>
            <span v-if="item.unread" class="msg-dot" />
          </button>
        </div>
      </aside>

      <section class="msg-main">
        <template v-if="current">
          <header class="msg-main-head">
            <div>
              <div class="msg-main-title">{{ current.peerNickname || '用户' }}</div>
              <div class="msg-main-sub">
                {{ current.systemNotice ? '系统通知' : (current.bookTitle ? '最近关于《' + current.bookTitle + '》' : '会话') }}
              </div>
            </div>
          </header>

          <div ref="scrollRef" class="msg-chat">
            <div v-if="!messages.length" class="msg-empty-chat">还没有消息，打个招呼吧</div>
            <template v-for="(m, idx) in messages" :key="m.id">
              <div v-if="showTime(idx)" class="msg-time">{{ formatFullTime(m.createTime) }}</div>
              <div class="msg-row" :class="{ mine: isMine(m) }">
                <div
                  class="msg-avatar sm"
                  :class="{ clickable: !!(isMine(m) ? myAvatar : current.peerAvatar) }"
                  :style="avatarStyle(
                    isMine(m) ? myName : m.senderNickname,
                    isMine(m) ? myAvatar : current.peerAvatar,
                    current.systemNotice && !isMine(m)
                  )"
                  :title="(isMine(m) ? myAvatar : current.peerAvatar) ? '查看头像' : undefined"
                  @click="previewChatAvatar(m)"
                >
                  <img
                    v-if="isMine(m) ? myAvatar : current.peerAvatar"
                    :src="fileUrl(isMine(m) ? myAvatar : current.peerAvatar)"
                    alt=""
                  />
                  <span v-else>{{ avatarText(isMine(m) ? myName : m.senderNickname, current.systemNotice && !isMine(m)) }}</span>
                </div>
                <div v-if="isRecalled(m)" class="msg-recalled">
                  {{ isMine(m) ? '你撤回了一条消息' : '对方撤回了一条消息' }}
                </div>
                <div v-else class="msg-bubble-wrap">
                  <div class="msg-bubble">
                    <div class="msg-bubble-text">{{ m.content }}</div>
                    <div v-if="current.systemNotice && !isMine(m)" class="msg-bubble-tip">此条消息为系统通知</div>
                  </div>
                  <button
                    v-if="canRecall(m)"
                    type="button"
                    class="msg-recall-btn"
                    :disabled="recallingId === m.id"
                    @click="recall(m)"
                  >
                    撤回
                  </button>
                </div>
              </div>
            </template>
          </div>

          <footer class="msg-composer">
            <template v-if="current.systemNotice">
              <div class="msg-system-tip">系统通知，无需回复</div>
            </template>
            <template v-else>
              <a-textarea
                v-model="content"
                class="msg-input"
                placeholder="请输入消息内容"
                :max-length="500"
                :auto-size="{ minRows: 3, maxRows: 5 }"
                @keydown.enter.exact.prevent="send"
              />
              <div class="msg-composer-bar">
                <span class="msg-counter">{{ content.length }}/500</span>
                <a-button type="primary" :disabled="!content.trim()" :loading="sending" @click="send">发送</a-button>
              </div>
            </template>
          </footer>
        </template>

        <div v-else class="msg-placeholder">
          <div>选择左侧会话开始聊天</div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { getInquiries, getMessages, recallInquiryMsg, replyInquiry } from '@/api'
import { fileUrl } from '@/api/http'
import { useAvatarPreview } from '@/composables/useAvatarPreview'
import { useUserStore } from '@/stores/user'

/** 与后端一致：发送后 2 分钟内可撤回 */
const RECALL_MS = 2 * 60 * 1000

const route = useRoute()
const user = useUserStore()
const { open: previewAvatar } = useAvatarPreview()
const list = ref<any[]>([])
const messages = ref<any[]>([])
const currentId = ref<number>()
const content = ref('')
const sending = ref(false)
const recallingId = ref<number | null>(null)
const nowTick = ref(Date.now())
const scrollRef = ref<HTMLElement>()
let tickTimer: ReturnType<typeof setInterval> | undefined

const current = computed(() => list.value.find((i) => i.id === currentId.value))
const myId = computed(() => Number(user.profile?.id || 0))
const myName = computed(() => user.profile?.nickname || user.profile?.username || '我')
const myAvatar = computed(() => user.profile?.avatar || '')

const isMine = (m: any) => Number(m.senderId) === myId.value

const previewPeer = (item: any) => {
  if (item?.peerAvatar) previewAvatar(fileUrl(item.peerAvatar))
}

const previewChatAvatar = (m: any) => {
  const url = isMine(m) ? myAvatar.value : current.value?.peerAvatar
  if (url) previewAvatar(fileUrl(url))
}

const isRecalled = (m: any) => Number(m.recalled) === 1
const canRecall = (m: any) => {
  void nowTick.value
  if (!isMine(m) || isRecalled(m) || !m.createTime) return false
  if (m.canRecall === false) return false
  return Date.now() - new Date(m.createTime).getTime() < RECALL_MS
}
const avatarText = (name?: string, system?: boolean) => {
  if (system) return '通'
  const n = (name || '用').trim()
  return n.slice(0, 1)
}

const avatarStyle = (name?: string, avatar?: string | null, system?: boolean) => {
  if (avatar) return {}
  if (system) return { background: 'linear-gradient(135deg, #0d6b58, #2a9a7f)' }
  const colors = ['#0d6b58', '#3d8f7a', '#65789b', '#b83a2e', '#5c7c6e', '#4a6b5e', '#8a6b4a', '#3d6b8a']
  const key = (name || 'x').charCodeAt(0) % colors.length
  return { background: colors[key] }
}

const formatShortTime = (t?: string) => {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return ''
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) {
    return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const formatFullTime = (t?: string) => {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const pad = (n: number) => String(n).padStart(2, '0')

const showTime = (idx: number) => {
  if (idx === 0) return true
  const cur = messages.value[idx]?.createTime
  const prev = messages.value[idx - 1]?.createTime
  if (!cur || !prev) return false
  return Math.abs(new Date(cur).getTime() - new Date(prev).getTime()) > 5 * 60 * 1000
}

const scrollBottom = async (smooth = false) => {
  await nextTick()
  const el = scrollRef.value
  if (!el) return
  if (smooth && typeof el.scrollTo === 'function') {
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  } else {
    el.scrollTop = el.scrollHeight
  }
}

const load = async () => {
  list.value = (await getInquiries({ pageNo: 1, pageSize: 50 })).list || []
}

const open = async (id: number) => {
  currentId.value = id
  messages.value = (await getMessages(id)) || []
  const hit = list.value.find((i) => i.id === id)
  if (hit) hit.unread = 0
  await user.refreshBadges()
  await scrollBottom(false)
}

const openFromQuery = async () => {
  const raw = route.query.id
  const id = Number(Array.isArray(raw) ? raw[0] : raw)
  if (!id || Number.isNaN(id)) return
  if (!list.value.some((i) => i.id === id)) {
    await load()
  }
  await open(id)
}

const send = async () => {
  const text = content.value.trim()
  if (!currentId.value || !text || current.value?.systemNotice || sending.value) return
  sending.value = true
  try {
    await replyInquiry(currentId.value, text)
    content.value = ''
    messages.value = (await getMessages(currentId.value)) || []
    await load()
    await scrollBottom(true)
  } finally {
    sending.value = false
  }
}

const recall = async (m: any) => {
  if (!canRecall(m) || recallingId.value) return
  recallingId.value = m.id
  try {
    await recallInquiryMsg(m.id)
    messages.value = (await getMessages(currentId.value!)) || []
    await load()
    Message.success('已撤回')
  } catch {
    if (currentId.value) {
      messages.value = (await getMessages(currentId.value)) || []
    }
  } finally {
    recallingId.value = null
  }
}

onMounted(async () => {
  tickTimer = setInterval(() => {
    nowTick.value = Date.now()
  }, 15_000)
  if (!user.profile) {
    try {
      await user.loadProfile()
    } catch {
      /* ignore */
    }
  }
  await load()
  await openFromQuery()
})

watch(
  () => route.query.id,
  async () => {
    await openFromQuery()
  }
)

onUnmounted(() => {
  if (tickTimer) clearInterval(tickTimer)
})
</script>

<style scoped>
.msg-page {
  max-width: 1080px;
  padding-top: 12px;
  padding-bottom: 24px;
}
.msg-shell {
  display: grid;
  grid-template-columns: 320px 1fr;
  height: calc(100vh - 110px);
  min-height: 520px;
  max-height: calc(100vh - 110px);
  background: var(--surface-raised);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-soft);
}
.msg-side {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border-right: 1px solid var(--line);
  background: #f5f9f7;
  overflow: hidden;
}
.msg-side-head {
  flex: none;
  padding: 16px 18px;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #e8f2ee 0%, #f5f9f7 100%);
}
.msg-side-list {
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}
.msg-side-list::-webkit-scrollbar,
.msg-chat::-webkit-scrollbar {
  width: 6px;
}
.msg-side-list::-webkit-scrollbar-thumb,
.msg-chat::-webkit-scrollbar-thumb {
  background: rgba(20, 35, 28, 0.22);
  border-radius: 6px;
}
.msg-side-list::-webkit-scrollbar-track,
.msg-chat::-webkit-scrollbar-track {
  background: transparent;
}
.msg-empty-side,
.msg-empty-chat,
.msg-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #86909c;
  font-size: 14px;
}
.msg-empty-side {
  min-height: 160px;
}
.msg-item {
  position: relative;
  display: flex;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  border-bottom: 1px solid #f2f3f5;
}
.msg-item:hover {
  background: #f5f7fb;
}
.msg-item.active {
  background: var(--teal-soft);
}
.msg-avatar {
  flex: none;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #86909c;
}
.msg-avatar.clickable {
  cursor: zoom-in;
}
.msg-avatar.clickable:hover {
  box-shadow: 0 0 0 2px rgba(13, 107, 88, 0.35);
}
.msg-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.msg-avatar.sm {
  width: 36px;
  height: 36px;
  font-size: 14px;
}
.msg-item-body {
  min-width: 0;
  flex: 1;
}
.msg-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.msg-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-item-time {
  flex: none;
  font-size: 12px;
  color: #c0c4cc;
}
.msg-item-book {
  margin-top: 2px;
  font-size: 12px;
  color: #4e5969;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-item-preview {
  margin-top: 2px;
  font-size: 12px;
  color: #86909c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-dot {
  position: absolute;
  top: 16px;
  right: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f53f3f;
}
.msg-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  background: #eef4f1;
}
.msg-main-head {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: var(--surface-raised);
  border-bottom: 1px solid var(--line);
}
.msg-main-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
}
.msg-main-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #86909c;
}
.msg-chat {
  flex: 1 1 auto;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 18px 20px 8px;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}
.msg-time {
  margin: 8px 0 14px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  align-items: flex-start;
}
.msg-row.mine {
  flex-direction: row-reverse;
}
.msg-bubble-wrap {
  max-width: min(72%, 460px);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.msg-row.mine .msg-bubble-wrap {
  align-items: flex-end;
}
.msg-recall-btn {
  border: none;
  background: transparent;
  padding: 0 2px;
  font-size: 12px;
  color: #86909c;
  cursor: pointer;
  line-height: 1.4;
}
.msg-recall-btn:hover:not(:disabled) {
  color: #0d6b58;
}
.msg-recall-btn:disabled {
  opacity: 0.5;
  cursor: default;
}
.msg-recalled {
  max-width: min(72%, 460px);
  padding: 6px 10px;
  font-size: 12px;
  color: #86909c;
  background: rgba(134, 144, 156, 0.08);
  border-radius: 8px;
}
.msg-bubble {
  padding: 10px 12px;
  background: var(--surface-raised);
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(20, 35, 28, 0.04);
  word-break: break-word;
}
.msg-row.mine .msg-bubble {
  background: linear-gradient(135deg, #0d6b58, #1a8570);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 12px rgba(13, 107, 88, 0.22);
}
.msg-bubble-text {
  font-size: 14px;
  line-height: 1.55;
  white-space: pre-wrap;
}
.msg-bubble-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #86909c;
}
.msg-row.mine .msg-bubble-tip {
  color: rgba(255, 255, 255, 0.8);
}
.msg-composer {
  flex: none;
  padding: 12px 16px 14px;
  background: #fff;
  border-top: 1px solid #eef0f5;
}
.msg-system-tip {
  padding: 16px;
  text-align: center;
  color: #86909c;
  font-size: 13px;
  background: #f7f8fa;
  border-radius: 10px;
}
.msg-input :deep(textarea) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 4px 0 !important;
  resize: none;
}
.msg-composer-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
.msg-counter {
  font-size: 12px;
  color: #c0c4cc;
}
.msg-placeholder {
  flex: 1;
}
@media (max-width: 800px) {
  .msg-page {
    padding-left: 12px;
    padding-right: 12px;
  }
  .msg-shell {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(180px, 34%) 1fr;
    height: calc(100vh - 96px);
    min-height: 560px;
    max-height: calc(100vh - 96px);
  }
  .msg-side {
    max-height: none;
    border-right: 0;
    border-bottom: 1px solid var(--line);
  }
  .msg-main {
    min-height: 0;
  }
}
</style>
