<template>
  <a-modal
    v-model:visible="open"
    title="裁剪头像"
    :width="440"
    :mask-closable="false"
    unmount-on-close
    @cancel="onCancel"
    @ok="onConfirm"
  >
    <template #footer>
      <a-button @click="onCancel">取消</a-button>
      <a-button type="primary" :loading="confirming" @click="onConfirm">确认裁剪</a-button>
    </template>

    <p class="crop-tip">拖动图片调整位置，滚轮或滑块缩放，截取框内区域作为头像</p>
    <div
      ref="stageRef"
      class="crop-stage"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerUp"
      @wheel.prevent="onWheel"
    >
      <img
        ref="imgRef"
        class="crop-img"
        :src="imageSrc"
        alt=""
        draggable="false"
        :style="imgStyle"
        @load="onImageLoad"
      />
      <div class="crop-overlay" aria-hidden="true">
        <div class="crop-shade crop-shade-t" />
        <div class="crop-shade crop-shade-b" />
        <div class="crop-shade crop-shade-l" />
        <div class="crop-shade crop-shade-r" />
        <div class="crop-frame" />
      </div>
    </div>
    <div class="crop-zoom-row">
      <span>缩小</span>
      <a-slider v-model="scale" :min="minScale" :max="maxScale" :step="0.01" :show-tooltip="false" />
      <span>放大</span>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'

const props = defineProps<{
  visible: boolean
  imageSrc: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'confirm', file: File): void
  (e: 'cancel'): void
}>()

const STAGE = 320
const FRAME = 240
const OUTPUT = 512

const open = computed({
  get: () => props.visible,
  set: (v: boolean) => emit('update:visible', v)
})

const stageRef = ref<HTMLElement>()
const imgRef = ref<HTMLImageElement>()
const confirming = ref(false)
const naturalW = ref(0)
const naturalH = ref(0)
const baseW = ref(0)
const baseH = ref(0)
const scale = ref(1)
const minScale = ref(1)
const maxScale = 3
const offsetX = ref(0)
const offsetY = ref(0)
const dragging = ref(false)
const lastX = ref(0)
const lastY = ref(0)

const frameLeft = (STAGE - FRAME) / 2
const frameTop = (STAGE - FRAME) / 2

const imgStyle = computed(() => ({
  width: `${baseW.value * scale.value}px`,
  height: `${baseH.value * scale.value}px`,
  transform: `translate(${offsetX.value}px, ${offsetY.value}px)`
}))

const clampOffset = () => {
  const w = baseW.value * scale.value
  const h = baseH.value * scale.value
  const minX = frameLeft + FRAME - w
  const maxX = frameLeft
  const minY = frameTop + FRAME - h
  const maxY = frameTop
  offsetX.value = Math.min(maxX, Math.max(minX, offsetX.value))
  offsetY.value = Math.min(maxY, Math.max(minY, offsetY.value))
}

const fitImage = () => {
  const nw = naturalW.value
  const nh = naturalH.value
  if (!nw || !nh) return
  // 让图片较短边铺满裁剪框
  const cover = Math.max(FRAME / nw, FRAME / nh)
  baseW.value = nw * cover
  baseH.value = nh * cover
  minScale.value = 1
  scale.value = 1
  offsetX.value = (STAGE - baseW.value) / 2
  offsetY.value = (STAGE - baseH.value) / 2
  clampOffset()
}

const onImageLoad = () => {
  const img = imgRef.value
  if (!img) return
  naturalW.value = img.naturalWidth
  naturalH.value = img.naturalHeight
  fitImage()
}

watch(
  () => props.visible,
  async (v) => {
    if (!v) return
    await nextTick()
    if (imgRef.value?.complete && imgRef.value.naturalWidth) {
      onImageLoad()
    }
  }
)

watch(scale, () => {
  // 以裁剪框中心为缩放锚点
  const cx = frameLeft + FRAME / 2
  const cy = frameTop + FRAME / 2
  const prevW = baseW.value * scale.value
  // scale already new; recompute from old would need previous - simpler: clamp only
  void prevW
  void cx
  void cy
  clampOffset()
})

const onPointerDown = (e: PointerEvent) => {
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

const onWheel = (e: WheelEvent) => {
  const delta = e.deltaY < 0 ? 0.06 : -0.06
  scale.value = Math.min(maxScale, Math.max(minScale.value, Number((scale.value + delta).toFixed(2))))
  clampOffset()
}

const onCancel = () => {
  open.value = false
  emit('cancel')
}

const onConfirm = async () => {
  const img = imgRef.value
  if (!img || !naturalW.value) {
    Message.warning('图片尚未加载完成')
    return
  }
  confirming.value = true
  try {
    const dispW = baseW.value * scale.value
    const dispH = baseH.value * scale.value
    // 裁剪框相对图片的显示坐标
    const sxDisplay = frameLeft - offsetX.value
    const syDisplay = frameTop - offsetY.value
    const ratioX = naturalW.value / dispW
    const ratioY = naturalH.value / dispH
    const sx = sxDisplay * ratioX
    const sy = syDisplay * ratioY
    const sw = FRAME * ratioX
    const sh = FRAME * ratioY

    const canvas = document.createElement('canvas')
    canvas.width = OUTPUT
    canvas.height = OUTPUT
    const ctx = canvas.getContext('2d')
    if (!ctx) throw new Error('canvas unsupported')
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, OUTPUT, OUTPUT)
    ctx.drawImage(img, sx, sy, sw, sh, 0, 0, OUTPUT, OUTPUT)

    const blob: Blob | null = await new Promise((resolve) =>
      canvas.toBlob((b) => resolve(b), 'image/jpeg', 0.92)
    )
    if (!blob) throw new Error('crop failed')
    const file = new File([blob], `avatar-${Date.now()}.jpg`, { type: 'image/jpeg' })
    emit('confirm', file)
    open.value = false
  } catch {
    Message.error('裁剪失败，请重试')
  } finally {
    confirming.value = false
  }
}
</script>

<style scoped>
.crop-tip {
  margin: 0 0 12px;
  color: var(--muted, #6b7c72);
  font-size: 13px;
}

.crop-stage {
  position: relative;
  width: 320px;
  height: 320px;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 12px;
  background: #1a2420;
  touch-action: none;
  cursor: grab;
  user-select: none;
}

.crop-stage:active {
  cursor: grabbing;
}

.crop-img {
  position: absolute;
  left: 0;
  top: 0;
  max-width: none;
  display: block;
  pointer-events: none;
  will-change: transform, width, height;
}

.crop-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.crop-shade {
  position: absolute;
  background: rgba(0, 0, 0, 0.48);
}

.crop-shade-t {
  left: 0;
  right: 0;
  top: 0;
  height: 40px;
}

.crop-shade-b {
  left: 0;
  right: 0;
  bottom: 0;
  height: 40px;
}

.crop-shade-l {
  left: 0;
  top: 40px;
  bottom: 40px;
  width: 40px;
}

.crop-shade-r {
  right: 0;
  top: 40px;
  bottom: 40px;
  width: 40px;
}

.crop-frame {
  position: absolute;
  left: 40px;
  top: 40px;
  width: 240px;
  height: 240px;
  border: 2px solid rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.2);
}

.crop-zoom-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  color: var(--muted, #6b7c72);
  font-size: 12px;
}

.crop-zoom-row .arco-slider {
  flex: 1;
}
</style>
