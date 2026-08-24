<template>
  <a-input v-model="modelValue" v-bind="$attrs">
    <template #append>
      <a-color-picker v-model="color" />
    </template>
  </a-input>
</template>

<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { useVModels } from '@vueuse/core'

/**
 * 带颜色选择器输入框
 */
defineOptions({ name: 'InputWithColor' })

const props = defineProps({
  modelValue: propTypes.string.def('').isRequired,
  color: propTypes.string.def('').isRequired
})
const emit = defineEmits(['update:modelValue', 'update:color'])
const { modelValue, color } = useVModels(props, emit)
</script>
<style scoped lang="scss">
:deep(.arco-input-append) {
  padding: 0;
}
</style>
