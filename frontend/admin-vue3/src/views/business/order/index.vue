<template>
  <div class="infra-page">
    <PageHeader title="预约单" />
    <div class="list-card">
      <div class="toolbar-left">
        <a-input-search v-model="queryParams.orderNo" placeholder="单号" allow-clear style="width:200px" @search="handleQuery" />
        <a-select v-model="queryParams.status" placeholder="状态" allow-clear style="width:140px" @change="handleQuery">
          <a-option :value="0">待确认</a-option>
          <a-option :value="1">已同意</a-option>
          <a-option :value="2">已完成</a-option>
          <a-option :value="3">已取消</a-option>
        </a-select>
      </div>
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="单号" data-index="orderNo" :width="180" />
          <a-table-column title="书名" data-index="bookTitle" />
          <a-table-column title="买家" data-index="buyerNickname" :width="100" />
          <a-table-column title="卖家" data-index="sellerNickname" :width="100" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">{{ ['待确认','已同意','已完成','已取消'][record.status] }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="120">
            <template #cell="{ record }">
              <a-button v-if="record.status === 0 || record.status === 1" type="text" size="small" status="danger"
                        v-hasPermi="['business:order:close']" @click="close(record.id)">关闭</a-button>
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
import { closeOrder, getOrderPage } from '@/api/business/book'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'
defineOptions({ name: 'BusinessOrder' })
const message = useMessage()
const badgeStore = useBusinessBadgeStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 20, orderNo: undefined as string | undefined, status: undefined as number | undefined })
const getList = async () => {
  loading.value = true
  try {
    const data = await getOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const close = async (id: number) => {
  const reason = window.prompt('关闭原因', '管理员关闭')
  if (reason == null) return
  await closeOrder(id, reason)
  message.success('已关闭')
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
</style>
