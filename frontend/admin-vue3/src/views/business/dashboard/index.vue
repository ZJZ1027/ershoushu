<template>
  <div class="infra-page">
    <PageHeader title="运营工作台" />
    <a-row :gutter="16">
      <a-col v-for="c in cards" :key="c.label" :xs="12" :lg="8">
        <a-card class="kpi" :bordered="false" hoverable @click="push(c.path)">
          <div class="kpi-label">{{ c.label }}</div>
          <div class="kpi-value">{{ c.value }}</div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { getDashboard } from '@/api/business/book'
defineOptions({ name: 'BusinessDashboard' })
const { push } = useRouter()
const data = ref<any>({})
const cards = computed(() => [
  { label: '待审书籍', value: data.value.pendingBook ?? 0, path: '/business/book' },
  { label: '在售书籍', value: data.value.onSaleBook ?? 0, path: '/business/book' },
  { label: '预约中', value: data.value.reservedBook ?? 0, path: '/business/order' },
  { label: '待确认预约', value: data.value.pendingOrder ?? 0, path: '/business/order' },
  { label: '待处理举报', value: data.value.pendingReport ?? 0, path: '/business/report' },
  { label: '校园用户', value: data.value.memberCount ?? 0, path: '/business/member' }
])
onMounted(async () => {
  data.value = await getDashboard()
})
</script>
<style lang="scss" scoped>
.infra-page { display: flex; flex-direction: column; gap: 16px; }
.kpi { margin-bottom: 16px; cursor: pointer; }
.kpi-label { color: var(--color-text-3); }
.kpi-value { font-size: 28px; font-weight: 600; margin-top: 8px; }
</style>
