<template>
  <div
    class="stat-card"
    :class="[toneClass, { 'is-clickable': clickable, 'is-active': active, 'is-compact': compact }]"
  >
    <div class="stat-top">
      <span v-if="icon || $slots.icon" class="stat-icon">
        <slot name="icon"><component :is="icon" /></slot>
      </span>
      <span class="stat-label">{{ label }}</span>
    </div>

    <div class="stat-body">
      <div class="stat-value">
        <span v-if="loading" class="stat-skeleton"></span>
        <template v-else>
          {{ displayValue }}<span v-if="unit" class="stat-unit">{{ unit }}</span>
        </template>
      </div>
      <div v-if="$slots.chart" class="stat-chart">
        <slot name="chart"></slot>
      </div>
    </div>

    <div v-if="$slots.foot || hint" class="stat-foot">
      <slot name="foot">
        <span class="stat-hint">{{ hint }}</span>
      </slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch, onBeforeUnmount, type Component } from 'vue'
import { useVisualBrand } from '@/hooks/web/useVisualBrand'

/**
 * 统一统计卡（Arco / 设计 Token 版）
 * 首页与各业务概览页的 KPI 都用它，别再各写一套 .kpi-card / .stat-card。
 * - tone 决定图标点缀色，取自语义 token，避免页面再自创色值；
 * - chart 插槽放 Sparkline 等迷你图，foot 插槽放环比等自定义页脚。
 */
defineOptions({ name: 'StatCard' })

const props = withDefaults(
  defineProps<{
    label: string
    value: string | number
    unit?: string
    icon?: Component
    tone?: 'brand' | 'success' | 'warning' | 'danger' | 'info' | 'neutral'
    hint?: string
    loading?: boolean
    clickable?: boolean
    active?: boolean
    /** 紧凑变体：降低卡高与字号，用于 4 宫格等信息密度更高的场景 */
    compact?: boolean
    /** 计数为 0 时把点缀色降级为中性灰（避免「临期 0」仍是刺眼的告警红） */
    dimOnZero?: boolean
  }>(),
  {
    unit: '',
    tone: 'brand',
    hint: '',
    loading: false,
    clickable: false,
    active: false,
    compact: false,
    dimOnZero: false
  }
)

// ===== 数字滚动增长（仅「视觉体验版」启用；其余主题走原路径，行为零变化）=====
// rolled == null 表示「不接管」——此时 displayValue 完全等同改动前的逻辑，
// 保证默认商务蓝等 7 套线上主题渲染一模一样。
const isVisual = useVisualBrand()
const rolled = ref<number | null>(null)
const targetNum = computed(() =>
  typeof props.value === 'number' && Number.isFinite(props.value) ? props.value : null
)

let rafId = 0
const rollTo = (to: number) => {
  cancelAnimationFrame(rafId)
  const from = rolled.value ?? 0
  if (from === to) {
    rolled.value = to
    return
  }
  const dur = 720
  const start = performance.now()
  const step = (now: number) => {
    const t = Math.min(1, (now - start) / dur)
    const eased = 1 - Math.pow(1 - t, 3) // easeOutCubic：先快后慢，收尾平滑
    rolled.value = from + (to - from) * eased
    if (t < 1) rafId = requestAnimationFrame(step)
    else rolled.value = to
  }
  rafId = requestAnimationFrame(step)
}

watch(
  [isVisual, targetNum, () => props.loading],
  ([vis, target, loading]) => {
    if (vis && !loading && target != null) {
      if (rolled.value == null || Math.round(rolled.value) !== target) rollTo(target)
    } else {
      // 非视觉版 / 加载中 / 非纯数字：交还给原始 displayValue，不接管
      cancelAnimationFrame(rafId)
      rolled.value = null
    }
  },
  { immediate: true }
)
onBeforeUnmount(() => cancelAnimationFrame(rafId))

// Arco 样式指南：四位以上整数用千分撇分节。纯数字才格式化，
// 字符串值（如「12 / 34」「暂无」）原样输出，避免误伤。
const displayValue = computed(() => {
  if (rolled.value != null) return Math.round(rolled.value).toLocaleString('en-US')
  return typeof props.value === 'number' && Number.isFinite(props.value)
    ? props.value.toLocaleString('en-US')
    : props.value
})

