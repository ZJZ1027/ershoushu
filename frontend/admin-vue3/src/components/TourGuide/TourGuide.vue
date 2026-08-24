<template>
  <teleport to="body">
    <div v-if="visible" class="tg-root">
      <!-- 拦截层：挡住背景点击，引导只走气泡按钮驱动 -->
      <div class="tg-catch"></div>
      <!-- 聚光灯：box-shadow 把四周压暗，仅露出目标 -->
      <div class="tg-spot" :style="spotStyle"></div>
      <!-- 气泡 -->
      <div v-if="cur" ref="tipRef" class="tg-tip" :style="tipStyle" :data-placement="placement">
        <div class="tg-arrow" :style="arrowStyle"></div>
        <div class="tg-tip-head">
          <span class="tg-tip-title">{{ cur.title }}</span>
          <span class="tg-tip-count">{{ idx + 1 }}/{{ steps.length }}</span>
        </div>
        <div class="tg-tip-content">{{ cur.content }}</div>
        <div class="tg-tip-foot">
          <a-button size="mini" type="text" class="tg-skip" @click="skip">跳过引导</a-button>
          <div class="tg-tip-btns">
            <a-button v-if="idx > 0" size="small" @click="prev">
              <template #icon><icon-left /></template>
              上一步
            </a-button>
            <a-button size="small" type="primary" :loading="busy" @click="next">
              {{ nextLabel }}
              <template #icon><icon-right v-if="idx < steps.length - 1" /><icon-check v-else /></template>
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onBeforeUnmount, type CSSProperties } from 'vue'
import { IconLeft, IconRight, IconCheck } from '@arco-design/web-vue/es/icon'
import type { TourStep } from './types'

defineOptions({ name: 'TourGuide' })

const emit = defineEmits<{
  (e: 'finish'): void
  (e: 'skip'): void
}>()

const visible = ref(false)
const steps = ref<TourStep[]>([])
const idx = ref(0)
const busy = ref(false)
const tipRef = ref<HTMLElement | null>(null)

const rect = ref<{ top: number; left: number; width: number; height: number } | null>(null)
const placement = ref<'top' | 'bottom'>('bottom')
const tipPos = ref<{ top: number; left: number }>({ top: 0, left: 0 })
const arrowLeft = ref(0)

let currentEl: HTMLElement | null = null
const TIP_W = 340

const cur = computed(() => steps.value[idx.value])
const nextLabel = computed(() => {
  if (cur.value?.nextText) return cur.value.nextText
  return idx.value < steps.value.length - 1 ? '下一步' : '完成'
})

const spotStyle = computed<CSSProperties>(() => {
  const r = rect.value
  if (!r) return { opacity: 0 }
  return {
    top: `${r.top}px`,
    left: `${r.left}px`,
    width: `${r.width}px`,
    height: `${r.height}px`,
    opacity: 1
  }
})
const tipStyle = computed<CSSProperties>(() => ({
  top: `${tipPos.value.top}px`,
  left: `${tipPos.value.left}px`,
  width: `${TIP_W}px`
}))
const arrowStyle = computed<CSSProperties>(() => ({ left: `${arrowLeft.value}px` }))

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms))

/** 轮询等目标元素出现（抽屉动画/异步渲染） */
const waitForEl = async (selector: string, timeout = 4000): Promise<HTMLElement | null> => {
  const deadline = Date.now() + timeout
  while (Date.now() < deadline) {
    const el = document.querySelector(selector) as HTMLElement | null
    if (el && el.getBoundingClientRect().width > 0) return el
    await delay(70)
  }
  return document.querySelector(selector) as HTMLElement | null
}

const computeLayout = () => {
  if (!currentEl) return
  const pad = cur.value?.padding ?? 6
  const b = currentEl.getBoundingClientRect()
  rect.value = {
    top: b.top - pad,
    left: b.left - pad,
    width: b.width + pad * 2,
    height: b.height + pad * 2
  }
  const vw = window.innerWidth
  const vh = window.innerHeight
  const r = rect.value
  const tipH = tipRef.value?.offsetHeight || 150
  // 优先放下方，空间不够放上方
  let place: 'top' | 'bottom' = 'bottom'
  let top = r.top + r.height + 12
  if (top + tipH > vh - 12 && r.top - tipH - 12 > 12) {
    place = 'top'
    top = r.top - tipH - 12
  }
  let left = r.left + r.width / 2 - TIP_W / 2
  left = Math.max(12, Math.min(left, vw - TIP_W - 12))
  placement.value = place
  tipPos.value = { top, left }
  // 箭头对准目标中心
  const targetCenter = r.left + r.width / 2
  arrowLeft.value = Math.max(16, Math.min(targetCenter - left, TIP_W - 16))
}

