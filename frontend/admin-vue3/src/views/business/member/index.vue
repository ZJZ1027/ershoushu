<template>
  <div class="infra-page">
    <PageHeader title="会员管理" />
    <div class="list-card">
      <div class="toolbar-left">
        <a-input-search v-model="queryParams.username" placeholder="账号" allow-clear style="width:160px" @search="handleQuery" />
        <a-input-search v-model="queryParams.nickname" placeholder="昵称" allow-clear style="width:160px" @search="handleQuery" />
        <a-select v-model="queryParams.avatarAuditStatus" placeholder="头像审核" allow-clear style="width:140px" @change="handleQuery">
          <a-option :value="1">待审头像</a-option>
          <a-option :value="2">已驳回</a-option>
          <a-option :value="0">无待审</a-option>
        </a-select>
      </div>
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="头像" :width="88">
            <template #cell="{ record }">
              <img v-if="record.avatar" class="avatar-img" :src="fileUrl(record.avatar)" alt="" />
              <span v-else class="avatar-empty">无</span>
            </template>
          </a-table-column>
          <a-table-column title="待审头像" :width="120">
            <template #cell="{ record }">
              <div v-if="record.avatarPending" class="pending-cell">
                <img class="avatar-img" :src="fileUrl(record.avatarPending)" alt="" />
                <a-tag v-if="record.avatarAuditStatus === 1" color="orangered" size="small">待审</a-tag>
              </div>
              <span v-else class="muted">—</span>
            </template>
          </a-table-column>
          <a-table-column title="账号" data-index="username" />
          <a-table-column title="昵称" data-index="nickname" />
          <a-table-column title="手机" data-index="mobile" />
          <a-table-column title="微信" data-index="wechat" />
          <a-table-column title="校区" data-index="campus" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">
              <a-tag :color="record.status === 0 ? 'green' : 'red'">{{ record.status === 0 ? '正常' : '停用' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="280">
            <template #cell="{ record }">
              <a-button type="text" size="small" v-hasPermi="['business:member:query']" @click="openDetail(record.id)">资料</a-button>
              <a-button
                v-if="record.avatarAuditStatus === 1"
                type="text"
                size="small"
                v-hasPermi="['business:member:avatar']"
                @click="auditAvatar(record.id, true)"
              >通过头像</a-button>
              <a-button
                v-if="record.avatarAuditStatus === 1"
                type="text"
                size="small"
                status="danger"
                v-hasPermi="['business:member:avatar']"
                @click="openReject(record.id)"
              >驳回头像</a-button>
              <a-button type="text" size="small" v-hasPermi="['business:member:update']"
                        @click="toggle(record)">{{ record.status === 0 ? '停用' : '启用' }}</a-button>
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
      title="会员资料"
      :footer="false"
      width="640px"
      unmount-on-close
    >
      <a-spin :loading="detailLoading" style="width:100%">
        <div v-if="detail" class="member-detail">
          <div class="detail-head">
            <img v-if="detail.avatar" class="detail-avatar" :src="fileUrl(detail.avatar)" alt="" />
            <div v-else class="detail-avatar fallback">{{ (detail.nickname || detail.username || '?').slice(0, 1) }}</div>
            <div>
              <h3 class="detail-name">{{ detail.nickname || detail.username }}</h3>
              <p class="muted">账号 {{ detail.username }} · ID {{ detail.id }}</p>
              <a-tag :color="detail.status === 0 ? 'green' : 'red'">{{ detail.status === 0 ? '正常' : '停用' }}</a-tag>
            </div>
          </div>

          <div class="detail-grid">
            <div class="detail-item"><span class="label">手机</span><span>{{ detail.mobile || '—' }}</span></div>
            <div class="detail-item"><span class="label">微信</span><span>{{ detail.wechat || '—' }}</span></div>
            <div class="detail-item"><span class="label">校区</span><span>{{ detail.campus || '—' }}</span></div>
            <div class="detail-item detail-item-wide"><span class="label">个性签名</span><span>{{ detail.signature || '—' }}</span></div>
            <div class="detail-item"><span class="label">性别</span><span>{{ sexText(detail.sex) }}</span></div>
            <div class="detail-item"><span class="label">邮箱</span><span>{{ detail.email || '—' }}</span></div>
            <div class="detail-item"><span class="label">备注</span><span>{{ detail.remark || '—' }}</span></div>
            <div class="detail-item"><span class="label">注册时间</span><span>{{ detail.createTime || '—' }}</span></div>
            <div class="detail-item"><span class="label">最近登录</span><span>{{ detail.loginDate || '—' }}</span></div>
            <div class="detail-item"><span class="label">登录 IP</span><span>{{ detail.loginIp || '—' }}</span></div>
          </div>

          <div class="detail-avatar-block">
            <h4>头像审核</h4>
            <p class="muted">状态：{{ avatarAuditText(detail.avatarAuditStatus) }}</p>
            <p v-if="detail.avatarRejectReason" class="reject-reason">驳回原因：{{ detail.avatarRejectReason }}</p>
            <div class="avatar-pair">
              <div>
                <div class="muted">当前头像</div>
                <img v-if="detail.avatar" class="avatar-img lg" :src="fileUrl(detail.avatar)" alt="" />
                <span v-else class="avatar-empty">未设置</span>
              </div>
              <div>
                <div class="muted">待审头像</div>
                <img v-if="detail.avatarPending" class="avatar-img lg" :src="fileUrl(detail.avatarPending)" alt="" />
                <span v-else class="avatar-empty">无</span>
              </div>
            </div>
            <div v-if="detail.avatarAuditStatus === 1" class="detail-actions">
              <a-button type="primary" v-hasPermi="['business:member:avatar']" @click="auditAvatar(detail.id, true)">通过头像</a-button>
              <a-button status="danger" v-hasPermi="['business:member:avatar']" @click="openReject(detail.id)">驳回</a-button>
            </div>
          </div>
        </div>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:visible="rejectVisible"
      title="驳回头像"
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
import { auditMemberAvatar, getMember, getMemberPage, updateMemberStatus } from '@/api/business/book'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'

defineOptions({ name: 'BusinessMember' })

const message = useMessage()
const badgeStore = useBusinessBadgeStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectId = ref<number>()
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  username: undefined as string | undefined,
  nickname: undefined as string | undefined,
  avatarAuditStatus: undefined as number | undefined
})

