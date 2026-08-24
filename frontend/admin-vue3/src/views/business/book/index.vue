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
    >
      <a-spin :loading="detailLoading" style="width:100%">
        <div v-if="detail" class="book-detail">
          <div class="book-media">
            <img class="book-cover" :src="previewUrl" alt="" />
            <div class="book-thumbs">
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
            <p class="muted">卖家 {{ detail.sellerNickname || '—' }}</p>
            <p v-if="detail.sellerWechat || detail.sellerMobile">
              联系：{{ detail.sellerWechat || '' }} {{ detail.sellerMobile || '' }}
            </p>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { auditBook, getBook, getBookPage, offShelfBook } from '@/api/business/book'
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
  previewSrc.value = ''
  try {
    detail.value = await getBook(id)
    previewSrc.value = detail.value?.coverUrl || detail.value?.imageUrls?.[0] || ''
  } finally {
    detailLoading.value = false
  }
}

const audit = async (id: number, pass: boolean, reason?: string) => {
  await auditBook({ id, pass, rejectReason: reason })
  message.success('已处理')
  detailVisible.value = false
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
  detailVisible.value = false
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
.book-cover {
  width: 100%;
  min-height: 240px;
  max-height: 360px;
  object-fit: cover;
  border-radius: 12px;
  background: #f2f3f5;
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
