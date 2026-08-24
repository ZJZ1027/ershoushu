<template>
  <div class="infra-page">
    <PageHeader title="举报处理" />
    <div class="list-card">
      <div class="toolbar-left">
        <a-select v-model="queryParams.status" placeholder="状态" allow-clear style="width:140px" @change="handleQuery">
          <a-option :value="0">待处理</a-option>
          <a-option :value="1">已处理</a-option>
          <a-option :value="2">已驳回</a-option>
        </a-select>
      </div>
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="举报人" data-index="reporterNickname" :width="120" />
          <a-table-column title="类型" :width="90">
            <template #cell="{ record }">{{ record.targetType === 1 ? '书籍' : '用户' }}</template>
          </a-table-column>
          <a-table-column title="对象ID" data-index="targetId" :width="90" />
          <a-table-column title="原因" data-index="reasonCode" :width="120" />
          <a-table-column title="说明" data-index="content" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">{{ ['待处理','已处理','已驳回'][record.status] }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="160">
            <template #cell="{ record }">
              <template v-if="record.status === 0">
                <a-button type="text" size="small" v-hasPermi="['business:report:handle']" @click="openHandle(record.id, true)">处理</a-button>
                <a-button type="text" size="small" status="danger" v-hasPermi="['business:report:handle']" @click="openHandle(record.id, false)">驳回</a-button>
              </template>
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
      v-model:visible="showModal"
      :title="pass ? '处理备注' : '驳回备注'"
      unmount-on-close
      :on-before-ok="submitHandle"
      @cancel="closeModal"
    >
      <a-textarea
        v-model="remark"
        placeholder="可选填写处理说明"
        :auto-size="{ minRows: 3, maxRows: 6 }"
      />
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { getReportPage, handleReport } from '@/api/business/book'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'
defineOptions({ name: 'BusinessReport' })
const message = useMessage()
const badgeStore = useBusinessBadgeStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 20, status: 0 as number | undefined })
const showModal = ref(false)
const remark = ref('')
const currentId = ref<number>()
const pass = ref(true)

const getList = async () => {
  loading.value = true
  try {
    const data = await getReportPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const openHandle = (id: number, isPass: boolean) => {
  currentId.value = id
  pass.value = isPass
  remark.value = ''
  showModal.value = true
}
const closeModal = () => {
  showModal.value = false
  currentId.value = undefined
  remark.value = ''
}
const submitHandle = async () => {
  if (currentId.value == null) return false
  try {
    await handleReport(currentId.value, pass.value, remark.value.trim())
    message.success(pass.value ? '已处理' : '已驳回')
    currentId.value = undefined
    remark.value = ''
    await getList()
    await badgeStore.refresh()
    return true
  } catch {
    return false
  }
}
onMounted(getList)
</script>
<style lang="scss" scoped>
.infra-page { display: flex; flex-direction: column; gap: 16px; }
.list-card { padding: 16px 20px 20px; background: var(--color-bg-2, #fff); border: 1px solid var(--color-border-2, #e5e6eb); border-radius: 8px; }
.toolbar-left { display: flex; gap: 10px; margin-bottom: 16px; }
.list-pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
