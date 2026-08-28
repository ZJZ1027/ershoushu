<template>
  <div class="page seller-page">
    <a-spin :loading="loading" style="width: 100%">
      <template v-if="seller">
        <section class="seller-hero panel">
          <button type="button" class="seller-back" @click="router.back()">← 返回</button>
          <div class="seller-main">
            <button
              type="button"
              class="seller-avatar"
              :class="{ clickable: !!avatarSrc }"
              :title="avatarSrc ? '查看头像' : undefined"
              :disabled="!avatarSrc"
              @click="onAvatarClick"
            >
              <img v-if="avatarSrc" :src="avatarSrc" alt="" />
              <span v-else>{{ avatarLetter }}</span>
            </button>
            <div class="seller-info">
              <h1 class="page-title">{{ seller.nickname || '同学' }}</h1>
              <p class="page-sub">{{ signatureText }}</p>
              <div class="seller-stats">
                <router-link :to="'/social/followers/' + seller.id" class="seller-stat">
                  <strong>{{ seller.followerCount || 0 }}</strong>
                  <span>粉丝</span>
                </router-link>
                <router-link :to="'/social/following/' + seller.id" class="seller-stat">
                  <strong>{{ seller.followingCount || 0 }}</strong>
                  <span>关注</span>
                </router-link>
              </div>
              <div class="seller-tags">
                <span v-if="seller.campus" class="seller-tag">{{ seller.campus }}</span>
                <span class="seller-tag muted-tag">在售 {{ seller.onSaleCount || 0 }} 本</span>
              </div>
              <a-button
                v-if="!isSelf"
                class="seller-follow-btn"
                :type="seller.isFollowing ? 'outline' : 'primary'"
                :loading="followLoading"
                @click="onToggleFollow"
              >
                {{ seller.isFollowing ? '已关注' : '+ 关注' }}
              </a-button>
            </div>
          </div>
        </section>

        <div class="seller-books-head">
          <h2 class="home-section-title">TA 的在售书籍</h2>
          <span class="home-count">共 {{ total }} 本</span>
        </div>

        <div v-if="list.length" class="grid">
          <BookGridCard
            v-for="b in list"
            :key="b.id"
            :book="b"
            :condition-label="conditionLabel"
          />
        </div>
        <div v-else class="empty-state">暂无在售书籍</div>

        <div v-if="total > query.pageSize" class="home-pager">
          <a-pagination
            :total="total"
            v-model:current="query.pageNo"
            :page-size="query.pageSize"
            @change="loadBooks"
          />
        </div>
      </template>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import BookGridCard from '@/components/BookGridCard.vue'
import { getBookPage, getDict, getSeller, toggleFollow } from '@/api'
import { fileUrl } from '@/api/http'
import { useAvatarPreview } from '@/composables/useAvatarPreview'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const { open: previewAvatar } = useAvatarPreview()
const loading = ref(true)
const followLoading = ref(false)
const seller = ref<any>()
const list = ref<any[]>([])
const total = ref(0)
const conditions = ref<any[]>([])
const query = reactive({
  pageNo: 1,
  pageSize: 10
})

const sellerId = computed(() => Number(route.params.id))
const isSelf = computed(() => sellerId.value === user.profile?.id)

const avatarSrc = computed(() => fileUrl(seller.value?.avatar))
const avatarLetter = computed(() => {
  const name = seller.value?.nickname || '同学'
  return String(name).trim().charAt(0).toUpperCase() || '同'
})
const signatureText = computed(() => {
  const sig = String(seller.value?.signature || '').trim()
  return sig || '这位同学还没有填写个性签名'
})

const onAvatarClick = () => {
  if (avatarSrc.value) previewAvatar(avatarSrc.value)
}

const onToggleFollow = async () => {
  if (!seller.value?.id || followLoading.value) return
  followLoading.value = true
  try {
    const followed = await toggleFollow(seller.value.id)
    seller.value.isFollowing = followed
    seller.value.followerCount = Math.max(0, Number(seller.value.followerCount || 0) + (followed ? 1 : -1))
    await user.loadProfile()
    Message.success(followed ? '已关注' : '已取消关注')
  } finally {
    followLoading.value = false
  }
}

const conditionLabel = (code?: string) => {
  if (!code) return ''
  return conditions.value.find((d) => d.value === code)?.label || ''
}

const loadBooks = async () => {
  const data = await getBookPage({
    sellerId: sellerId.value,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  })
  list.value = data.list || []
  total.value = data.total || 0
}

const load = async () => {
  if (!sellerId.value) {
    Message.error('卖家不存在')
    router.replace('/')
    return
  }
  loading.value = true
  try {
    seller.value = await getSeller(sellerId.value)
    query.pageNo = 1
    await loadBooks()
  } catch {
    router.replace('/')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    load()
  }
)

onMounted(async () => {
  if (!user.profile) await user.loadProfile()
  conditions.value = (await getDict('book_condition')) || []
  await load()
})
</script>

<style scoped>
.seller-page {
  padding-top: 8px;
}

.seller-hero {
  position: relative;
  margin-bottom: 22px;
}

.seller-back {
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 13px;
  padding: 0;
  margin-bottom: 14px;
  cursor: pointer;
}

.seller-back:hover {
  color: var(--teal-deep);
}

.seller-main {
  display: flex;
  align-items: center;
  gap: 18px;
}

.seller-avatar {
  flex: none;
  width: 72px;
  height: 72px;
  padding: 0;
  border: 2px solid rgba(13, 107, 88, 0.12);
  border-radius: 50%;
  overflow: hidden;
  background: var(--teal-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--teal-deep);
  font-size: 28px;
  font-weight: 700;
}

.seller-avatar.clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.seller-avatar.clickable:hover {
  transform: scale(1.04);
  border-color: rgba(13, 107, 88, 0.28);
  box-shadow: 0 8px 20px rgba(13, 107, 88, 0.12);
}

.seller-avatar:disabled {
  cursor: default;
}

.seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-info {
  min-width: 0;
}

.seller-info .page-title {
  margin-bottom: 4px;
}

.seller-info .page-sub {
  margin-bottom: 10px;
}

.seller-stats {
  display: flex;
  gap: 18px;
  margin-bottom: 10px;
}

.seller-stat {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: inherit;
  font-size: 13px;
}

.seller-stat strong {
  font-size: 16px;
  color: var(--ink);
}

.seller-stat span {
  color: var(--muted);
}

.seller-stat:hover strong {
  color: var(--teal-deep);
}

.seller-follow-btn {
  margin-top: 10px;
}

.seller-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.seller-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(13, 107, 88, 0.08);
  color: var(--teal-deep);
  font-size: 12px;
}

.seller-tag.muted-tag {
  background: rgba(107, 124, 114, 0.1);
  color: var(--muted);
}

.seller-books-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.home-section-title {
  margin: 0;
  font-family: var(--font-brand);
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.home-count {
  color: var(--muted);
  font-size: 13px;
}

.home-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}

@media (max-width: 640px) {
  .seller-main {
    align-items: flex-start;
  }

  .seller-avatar {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }
}
</style>
