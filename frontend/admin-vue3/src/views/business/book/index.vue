<template>
  <div class="infra-page">
    <PageHeader title="书籍审核" />
    <div class="list-card">
      <div class="toolbar-left">
        <a-input-search v-model="queryParams.title" placeholder="书名" allow-clear style="width:200px" @search="handleQuery" />
        <a-select v-model="queryParams.status" placeholder="状态" allow-clear style="width:140px" @change="handleQuery">
          <a-option :value="0">待审</a-option>
          <a-option :value="1">在售</a-option>
          <a-option :value="2">预约中</a-option>
          <a-option :value="3">已成交</a-option>
          <a-option :value="4">已下架</a-option>
          <a-option :value="5">已驳回</a-option>
          <a-option :value="6">草稿</a-option>
        </a-select>
      </div>
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="书名" data-index="title" />
          <a-table-column title="卖家" data-index="sellerNickname" :width="120" />
          <a-table-column title="售价" data-index="price" :width="90" />
          <a-table-column title="校区" data-index="campus" :width="100" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">{{ statusText(record.status) }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="260">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="openDetail(record.id)">查看</a-button>
              <a-button v-if="record.status === 0 || record.status === 5" type="text" size="small"
                        v-hasPermi="['business:book:audit']" @click="audit(record.id, true)">通过</a-button>
              <a-button v-if="record.status === 0" type="text" size="small" status="danger"
                        v-hasPermi="['business:book:audit']" @click="openReject(record.id)">驳回</a-button>
              <a-button v-if="record.status === 1 || record.status === 2" type="text" size="small"
                        v-hasPermi="['business:book:offshelf']" @click="offShelf(record.id)">下架</a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <div class="list-pager">
        <a-pagination :total="total" v-model:current="queryParams.pageNo" v-model:page-size="queryParams.pageSize"
                      show-total @change="getList" @page-size-change="handleQuery" />
      </div>
    </div>

    <a-modal
      v-model:visible="detailVisible"
      title="书籍详情"
      :width="860"
      :footer="false"
      unmount-on-close
      @close="closeDetail"
    >
      <a-spin :loading="detailLoading" style="width:100%">
        <div v-if="detail" class="book-detail">
          <div class="book-media">
            <div class="book-cover-shell">
              <button
                v-if="gallery.length > 1"
                type="button"
                class="book-nav book-nav-prev"
                aria-label="上一张"
                @click.stop="shiftGallery(-1)"
              >
                ‹
              </button>
              <button type="button" class="book-cover-btn" title="点击查看完整图片" @click="openImagePreview()">
                <img class="book-cover" :key="'cover-' + detail.id + '-' + previewSrc" :src="previewUrl" alt="" />
                <span class="book-cover-tip">点击查看完整图片</span>
              </button>
              <button
                v-if="gallery.length > 1"
                type="button"
                class="book-nav book-nav-next"
                aria-label="下一张"
                @click.stop="shiftGallery(1)"
              >
                ›
              </button>
            </div>
            <div class="book-thumbs">
              <img
                v-for="(u, i) in gallery"
                :key="detail.id + '-' + u + '-' + i"
                :src="fileUrl(u)"
                :class="{ active: previewSrc === u }"
                alt=""
                @click="previewSrc = u"
                @dblclick="openImagePreview(u)"
              />
            </div>
          </div>
          <div class="book-info">
            <h2 class="book-title">{{ detail.title }}</h2>
            <div class="book-price">
              ¥{{ detail.price }}
              <span v-if="detail.originPrice" class="muted">原价 {{ detail.originPrice }}</span>
            </div>
            <p class="muted">
              {{ detail.author || '—' }} · {{ detail.publisher || '—' }} · ISBN {{ detail.isbn || '—' }}
            </p>
            <p>成色 {{ detail.conditionCode || '—' }} · {{ detail.campus || '—' }} · {{ detail.meetupPlace || '—' }}</p>
            <p v-if="detail.courseName || detail.majorName" class="muted">
              课程 {{ detail.courseName || '—' }} · 专业 {{ detail.majorName || '—' }}
            </p>
            <p v-if="detail.categoryName" class="muted">分类 {{ detail.categoryName }}</p>
            <p>{{ detail.description || '暂无描述' }}</p>
            <button
              v-if="detail.sellerId"
              type="button"
              class="book-seller clickable"
              @click="openSellerProfile"
            >
              <span class="book-seller-avatar">
                <img v-if="sellerAvatarSrc" :src="sellerAvatarSrc" alt="" />
                <span v-else>{{ sellerLetter }}</span>
              </span>
              <div class="book-seller-meta">
                <p class="book-seller-name">卖家 {{ detail.sellerNickname || '—' }}</p>
                <p v-if="detail.sellerWechat || detail.sellerMobile" class="muted">
                  联系：{{ detail.sellerWechat || '' }} {{ detail.sellerMobile || '' }}
                </p>
                <p class="book-seller-hint">点击查看卖家资料</p>
              </div>
            </button>
            <div v-else class="book-seller">
              <span class="book-seller-avatar">{{ sellerLetter }}</span>
              <div class="book-seller-meta">
                <p class="book-seller-name">卖家 —</p>
              </div>
            </div>
            <p class="muted">状态 {{ statusText(detail.status) }}</p>
            <p v-if="detail.rejectReason" class="reject-reason">驳回原因：{{ detail.rejectReason }}</p>
            <div class="book-actions">
              <a-button
                v-if="detail.status === 0 || detail.status === 5"
                type="primary"
                v-hasPermi="['business:book:audit']"
                @click="audit(detail.id, true)"
              >通过</a-button>
              <a-button
                v-if="detail.status === 0"
                status="danger"
                v-hasPermi="['business:book:audit']"
                @click="openReject(detail.id)"
              >驳回</a-button>
              <a-button
                v-if="detail.status === 1 || detail.status === 2"
                v-hasPermi="['business:book:offshelf']"
                @click="offShelf(detail.id)"
              >下架</a-button>
            </div>
          </div>
        </div>
      </a-spin>
    </a-modal>

    <Teleport to="body">
      <div
        v-if="imagePreviewVisible"
        class="image-preview-mask"
        role="dialog"
        aria-modal="true"
        aria-label="查看图片"
        @click="closeImagePreview"
      >
        <button type="button" class="image-preview-close" aria-label="关闭" @click.stop="closeImagePreview">×</button>
        <button
          v-if="gallery.length > 1"
          type="button"
          class="image-nav image-nav-prev"
          aria-label="上一张"
          @click.stop="shiftGallery(-1)"
        >
          ‹
        </button>
        <div class="image-preview-wrap" @click.stop>
          <img :key="imagePreviewUrl" :src="imagePreviewUrl" alt="" />
          <span v-if="gallery.length > 1" class="image-counter">
            {{ previewIndex + 1 }} / {{ gallery.length }}
          </span>
        </div>
        <button
          v-if="gallery.length > 1"
          type="button"
          class="image-nav image-nav-next"
          aria-label="下一张"
          @click.stop="shiftGallery(1)"
        >
          ›
        </button>
      </div>
    </Teleport>

    <a-modal
      v-model:visible="sellerProfileVisible"
      title="卖家资料"
      :footer="false"
      width="640px"
      unmount-on-close
      @close="closeSellerProfile"
    >
      <a-spin :loading="sellerProfileLoading" style="width: 100%">
        <MemberProfilePanel :member="sellerProfile" />
      </a-spin>
    </a-modal>

    <a-modal
      v-model:visible="rejectVisible"
      title="驳回原因"
      unmount-on-close
      :on-before-ok="submitReject"
      @cancel="closeReject"
    >
      <a-textarea
        v-model="rejectReason"
        placeholder="请填写驳回原因"
        :auto-size="{ minRows: 3, maxRows: 6 }"
      />
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import MemberProfilePanel from '@/components/business/MemberProfilePanel.vue'
import { auditBook, getBook, getBookPage, getMember, offShelfBook } from '@/api/business/book'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'

