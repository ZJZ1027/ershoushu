<template>
  <Teleport to="body">
    <Transition name="avatar-preview">
      <div
        v-if="visible && src"
        class="avatar-preview-mask"
        role="dialog"
        aria-modal="true"
        aria-label="查看头像"
        @click="close"
      >
        <button type="button" class="avatar-preview-close" aria-label="关闭" @click.stop="close">
          ×
        </button>

        <div class="avatar-preview-toolbar" @click.stop>
          <button type="button" class="zoom-btn" title="缩小" :disabled="scale <= MIN" @click="zoomOut">−</button>
          <span class="zoom-label">{{ Math.round(scale * 100) }}%</span>
          <button type="button" class="zoom-btn" title="放大" :disabled="scale >= MAX" @click="zoomIn">+</button>
          <button type="button" class="zoom-btn reset" title="还原" :disabled="scale === 1 && !offsetX && !offsetY" @click="resetView">
            1:1
          </button>
        </div>

        <div
          ref="stageRef"
          class="avatar-preview-stage"
          :class="{ dragging }"
          @click.stop
          @pointerdown="onPointerDown"
          @pointermove="onPointerMove"
          @pointerup="onPointerUp"
          @pointercancel="onPointerUp"
          @wheel.prevent="onWheel"
        >
          <img
            class="avatar-preview-img"
            :src="src"
            alt="头像预览"
            :style="imgStyle"
            draggable="false"
            @dblclick.stop="toggleZoom"
          />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useAvatarPreview } from '@/composables/useAvatarPreview'

const MIN = 0.5
const MAX = 3
const STEP = 0.25
const WHEEL_STEP = 0.08

const { visible, src, close } = useAvatarPreview()
const stageRef = ref<HTMLElement>()
const scale = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const dragging = ref(false)
const lastX = ref(0)
const lastY = ref(0)

const clamp = (n: number) => Math.min(MAX, Math.max(MIN, Math.round(n * 100) / 100))

const imgStyle = computed(() => ({
  transform: `translate(${offsetX.value}px, ${offsetY.value}px) scale(${scale.value})`,
  transition: dragging.value ? 'none' : 'transform 0.15s ease'
}))

const maxPan = () => {
  const stage = stageRef.value
  if (!stage) return 240 * scale.value
  return Math.max(stage.clientWidth, stage.clientHeight) * 0.45 * scale.value
}

const clampOffset = () => {
  if (scale.value <= 1) {
    offsetX.value = 0
    offsetY.value = 0
    return
  }
  const limit = maxPan()
  offsetX.value = Math.min(limit, Math.max(-limit, offsetX.value))
  offsetY.value = Math.min(limit, Math.max(-limit, offsetY.value))
}

const resetView = () => {
  scale.value = 1
  offsetX.value = 0
  offsetY.value = 0
}

const zoomIn = () => {
  scale.value = clamp(scale.value + STEP)
  clampOffset()
}

const zoomOut = () => {
  scale.value = clamp(scale.value - STEP)
  clampOffset()
}

const toggleZoom = () => {
  if (scale.value === 1) {
    scale.value = 2
  } else {
    resetView()
  }
}

const onWheel = (e: WheelEvent) => {
  const delta = e.deltaY < 0 ? WHEEL_STEP : -WHEEL_STEP
  scale.value = clamp(scale.value + delta)
  clampOffset()
}

const onPointerDown = (e: PointerEvent) => {
  if (e.button !== 0) return
  dragging.value = true
  lastX.value = e.clientX
  lastY.value = e.clientY
  stageRef.value?.setPointerCapture(e.pointerId)
}

const onPointerMove = (e: PointerEvent) => {
  if (!dragging.value) return
  offsetX.value += e.clientX - lastX.value
  offsetY.value += e.clientY - lastY.value
  lastX.value = e.clientX
  lastY.value = e.clientY
  clampOffset()
}

const onPointerUp = (e: PointerEvent) => {
  dragging.value = false
  try {
    stageRef.value?.releasePointerCapture(e.pointerId)
  } catch {
    /* ignore */
  }
}

watch(visible, (v) => {
  if (v) resetView()
})

const onKey = (e: KeyboardEvent) => {
  if (!visible.value) return
  if (e.key === 'Escape') {
    close()
    return
  }
  if (e.key === '+' || e.key === '=') {
    e.preventDefault()
    zoomIn()
  } else if (e.key === '-' || e.key === '_') {
    e.preventDefault()
    zoomOut()
  } else if (e.key === '0') {
    e.preventDefault()
    resetView()
  }
}

onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => window.removeEventListener('keydown', onKey))
</script>

<style scoped>
.avatar-preview-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: rgba(20, 35, 28, 0.78);
  cursor: zoom-out;
}

.avatar-preview-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  cursor: default;
}

.zoom-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  line-height: 1;
  cursor: pointer;
}

.zoom-btn.reset {
  width: auto;
  min-width: 44px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 650;
}

.zoom-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
}

.zoom-btn:disabled {
  opacity: 0.35;
  cursor: default;
}

.zoom-label {
  min-width: 52px;
  text-align: center;
  color: #fff;
  font-size: 13px;
  font-weight: 650;
  font-variant-numeric: tabular-nums;
}

.avatar-preview-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  width: min(92vw, 720px);
  height: min(78vh, 720px);
  overflow: hidden;
  touch-action: none;
  cursor: grab;
  user-select: none;
}

.avatar-preview-stage.dragging {
  cursor: grabbing;
}

.avatar-preview-img {
  width: min(86vw, 640px, 78vh);
  height: min(86vw, 640px, 78vh);
  max-width: none;
  max-height: none;
  object-fit: cover;
  object-position: center;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.35);
  background: transparent;
  transform-origin: center center;
  user-select: none;
  pointer-events: none;
}

.avatar-preview-close {
  position: absolute;
  top: 18px;
  right: 22px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.avatar-preview-close:hover {
  background: rgba(255, 255, 255, 0.28);
}

.avatar-preview-enter-active,
.avatar-preview-leave-active {
  transition: opacity 0.18s ease;
}

.avatar-preview-enter-from,
.avatar-preview-leave-to {
  opacity: 0;
}
</style>