// 计数为 0 且开启 dimOnZero 时，语气降级为中性灰
const isZero = computed(() => {
  const n = typeof props.value === 'number' ? props.value : Number(props.value)
  return Number.isFinite(n) && n === 0
})
const toneClass = computed(() =>
  props.dimOnZero && isZero.value ? 'tone-neutral' : `tone-${props.tone}`
)
</script>

<style scoped lang="scss">

@keyframes stat-shimmer {
  0% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0 50%;
  }
}

.stat-card {
  --c: var(--bm-brand);
  --cbg: var(--bm-brand-bg);

  padding: 16px 18px 13px;
  background: var(--bm-bg-card);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius);
  box-shadow: var(--bm-shadow-card);
  transition: transform var(--bm-dur) var(--bm-ease-out),
    box-shadow var(--bm-dur) var(--bm-ease-out), border-color var(--bm-dur) var(--bm-ease-out);
}

.stat-card.is-clickable {
  cursor: pointer;
}

.stat-card:hover {
  border-color: color-mix(in srgb, var(--c) 22%, transparent);
  transform: translateY(-3px);
  box-shadow: var(--bm-shadow-hover);
}

.stat-card:hover .stat-icon {
  transform: scale(1.08);
}

.stat-card.is-clickable:hover {
  border-color: color-mix(in srgb, var(--c) 38%, transparent);
}

.stat-card.is-active {
  border-color: var(--c);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--c) 16%, transparent);
}

.tone-brand {
  --c: var(--bm-brand);
  --cbg: var(--bm-brand-bg);
}

.tone-success {
  --c: var(--bm-success);
  --cbg: var(--bm-success-bg);
}

.tone-warning {
  --c: var(--bm-warning);
  --cbg: var(--bm-warning-bg);
}

.tone-danger {
  --c: var(--bm-danger);
  --cbg: var(--bm-danger-bg);
}

.tone-info {
  --c: var(--bm-info);
  --cbg: var(--bm-info-bg);
}

.tone-neutral {
  --c: var(--bm-text-3);
  --cbg: var(--bm-fill);
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  display: inline-flex;
  width: 34px;
  height: 34px;
  font-size: 18px;
  color: var(--c);
  background: var(--cbg);
  border-radius: 8px;
  transition: transform var(--bm-dur) var(--bm-ease-out);
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-label {
  overflow: hidden;
  font-size: 14px;
  font-weight: 500;
  color: var(--bm-text-2);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-body {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  margin-top: 12px;
}

.stat-value {
  font-family: var(--bm-font-num);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.02em;
  color: var(--bm-text-1);
  font-variant-numeric: tabular-nums;
}

.stat-unit {
  margin-left: 3px;
  font-size: 14px;
  font-weight: 500;
  color: var(--bm-text-3);
}

.stat-chart {
  flex-shrink: 0;
}

.stat-foot {
  min-height: 18px;
  margin-top: 9px;
}

.stat-hint {
  font-size: 12px;
  color: var(--bm-text-3);
}

/* ===== 紧凑变体：压缩内边距、上下间距与主数字字号，消灭 4 宫格里的大留白 ===== */
.stat-card.is-compact {
  padding: 13px 16px 12px;
}

.stat-card.is-compact .stat-body {
  margin-top: 8px;
}

.stat-card.is-compact .stat-value {
  font-size: 24px;
}

.stat-card.is-compact .stat-icon {
  width: 30px;
  height: 30px;
  font-size: 16px;
  border-radius: 8px;
}

.stat-card.is-compact .stat-foot {
  margin-top: 6px;
}

.stat-skeleton {
  display: inline-block;
  width: 48px;
  height: 26px;
  background: linear-gradient(90deg, var(--bm-fill) 25%, var(--bm-fill-light) 37%, var(--bm-fill) 63%);
  background-size: 400% 100%;
  border-radius: 6px;
  animation: stat-shimmer 1.2s ease infinite;
}
</style>