defineOptions({ name: 'BusinessBook' })

const message = useMessage()
const badgeStore = useBusinessBadgeStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  title: undefined as string | undefined,
  status: 0 as number | undefined
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>()
const previewSrc = ref('')

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectId = ref<number>()

const sellerProfileVisible = ref(false)
const sellerProfileLoading = ref(false)
const sellerProfile = ref<any>()

const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')

const statusText = (s: number) => ['待审', '在售', '预约中', '已成交', '已下架', '已驳回', '草稿'][s] || String(s)

const fileUrl = (url?: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_BASE_URL + url
}

const gallery = computed(() => {
  const urls = [...(detail.value?.imageUrls || [])]
  if (detail.value?.coverUrl && !urls.includes(detail.value.coverUrl)) {
    urls.unshift(detail.value.coverUrl)
  }
  return urls
})

const previewUrl = computed(() => {
  const src = previewSrc.value || gallery.value[0] || detail.value?.coverUrl
  return fileUrl(src) || 'https://placehold.co/600x400?text=Book'
})

const previewIndex = computed(() => {
  const current = previewSrc.value || gallery.value[0]
  const idx = gallery.value.indexOf(current)
  return idx >= 0 ? idx : 0
})

const shiftGallery = (delta: number) => {
  if (gallery.value.length <= 1) return
  const next = (previewIndex.value + delta + gallery.value.length) % gallery.value.length
  previewSrc.value = gallery.value[next]
  imagePreviewUrl.value = fileUrl(previewSrc.value)
}

