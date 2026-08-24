<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { PropType } from 'vue'

defineOptions({ name: 'XTextButton' })

const props = defineProps({
  modelValue: propTypes.bool.def(false),
  loading: propTypes.bool.def(false),
  preIcon: propTypes.string.def(''),
  postIcon: propTypes.string.def(''),
  title: propTypes.string.def(''),
  type: propTypes.oneOf(['', 'primary', 'success', 'warning', 'danger', 'info']).def('primary'),
  circle: propTypes.bool.def(false),
  round: propTypes.bool.def(false),
  plain: propTypes.bool.def(false),
  onClick: { type: Function as PropType<(...args) => any>, default: null }
})
// XTextButton 恒为文字按钮（Arco type='text'），语义色 success/warning/danger 映射到 status
const getBindValue = computed(() => {
  const result: Record<string, any> = { ...useAttrs() }
  if (result.size === 'default') result.size = 'medium'
  result.type = 'text'
  const t = props.type
  if (t === 'success' || t === 'warning' || t === 'danger') {
    result.status = t
  }
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
