<template>
  <div class="page">
    <a-spin :loading="loading" style="width:100%">
      <div v-if="book" class="detail">
        <BookImageGallery
          :key="book.id"
          :cover-url="book.coverUrl"
          :image-urls="book.imageUrls"
        />
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
          <router-link v-if="book.sellerId" :to="'/seller/' + book.sellerId" class="detail-seller">
            <span class="detail-seller-avatar">
              <img v-if="sellerAvatarSrc" :src="sellerAvatarSrc" alt="" />
              <span v-else>{{ sellerLetter }}</span>
            </span>
            <span class="detail-seller-meta">
              <span class="detail-seller-name">卖家 {{ book.sellerNickname || '同学' }}</span>
              <span class="detail-seller-hint">点击查看 TA 的在售书籍与个人资料</span>
            </span>
          </router-link>
          <p v-else class="muted">卖家 —</p>
          <p v-if="book.sellerWechat || book.sellerMobile">联系：{{ book.sellerWechat || '' }} {{ book.sellerMobile || '' }}</p>
          <p v-else class="muted">卖家确认预约后显示联系方式</p>
          <div v-if="canTrade" class="detail-actions">
            <a-button type="primary" @click="want">我想要</a-button>
            <a-button @click="fav">{{ book.favorited ? '已收藏' : '收藏' }}</a-button>
            <a-button :loading="openingMsg" @click="goMessage">留言</a-button>
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
    <a-modal v-model:visible="showReport" title="举报说明" :on-before-ok="submitReport">
      <a-textarea v-model="reportContent" placeholder="请填写举报原因，例如封面不符、虚假信息等" :auto-size="{ minRows: 3, maxRows: 6 }" />
    </a-modal>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import BookImageGallery from '@/components/BookImageGallery.vue'
import { createReport, getBook, openInquiry, toggleFavorite, wantBook } from '@/api'
import { fileUrl } from '@/api/http'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const loading = ref(true)
const book = ref<any>()
const showWant = ref(false)
const showReport = ref(false)
const openingMsg = ref(false)
const reportContent = ref('')
const wantForm = reactive({ meetupPlace: '', remark: '' })
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
const sellerAvatarSrc = computed(() => fileUrl(book.value?.sellerAvatar))
const sellerLetter = computed(() => {
  const name = book.value?.sellerNickname || '同学'
  return String(name).trim().charAt(0).toUpperCase() || '同'
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
const goMessage = async () => {
  if (!needLogin()) return
  if (!book.value?.id || openingMsg.value) return
  openingMsg.value = true
  try {
    const inquiryId = await openInquiry(book.value.id)
    await router.push({ path: '/messages', query: { id: String(inquiryId) } })
  } finally {
    openingMsg.value = false
  }
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
.detail-title {
  margin: 0 0 8px;
  font-family: var(--font-body);
  font-size: clamp(1.5rem, 2.4vw, 1.9rem);
  font-weight: 700;
  letter-spacing: -0.01em;
  font-style: normal;
  font-variant-numeric: normal;
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
.detail-seller {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 14px 0 8px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}
.detail-seller:hover {
  border-color: #b9cfc5;
  box-shadow: 0 8px 20px rgba(20, 35, 28, 0.06);
  transform: translateY(-1px);
}
.detail-seller-avatar {
  flex: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--teal-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--teal-deep);
  font-size: 18px;
  font-weight: 700;
}
.detail-seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-seller-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.detail-seller-name {
  font-weight: 650;
  color: var(--ink);
}
.detail-seller-hint {
  color: var(--muted);
  font-size: 12px;
}
</style>