const sellerAvatarSrc = computed(() => fileUrl(detail.value?.sellerAvatar))
const sellerLetter = computed(() => {
  const name = detail.value?.sellerNickname || '卖'
  return String(name).trim().charAt(0).toUpperCase() || '卖'
})

const getList = async () => {
  loading.value = true
  try {
    const data = await getBookPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const openDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  // 先清空，避免第二次打开仍短暂显示上一本书的图/文
  detail.value = undefined
  previewSrc.value = ''
  try {
    detail.value = await getBook(id)
    previewSrc.value = detail.value?.coverUrl || detail.value?.imageUrls?.[0] || ''
  } finally {
    detailLoading.value = false
  }
}

const closeDetail = () => {
  detailVisible.value = false
  detail.value = undefined
  previewSrc.value = ''
  detailLoading.value = false
  closeSellerProfile()
  closeImagePreview()
}

const openImagePreview = (url?: string) => {
  if (url) {
    previewSrc.value = url
  }
  const raw = previewSrc.value || gallery.value[0] || detail.value?.coverUrl
  if (!raw) return
  imagePreviewUrl.value = fileUrl(raw)
  imagePreviewVisible.value = true
}

const closeImagePreview = () => {
  imagePreviewVisible.value = false
}

const onPreviewKey = (e: KeyboardEvent) => {
  if (!imagePreviewVisible.value) return
  if (e.key === 'ArrowLeft') {
    e.preventDefault()
    shiftGallery(-1)
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    shiftGallery(1)
  } else if (e.key === 'Escape') {
    closeImagePreview()
  }
}

watch(imagePreviewVisible, (visible) => {
  if (visible) {
    window.addEventListener('keydown', onPreviewKey)
  } else {
    window.removeEventListener('keydown', onPreviewKey)
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', onPreviewKey)
})

const openSellerProfile = async () => {
  const sellerId = detail.value?.sellerId
  if (!sellerId) return
  sellerProfileVisible.value = true
  sellerProfileLoading.value = true
  sellerProfile.value = undefined
  try {
    sellerProfile.value = await getMember(sellerId)
  } catch {
    message.error('无法加载卖家资料')
    sellerProfileVisible.value = false
  } finally {
    sellerProfileLoading.value = false
  }
}

const closeSellerProfile = () => {
  sellerProfileVisible.value = false
  sellerProfile.value = undefined
  sellerProfileLoading.value = false
}

const audit = async (id: number, pass: boolean, reason?: string) => {
  await auditBook({ id, pass, rejectReason: reason })
  message.success('已处理')
  closeDetail()
  await getList()
  await badgeStore.refresh()
}

const openReject = (id: number) => {
  rejectId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

const closeReject = () => {
  rejectVisible.value = false
  rejectId.value = undefined
  rejectReason.value = ''
}

const submitReject = async () => {
  const reason = rejectReason.value.trim()
  if (!reason) {
    message.warning('请填写驳回原因')
    return false
  }
  if (rejectId.value == null) return false
  try {
    await audit(rejectId.value, false, reason)
    closeReject()
    return true
  } catch {
    return false
  }
}

const offShelf = async (id: number) => {
  await message.delConfirm('确认强制下架？')
  await offShelfBook(id)
  message.success('已下架')
  closeDetail()
  await getList()
  await badgeStore.refresh()
}

onMounted(getList)
</script>
<style lang="scss" scoped>
.infra-page { display: flex; flex-direction: column; gap: 16px; }
.list-card { padding: 16px 20px 20px; background: var(--color-bg-2, #fff); border: 1px solid var(--color-border-2, #e5e6eb); border-radius: 8px; }
.toolbar-left { display: flex; gap: 10px; margin-bottom: 16px; }
.list-pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.book-detail {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 20px;
}
.book-cover-shell {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}
.book-nav {
  position: absolute;
  top: 50%;
  z-index: 2;
  width: 34px;
  height: 34px;
  margin-top: -17px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease, transform 0.2s ease;
}
.book-nav:hover {
  background: rgba(0, 0, 0, 0.62);
}
.book-nav-prev {
  left: 10px;
}
.book-nav-next {
  right: 10px;
}
.book-cover-btn {
  position: relative;
  display: block;
  flex: 1;
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  cursor: zoom-in;
  border-radius: 12px;
  overflow: hidden;
}
.book-cover-btn:hover .book-cover-tip {
  opacity: 1;
}
.book-cover {
  width: 100%;
  min-height: 240px;
  max-height: 360px;
  object-fit: cover;
  display: block;
  border-radius: 12px;
  background: #f2f3f5;
}
.book-cover-tip {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 8px 12px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  color: #fff;
  font-size: 12px;
  text-align: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}
.image-preview-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 72px;
  background: rgba(0, 0, 0, 0.72);
}
.image-preview-close {
  position: absolute;
  top: 18px;
  right: 22px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}
.image-preview-close:hover {
  background: rgba(255, 255, 255, 0.28);
}
.image-nav {
  position: fixed;
  top: 50%;
  z-index: 3001;
  width: 44px;
  height: 44px;
  margin-top: -22px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}
.image-nav:hover {
  background: rgba(0, 0, 0, 0.62);
}
.image-nav-prev {
  left: 24px;
}
.image-nav-next {
  right: 24px;
}
.image-preview-wrap {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-width: min(920px, 100%);
  max-height: 86vh;
}
.image-preview-wrap img {
  max-width: min(920px, 100%);
  max-height: 82vh;
  object-fit: contain;
  display: block;
  background: #fff;
  border-radius: 8px;
}
.image-counter {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.book-thumbs {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.book-thumbs img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  background: #f2f3f5;
}
.book-thumbs img.active {
  border-color: rgb(var(--primary-6, 22 93 255));
}
.book-title {
  margin: 0 0 8px;
  font-size: 22px;
  line-height: 1.3;
}
.book-price {
  margin-bottom: 8px;
  color: #f53f3f;
  font-size: 22px;
  font-weight: 600;
}
.book-price .muted {
  margin-left: 8px;
  color: var(--color-text-3);
  font-size: 13px;
  font-weight: 400;
}
.muted { color: var(--color-text-3); }
.book-seller {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px 0;
  padding: 12px 14px;
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 10px;
  background: var(--color-fill-1, #f7f8fa);
  width: 100%;
  text-align: left;
}
.book-seller.clickable {
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.book-seller.clickable:hover {
  border-color: rgb(var(--primary-6, 22 93 255));
  box-shadow: 0 4px 14px rgba(22, 93, 255, 0.08);
}
.book-seller-avatar {
  flex: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: #e8f3ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgb(var(--primary-6, 22 93 255));
  font-size: 18px;
  font-weight: 600;
}
.book-seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.book-seller-meta {
  min-width: 0;
}
.book-seller-name {
  margin: 0 0 4px;
  color: var(--color-text-1);
  font-weight: 500;
}
.book-seller-meta .muted {
  margin: 0;
}
.book-seller-hint {
  margin: 4px 0 0;
  color: rgb(var(--primary-6, 22 93 255));
  font-size: 12px;
}
.reject-reason { color: #f53f3f; }
.book-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
}
@media (max-width: 768px) {
  .book-detail { grid-template-columns: 1fr; }
}
</style>
