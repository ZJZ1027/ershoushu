<template>
  <div class="page mine">
    <section class="mine-hero">
      <div class="mine-hero-main">
        <button
          type="button"
          class="mine-avatar"
          :class="{ clickable: !!avatarSrc }"
          :title="avatarSrc ? '查看头像' : undefined"
          :disabled="!avatarSrc"
          @click="onAvatarClick"
        >
          <img v-if="avatarSrc" :src="avatarSrc" alt="" />
          <span v-else>{{ avatarLetter }}</span>
        </button>
        <div>
          <p class="mine-kicker">个人中心</p>
          <h1 class="page-title">{{ displayName }}</h1>
          <p class="page-sub">{{ signatureText }}</p>
          <div class="mine-stats">
            <router-link :to="'/social/followers/' + user.profile?.id" class="mine-stat">
              <strong>{{ followerCount }}</strong>
              <span>粉丝</span>
            </router-link>
            <router-link :to="'/social/following/' + user.profile?.id" class="mine-stat">
              <strong>{{ followingCount }}</strong>
              <span>关注</span>
            </router-link>
          </div>
        </div>
      </div>
      <a-button type="primary" @click="$router.push('/profile')">个人资料</a-button>
    </section>

    <a-tabs default-active-key="books" class="mine-tabs">
      <a-tab-pane key="books" title="我发布的">
        <div v-if="!books.length" class="empty-state">还没有发布记录</div>
        <div v-else class="mine-list">
          <article v-for="item in books" :key="item.id" class="mine-item">
            <div>
              <h3 class="mine-item-title">{{ item.title }}</h3>
              <p class="muted">{{ bookDesc(item) }}</p>
            </div>
            <div class="mine-actions">
              <a-button type="text" @click="$router.push('/book/' + item.id)">查看</a-button>
              <a-button
                v-if="[0, 1, 5, 6].includes(item.status)"
                type="text"
                @click="$router.push('/publish?id=' + item.id)"
              >编辑</a-button>
              <a-button
                v-if="item.status === 1 || item.status === 0"
                type="text"
                status="danger"
                @click="off(item.id)"
              >下架</a-button>
            </div>
          </article>
        </div>
      </a-tab-pane>
      <a-tab-pane key="following" title="关注">
        <div class="mine-link-bar">
          <router-link :to="'/social/following/' + user.profile?.id">查看全部关注 →</router-link>
        </div>
        <div v-if="!followingList.length" class="empty-state">还没有关注任何人</div>
        <div v-else class="mine-list compact">
          <UserFollowRow
            v-for="item in followingList"
            :key="item.id"
            :user="item"
            :loading="togglingId === item.id"
            @toggle="onToggleFollow"
          />
        </div>
      </a-tab-pane>
      <a-tab-pane key="followers" title="粉丝">
        <div class="mine-link-bar">
          <router-link :to="'/social/followers/' + user.profile?.id">查看全部粉丝 →</router-link>
        </div>
        <div v-if="!followerList.length" class="empty-state">还没有粉丝</div>
        <div v-else class="mine-list compact">
          <UserFollowRow
            v-for="item in followerList"
            :key="item.id"
            :user="item"
            :loading="togglingId === item.id"
            @toggle="onToggleFollow"
          />
        </div>
      </a-tab-pane>
      <a-tab-pane key="fav" title="收藏">
        <div v-if="!favs.length" class="empty-state">暂无收藏</div>
        <div v-else class="mine-list">
          <article v-for="item in favs" :key="item.id" class="mine-item">
            <div>
              <h3 class="mine-item-title">{{ item.title }}</h3>
              <p class="price" style="font-size: 1rem; margin: 4px 0 0">¥{{ item.price }}</p>
            </div>
            <div class="mine-actions">
              <a-button type="text" @click="$router.push('/book/' + item.id)">查看</a-button>
            </div>
          </article>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import UserFollowRow from '@/components/UserFollowRow.vue'
