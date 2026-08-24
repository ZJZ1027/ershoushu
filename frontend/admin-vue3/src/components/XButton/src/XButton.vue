<script lang="ts" setup>
import { PropType } from 'vue'
import { propTypes } from '@/utils/propTypes'

defineOptions({ name: 'XButton' })

const props = defineProps({
  modelValue: propTypes.bool.def(false),
  loading: propTypes.bool.def(false),
  preIcon: propTypes.string.def(''),
  postIcon: propTypes.string.def(''),
  title: propTypes.string.def(''),
  type: propTypes.oneOf(['', 'primary', 'success', 'warning', 'danger', 'info']).def(''),
  link: propTypes.bool.def(false),
  circle: propTypes.bool.def(false),
  round: propTypes.bool.def(false),
  plain: propTypes.bool.def(false),
  onClick: { type: Function as PropType<(...args) => any>, default: null }
})
// 把语义化的 type/link/plain/round/circle 翻译成 Arco a-button 的 type/status/shape
const getBindValue = computed(() => {
  // 仅透传非声明 attrs（size/disabled/class/style 等），声明 props 不会出现在 attrs 里
  const result: Record<string, any> = { ...useAttrs() }

  // size：把 'default' 归一到 Arco 的 'medium'
  if (result.size === 'default') result.size = 'medium'

  const t = props.type
  // 形态：link(文字) > plain(描边) > 实心
  if (props.link) {
    result.type = 'text'
  } else if (props.plain) {
    result.type = 'outline'
  } else if (t === '' || t === 'info') {
    result.type = 'secondary'
  } else {
    result.type = 'primary'
  }
  // 语义色：success/warning/danger 映射到 Arco status（文字/描边/实心通用）
  if (t === 'success' || t === 'warning' || t === 'danger') {
    result.status = t
  }
  // 形状
  if (props.circle) {
    result.shape = 'circle'
  } else if (props.round) {
    result.shape = 'round'
  }
  result.loading = props.loading
  return result
})
</script>

<template>
  <a-button v-bind="getBindValue" @click="onClick">
    <Icon v-if="preIcon" :icon="preIcon" class="mr-1px" />
    {{ title ? title : '' }}
    <Icon v-if="postIcon" :icon="postIcon" class="mr-1px" />
  </a-button>
</template>
