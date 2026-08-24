<template>
  <div class="page">
    <a-spin :loading="loading" style="width:100%">
      <div v-if="book" class="detail">
        <div>
          <img :src="previewUrl" class="cover" alt="" />
          <div v-if="gallery.length > 1" class="thumbs">
            <img
              v-for="(u, i) in gallery"
              :key="u + '-' + i"
              :src="fileUrl(u)"
              :class="{ active: previewSrc === u }"
              alt=""
              @click="previewSrc = u"
            />
          </div>
        </div>
        <div class="detail-info panel">
          <h1 class="detail-title">{{ book.title }}</h1>
          <p v-if="statusLabel" class="status-tip">{{ statusLabel }}</p>
          <div class="price">
            <template v-if="book.price != null">¥{{ book.price }}</template>
            <template v-else>未定价</template>
            <span class="muted" v-if="book.originPrice">原价 {{ book.originPrice }}</span>
          </div>
          <p class="muted">{{ book.author || '—' }} · {{ book.publisher || '—' }} · ISBN {{ book.isbn || '—' }}</p>
          <p>成色 {{ book.conditionCode || '—' }} · {{ book.campus || '—' }} · {{ book.meetupPlace || '—' }}</p>
          <p>{{ book.description || '暂无描述' }}</p>
          <p class="muted">卖家 {{ book.sellerNickname || '—' }}</p>
          <p v-if="book.sellerWechat || book.sellerMobile">联系：{{ book.sellerWechat || '' }} {{ book.sellerMobile || '' }}</p>
          <p v-else class="muted">卖家确认预约后显示联系方式</p>
          <div v-if="canTrade" class="detail-actions">
            <a-button type="primary" @click="want">我想要</a-button>
            <a-button @click="fav">{{ book.favorited ? '已收藏' : '收藏' }}</a-button>
            <a-button @click="showMsg = true">留言</a-button>
            <a-button status="danger" @click="openReport">举报</a-button>
          </div>
          <div v-else class="detail-actions">
            <a-button type="primary" @click="router.push('/publish?id=' + book.id)">继续编辑</a-button>
            <a-button @click="router.push('/mine')">返回我的</a-button>
          </div>
        </div>
      </div>
    </a-spin>
    <a-modal v-model:visible="showWant" title="预约面交" @ok="submitWant">
      <a-form layout="vertical">
        <a-form-item label="地点"><a-input v-model="wantForm.meetupPlace" /></a-form-item>
        <a-form-item label="备注"><a-textarea v-model="wantForm.remark" /></a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:visible="showMsg" title="给卖家留言" @ok="submitMsg">
      <a-textarea v-model="msg" placeholder="问问成色、能否议价等" />
    </a-modal>
    <a-modal v-model:visible="showReport" title="举报说明" :on-before-ok="submitReport">
      <a-textarea v-model="reportContent" placeholder="请填写举报原因，例如封面不符、虚假信息等" :auto-size="{ minRows: 3, maxRows: 6 }" />
    </a-modal>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { createReport, getBook, sendInquiry, toggleFavorite, wantBook } from '@/api'
import { fileUrl } from '@/api/http'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const loading = ref(true)
const book = ref<any>()
const showWant = ref(false)
const showMsg = ref(false)
const showReport = ref(false)
const msg = ref('')
const reportContent = ref('')
const wantForm = reactive({ meetupPlace: '', remark: '' })
const previewSrc = ref('')
const gallery = computed(() => {
  const urls = [...(book.value?.imageUrls || [])]
  if (book.value?.coverUrl && !urls.includes(book.value.coverUrl)) {
    urls.unshift(book.value.coverUrl)
  }
  if (!urls.length && book.value?.coverUrl) {
    urls.push(book.value.coverUrl)
  }
  return urls
})
const previewUrl = computed(() => {
  const src = previewSrc.value || gallery.value[0] || book.value?.coverUrl
  return fileUrl(src) || 'https://placehold.co/600x400?text=Book'
})
const canTrade = computed(() => book.value?.status === 1 || book.value?.status === 2)
const statusLabel = computed(() => {
  const map: Record<number, string> = {
    0: '待审核，暂未上架',
    4: '已下架',
    5: '已驳回，请修改后重新提交',
    6: '草稿，尚未提交审核'
  }
  const s = book.value?.status
  return s == null ? '' : map[s] || ''
})

const needLogin = () => {
  if (!user.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

const load = async () => {
  loading.value = true
  try {
    book.value = await getBook(Number(route.params.id))
    wantForm.meetupPlace = book.value.meetupPlace || ''
    previewSrc.value = book.value.coverUrl || book.value.imageUrls?.[0] || ''
  } finally {
    loading.value = false
  }
}

const want = () => {
  if (!needLogin()) return
  showWant.value = true
}
const submitWant = async () => {
  await wantBook({ bookId: book.value.id, meetupPlace: wantForm.meetupPlace, remark: wantForm.remark })
  Message.success('已发起预约，等待卖家确认')
  showWant.value = false
  router.push('/orders')
}
const fav = async () => {
  if (!needLogin()) return
  await toggleFavorite(book.value.id)
  await load()
}
const submitMsg = async () => {
  if (!needLogin()) return
  await sendInquiry({ bookId: book.value.id, content: msg.value })
  Message.success('已发送')
  showMsg.value = false
  router.push('/messages')
}
const openReport = () => {
  if (!needLogin()) return
  reportContent.value = ''
  showReport.value = true
}
const submitReport = async () => {
  const content = reportContent.value.trim()
  if (!content) {
    Message.warning('请填写举报说明')
    return false
  }
  await createReport({ targetType: 1, targetId: book.value.id, reasonCode: 'other', content })
  Message.success('已提交举报')
  reportContent.value = ''
  return true
}

onMounted(load)
</script>
<style scoped>
.cover {
  width: 100%;
  border-radius: var(--radius);
  background: var(--mist);
  min-height: 240px;
  object-fit: cover;
  border: 1px solid var(--line);
}
.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.thumbs img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  box-sizing: border-box;
}
.thumbs img.active {
  border-color: var(--teal);
}
.detail-title {
  margin: 0 0 8px;
  font-family: var(--font-brand);
  font-size: clamp(1.5rem, 2.4vw, 1.9rem);
  letter-spacing: -0.02em;
}
.detail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
}
.status-tip {
  margin: 0 0 8px;
  color: #c46a00;
  font-size: 14px;
}
</style>
