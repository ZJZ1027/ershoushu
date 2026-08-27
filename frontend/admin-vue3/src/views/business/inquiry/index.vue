<template>
  <div class="infra-page">
    <PageHeader title="留言抽查" />
    <div class="list-card">
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="书籍" :width="200">
            <template #cell="{ record }">
              <span v-if="record.systemNotice">系统通知</span>
              <span v-else>{{ record.bookTitle || '—' }}</span>
            </template>
          </a-table-column>
          <a-table-column title="对方" data-index="peerNickname" :width="120" />
          <a-table-column title="最近留言" data-index="lastMsg" />
          <a-table-column title="时间" data-index="lastTime" :width="180" />
          <a-table-column title="操作" :width="100">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="open(record.id)">查看</a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <div class="list-pager">
        <a-pagination :total="total" v-model:current="queryParams.pageNo" v-model:page-size="queryParams.pageSize"
                      show-total @change="getList" @page-size-change="getList" />
      </div>
    </div>
    <a-modal v-model:visible="visible" title="留言内容" :footer="false" :width="520">
      <a-list :data="messages" size="small">
        <template #item="{ item }">
          <a-list-item>
            {{ item.senderNickname }}：
            <span v-if="Number(item.recalled) === 1" style="color: var(--color-text-3)">已撤回一条消息</span>
            <span v-else>{{ item.content }}</span>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { getInquiryMessages, getInquiryPage, markAllInquiryRead } from '@/api/business/book'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'
import { onActivated, onMounted } from 'vue'
defineOptions({ name: 'BusinessInquiry' })
const badgeStore = useBusinessBadgeStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 20 })
const visible = ref(false)
const messages = ref<any[]>([])
const getList = async () => {
  loading.value = true
  try {
    const data = await getInquiryPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const open = async (id: number) => {
  messages.value = await getInquiryMessages(id)
  visible.value = true
  await badgeStore.refresh()
}
/** 点进留言抽查菜单：全部视为已读，角标清零 */
const markAllRead = async () => {
  try {
    await markAllInquiryRead()
    badgeStore.inquiry = 0
  } catch {
    /* 忽略，不影响列表 */
  }
  await badgeStore.refresh()
}
const enterPage = async () => {
  await markAllRead()
  await getList()
}
onMounted(enterPage)
onActivated(enterPage)
</script>
<style lang="scss" scoped>
.infra-page { display: flex; flex-direction: column; gap: 16px; }
.list-card { padding: 16px 20px 20px; background: var(--color-bg-2, #fff); border: 1px solid var(--color-border-2, #e5e6eb); border-radius: 8px; }
.list-pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