const fileUrl = (url?: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_BASE_URL + url
}

const sexText = (sex?: number) => {
  if (sex === 1) return '男'
  if (sex === 2) return '女'
  return '—'
}

const avatarAuditText = (status?: number) => {
  if (status === 1) return '待审'
  if (status === 2) return '已驳回'
  return '无待审'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getMemberPage(queryParams)
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
  detail.value = undefined
  try {
    detail.value = await getMember(id)
  } finally {
    detailLoading.value = false
  }
}

const toggle = async (record: any) => {
  await updateMemberStatus({ id: record.id, status: record.status === 0 ? 1 : 0 })
  message.success('已更新')
  await getList()
  if (detail.value?.id === record.id) {
    detail.value = await getMember(record.id)
  }
}

const auditAvatar = async (id: number, pass: boolean, reason?: string) => {
  await auditMemberAvatar({ id, pass, rejectReason: reason })
  message.success(pass ? '头像已通过' : '头像已驳回')
  await getList()
  badgeStore.refresh()
  if (detailVisible.value && detail.value?.id === id) {
    detail.value = await getMember(id)
  }
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
  if (!rejectId.value) return false
  await auditAvatar(rejectId.value, false, reason)
  closeReject()
  return true
}

onMounted(() => {
  badgeStore.markMemberSeen()
  getList()
  badgeStore.refresh()
})
</script>
<style lang="scss" scoped>
.infra-page { display: flex; flex-direction: column; gap: 16px; }
.list-card { padding: 16px 20px 20px; background: var(--color-bg-2, #fff); border: 1px solid var(--color-border-2, #e5e6eb); border-radius: 8px; }
.toolbar-left { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px; }
.list-pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.avatar-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background: #f2f3f5;
  display: block;
}
.avatar-img.lg { width: 72px; height: 72px; }
.avatar-empty { color: var(--color-text-3, #86909c); font-size: 13px; }
.pending-cell { display: flex; flex-direction: column; align-items: flex-start; gap: 4px; }
.muted { color: var(--color-text-3, #86909c); font-size: 13px; }
.member-detail { display: flex; flex-direction: column; gap: 20px; }
.detail-head { display: flex; align-items: center; gap: 16px; }
.detail-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  background: #e8f3ef;
  flex: none;
}
.detail-avatar.fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #0d6b58;
}
.detail-name { margin: 0 0 4px; font-size: 18px; }
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 8px;
  background: var(--color-fill-1, #f7f8fa);
}
.detail-item .label {
  color: var(--color-text-3, #86909c);
  font-size: 12px;
}
.detail-item-wide {
  grid-column: 1 / -1;
}
.detail-avatar-block h4 { margin: 0 0 8px; font-size: 15px; }
.reject-reason { color: #c23b3b; margin: 4px 0 10px; }
.avatar-pair { display: flex; gap: 24px; margin-top: 8px; }
.detail-actions { display: flex; gap: 8px; margin-top: 14px; }
@media (max-width: 600px) {
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