import { getFavorites, getFollowers, getFollowing, getMyBooks, offShelf, toggleFollow } from '@/api'
import { fileUrl } from '@/api/http'
import { useAvatarPreview } from '@/composables/useAvatarPreview'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const { open: previewAvatar } = useAvatarPreview()
const books = ref<any[]>([])
const favs = ref<any[]>([])
const followerList = ref<any[]>([])
const followingList = ref<any[]>([])
const togglingId = ref<number>()
const displayName = computed(() => user.profile?.nickname || user.profile?.username || '我的书栈')
const followerCount = computed(() => user.profile?.followerCount ?? 0)
const followingCount = computed(() => user.profile?.followingCount ?? 0)
const signatureText = computed(
  () => user.profile?.signature || '管理你发布的教材与收藏，完善联系方式方便面交。'
)
const avatarSrc = computed(() => fileUrl(user.profile?.avatar))
const avatarLetter = computed(() => String(displayName.value).trim().charAt(0).toUpperCase() || '我')

const onAvatarClick = () => {
  if (avatarSrc.value) previewAvatar(avatarSrc.value)
}

const statusText = (s: number) => ['待审', '在售', '预约中', '已成交', '已下架', '已驳回', '草稿'][s] || ''
const bookDesc = (item: any) => {
  const price = item.price == null ? '未定价' : '¥' + item.price
  return price + ' · ' + statusText(item.status)
}

const load = async () => {
  if (!user.profile) await user.loadProfile()
  const uid = user.profile?.id
  books.value = (await getMyBooks({ pageNo: 1, pageSize: 50 })).list
  favs.value = (await getFavorites({ pageNo: 1, pageSize: 50 })).list
  if (uid) {
    followingList.value = (await getFollowing({ userId: uid, pageNo: 1, pageSize: 8 })).list || []
    followerList.value = (await getFollowers({ userId: uid, pageNo: 1, pageSize: 8 })).list || []
  }
}

const onToggleFollow = async (targetId: number) => {
  togglingId.value = targetId
  try {
    const followed = await toggleFollow(targetId)
    Message.success(followed ? '已关注' : '已取消关注')
    const updateItem = (list: any[]) => {
      const item = list.find((u) => u.id === targetId)
      if (item) item.isFollowing = followed
    }
    updateItem(followingList.value)
    updateItem(followerList.value)
  } finally {
    togglingId.value = undefined
  }
}

const off = async (id: number) => {
  await offShelf(id)
  Message.success('已下架')
  await load()
}
onMounted(load)
</script>
<style scoped>
.mine-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.mine-hero-main {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.mine-avatar {
  flex: none;
  width: 72px;
  height: 72px;
  padding: 0;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--line);
  background: var(--teal-soft);
  color: var(--teal-deep);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-brand);
  font-size: 1.6rem;
  font-weight: 700;
  cursor: default;
}

.mine-avatar.clickable {
  cursor: zoom-in;
}

.mine-avatar.clickable:hover {
  border-color: var(--teal);
  box-shadow: 0 0 0 3px rgba(13, 107, 88, 0.16);
}

.mine-avatar:disabled {
  cursor: default;
}

.mine-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.mine-kicker {
  margin: 0 0 8px;
  color: var(--teal);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.mine-stats {
  display: flex;
  gap: 18px;
  margin-top: 8px;
}

.mine-stat {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: inherit;
  font-size: 13px;
}

.mine-stat strong {
  font-size: 16px;
  color: var(--ink);
}

.mine-stat span {
  color: var(--muted);
}

.mine-stat:hover strong {
  color: var(--teal-deep);
}

.mine-link-bar {
  margin-bottom: 12px;
  text-align: right;
}

.mine-link-bar a {
  color: var(--teal-deep);
  font-size: 13px;
}

.mine-list.compact {
  gap: 8px;
}

.mine-tabs {
  margin-top: 8px;
}

.mine-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mine-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface-raised);
  box-shadow: var(--shadow-soft);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.mine-item:hover {
  border-color: #b9cfc5;
  transform: translateY(-1px);
}

.mine-item-title {
  margin: 0 0 4px;
  font-family: var(--font-body);
  font-size: 1rem;
  font-weight: 650;
  font-style: normal;
}

.mine-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

@media (max-width: 640px) {
  .mine-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .mine-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