const enter = async (i: number) => {
  const step = steps.value[i]
  if (!step) return finish()
  if (step.before) {
    busy.value = true
    try {
      await step.before()
    } catch {
      /* before 出错不阻断引导 */
    }
    busy.value = false
  }
  const el = await waitForEl(step.waitFor || step.target)
  if (!el) {
    // 目标始终不出现：跳过该步，避免卡住
    if (i < steps.value.length - 1) return enter(i + 1)
    return finish()
  }
  currentEl = el
  try {
    el.scrollIntoView({ block: 'center', behavior: 'smooth' })
  } catch {
    /* 忽略 */
  }
  await delay(220)
  await nextTick()
  computeLayout()
  // 二次校正（等待 tip 高度稳定 + 滚动完成）
  await delay(60)
  computeLayout()
}

const start = async (list: TourStep[]) => {
  if (!list?.length) return
  steps.value = list
  idx.value = 0
  visible.value = true
  bindListeners()
  await nextTick()
  await enter(0)
}
const stop = () => {
  visible.value = false
  steps.value = []
  currentEl = null
  rect.value = null
  unbindListeners()
}

const next = async () => {
  const step = cur.value
  if (step?.onNext) {
    busy.value = true
    try {
      await step.onNext()
    } catch {
      /* 忽略 */
    }
    busy.value = false
  }
  if (idx.value < steps.value.length - 1) {
    idx.value++
    await enter(idx.value)
  } else {
    finish()
  }
}
const prev = async () => {
  if (idx.value > 0) {
    idx.value--
    await enter(idx.value)
  }
}
const finish = () => {
  const wasVisible = visible.value
  stop()
  if (wasVisible) emit('finish')
}
const skip = () => {
  stop()
  emit('skip')
}

let rafId = 0
const onReposition = () => {
  if (rafId) return
  rafId = requestAnimationFrame(() => {
    rafId = 0
    computeLayout()
  })
}
const bindListeners = () => {
  window.addEventListener('resize', onReposition, true)
  window.addEventListener('scroll', onReposition, true)
}
const unbindListeners = () => {
  window.removeEventListener('resize', onReposition, true)
  window.removeEventListener('scroll', onReposition, true)
}
onBeforeUnmount(unbindListeners)

defineExpose({ start, stop })
</script>

<style lang="scss" scoped>
.tg-root {
  position: fixed;
  inset: 0;
  z-index: 3000;
}

.tg-catch {
  position: absolute;
  inset: 0;
  z-index: 1;

  /* 透明但拦截点击；压暗交给聚光灯的 box-shadow */
  background: transparent;
}

.tg-spot {
  position: fixed;
  z-index: 2;
  pointer-events: none;
  border: 2px solid var(--bm-brand, #165dff);
  border-radius: 8px;
  box-shadow: 0 0 0 9999px rgb(0 0 0 / 55%);
  transition: top var(--bm-dur-slow) var(--bm-ease-out), left var(--bm-dur-slow) var(--bm-ease-out),
    width var(--bm-dur-slow) var(--bm-ease-out), height var(--bm-dur-slow) var(--bm-ease-out);
}

.tg-tip {
  position: fixed;
  z-index: 3;
  padding: 16px 16px 12px;
  background: var(--bm-bg-card, #fff);
  border: 1px solid var(--bm-border-light, #e5e6eb);
  border-radius: 12px;
  box-shadow: 0 12px 40px -8px rgb(0 0 0 / 28%);
  animation: tg-pop 0.2s ease;
}

@keyframes tg-pop {
  from {
    opacity: 0;
    transform: translateY(6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tg-arrow {
  position: absolute;
  width: 12px;
  height: 12px;
  background: var(--bm-bg-card, #fff);
  border: 1px solid var(--bm-border-light, #e5e6eb);
  transform: rotate(45deg);
}

.tg-tip[data-placement='bottom'] .tg-arrow {
  top: -7px;
  border-right: none;
  border-bottom: none;
}

.tg-tip[data-placement='top'] .tg-arrow {
  bottom: -7px;
  border-top: none;
  border-left: none;
}

.tg-tip-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.tg-tip-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--bm-text-1, #1d2129);
}

.tg-tip-count {
  padding: 1px 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--bm-brand, #165dff);
  background: color-mix(in srgb, var(--bm-brand, #165dff) 12%, transparent);
  border-radius: 999px;
  flex-shrink: 0;
}

.tg-tip-content {
  margin: 10px 0 14px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--bm-text-2, #4e5969);
}

.tg-tip-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.tg-skip {
  color: var(--bm-text-3, #86909c);
}

.tg-tip-btns {
  display: flex;
  gap: 8px;
}

.tg-tip-btns :deep(.arco-btn) {
  border-radius: 8px;
}
</style>
