<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { isNumber } from '@/utils/is'

// 基于 Arco a-modal 的弹窗封装，对外提供：
// v-model 开关 / title 与 #title、#footer 插槽 / width / fullscreen 全屏切换 / scroll + maxHeight 内容滚动 / @opened、@closed。
defineOptions({ name: 'Dialog', inheritAttrs: false })

const slots = useSlots()
const attrs = useAttrs()
const emits = defineEmits(['update:modelValue', 'opened', 'closed'])

const props = defineProps({
  modelValue: propTypes.bool.def(false),
  title: propTypes.string.def('Dialog'),
  fullscreen: propTypes.bool.def(true), // 是否展示「全屏切换」按钮
  width: propTypes.oneOfType([String, Number]).def('40%'),
  scroll: propTypes.bool.def(false), // 是否开启滚动条。如果是的话，按照 maxHeight 设置最大高度
  maxHeight: propTypes.oneOfType([String, Number]).def('400px')
})

// 透传剩余 attrs：剔除已在上面显式处理的 props，以及调用方可能传入、但 a-modal 不认识的别名
const getBindValue = computed(() => {
  const delArr = [
    'modelValue',
    'title',
    'fullscreen',
    'width',
    'scroll',
    'maxHeight',
    'max-height',
    'canFullscreen',
    'append-to-body',
    'appendToBody',
    'close-on-click-modal',
    'closeOnClickModal',
    'lock-scroll',
    'lockScroll',
    'onOpened',
    'onClosed',
    'onUpdate:modelValue'
  ]
  const obj: Recordable = { ...attrs }
  for (const key of delArr) {
    delete obj[key]
  }
  return obj
})

const isFullscreen = ref(false)

const toggleFull = () => {
  isFullscreen.value = !unref(isFullscreen)
}

const dialogHeight = computed(() => {
  if (isFullscreen.value) return 'calc(100vh - 120px)'
  return isNumber(props.maxHeight) ? `${props.maxHeight}px` : props.maxHeight
})

const scrollStyle = computed(() => ({
  maxHeight: unref(dialogHeight),
  overflow: 'auto'
}))

const closing = ref(false)

const onVisibleChange = (val: boolean) => {
  emits('update:modelValue', val)
  if (!val) {
    closing.value = true
  }
}

const closeByIcon = () => {
  emits('update:modelValue', false)
  closing.value = true
}

const onOpen = () => {
  emits('opened')
}

const onClose = () => {
  closing.value = false
  emits('closed')
}
</script>

<template>
  <a-modal
    v-bind="getBindValue"
    :visible="modelValue"
    :width="width"
    :fullscreen="isFullscreen"
    :footer="Boolean(slots.footer)"
    :draggable="!isFullscreen"
    :closable="false"
    :mask-closable="true"
    :unmount-on-close="true"
    title-align="start"
    class="com-dialog"
    @update:visible="onVisibleChange"
    @open="onOpen"
    @close="onClose"
  >
    <template #title>
      <div class="com-dialog__header">
        <div class="com-dialog__title">
          <slot name="title">{{ title }}</slot>
        </div>
        <div class="com-dialog__actions">
          <Icon
            v-if="fullscreen"
            class="is-hover mr-10px cursor-pointer"
            :icon="isFullscreen ? 'radix-icons:exit-full-screen' : 'radix-icons:enter-full-screen'"
            color="var(--color-text-3)"
            hover-color="var(--color-primary-6)"
            @click="toggleFull"
          />
          <Icon
            class="is-hover cursor-pointer"
            icon="ep:close"
            color="var(--color-text-3)"
            hover-color="var(--color-primary-6)"
            @click.stop="closeByIcon"
          />
        </div>
      </div>
    </template>

    <div v-if="scroll" :style="scrollStyle">
      <slot></slot>
    </div>
    <slot v-else></slot>

    <template v-if="slots.footer" #footer>
      <div :style="{ 'pointer-events': closing ? 'none' : 'auto' }">
        <slot name="footer"></slot>
      </div>
    </template>
  </a-modal>
</template>

<style lang="scss">
.com-dialog {
  .arco-modal-header {
    height: 54px;
  }

  .arco-modal-title {
    flex: 1;
  }

  .com-dialog__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .com-dialog__title {
    font-weight: 600;
  }

  .com-dialog__actions {
    display: flex;
    align-items: center;
  }
}
</style>
