<template>
  <div class="infra-page">
    <PageHeader title="会员管理" />
    <div class="list-card">
      <div class="toolbar-left">
        <a-input-search v-model="queryParams.username" placeholder="账号" allow-clear style="width:180px" @search="handleQuery" />
        <a-input-search v-model="queryParams.nickname" placeholder="昵称" allow-clear style="width:180px" @search="handleQuery" />
      </div>
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
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
          <a-table-column title="操作" :width="120">
            <template #cell="{ record }">
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
  </div>
</template>
<script lang="ts" setup>
import { getMemberPage, updateMemberStatus } from '@/api/business/book'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'
defineOptions({ name: 'BusinessMember' })
const message = useMessage()
const badgeStore = useBusinessBadgeStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 20, username: undefined as string | undefined, nickname: undefined as string | undefined })
const getList = async () => {
  loading.value = true
  try {
    const data = await getMemberPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const toggle = async (record: any) => {
  await updateMemberStatus({ id: record.id, status: record.status === 0 ? 1 : 0 })
  message.success('已更新')
  await getList()
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
.toolbar-left { display: flex; gap: 10px; margin-bottom: 16px; }
.list-pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
