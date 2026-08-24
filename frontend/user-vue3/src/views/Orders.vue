<template>
  <div class="page">
    <h1 class="page-title">我的预约</h1>
    <p class="page-sub">跟进面交进度，确认后即可看到对方联系方式。</p>
    <div v-if="!list.length" class="empty-state">暂无预约记录</div>
    <div v-else class="order-list">
      <article v-for="item in list" :key="item.id" class="order-item">
        <div>
          <h3 class="order-title">{{ item.bookTitle }}</h3>
          <p class="muted">{{ item.orderNo }}</p>
          <p class="order-desc">{{ desc(item) }}</p>
        </div>
        <div class="order-actions">
          <a-button v-if="item.status === 0 && isSeller(item)" type="text" @click="act(() => agreeOrder(item.id))">同意</a-button>
          <a-button v-if="item.status === 1" type="text" @click="act(() => completeOrder(item.id))">确认面交</a-button>
          <a-button v-if="item.status === 0 || item.status === 1" type="text" status="danger" @click="act(() => cancelOrder(item.id, '取消预约'))">取消</a-button>
        </div>
      </article>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { agreeOrder, cancelOrder, completeOrder, getOrders } from '@/api'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const list = ref<any[]>([])
const statusText = (s: number) => ['待卖家确认', '已同意，可面交', '已完成', '已取消'][s]
const isSeller = (item: any) => item.sellerId === user.profile?.id
const desc = (item: any) => {
  const contact = item.sellerWechat || item.sellerMobile || item.buyerWechat || item.buyerMobile
  return statusText(item.status) + (contact ? ' · 联系 ' + contact : ' · 确认后显示联系方式')
    + (item.meetupPlace ? ' · ' + item.meetupPlace : '')
}
const load = async () => {
  list.value = (await getOrders({ pageNo: 1, pageSize: 50 })).list
}
const act = async (fn: () => Promise<any>) => {
  await fn()
  Message.success('已更新')
  await load()
  await user.refreshOrderBadge()
}
onMounted(async () => {
  if (!user.profile) await user.loadProfile()
  await load()
})
</script>
<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface-raised);
  box-shadow: var(--shadow-soft);
}

.order-title {
  margin: 0 0 4px;
  font-size: 1rem;
  font-weight: 650;
}

.order-desc {
  margin: 8px 0 0;
  color: var(--ink-soft);
  font-size: 0.9rem;
  line-height: 1.5;
}

.order-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

@media (max-width: 640px) {
  .order-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
