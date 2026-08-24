<template>
  <a-input v-model="color">
    <template #prepend>
      <a-color-picker v-model="color" />
    </template>
  </a-input>
</template>

<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'

// 颜色输入框
defineOptions({ name: 'ColorInput' })

const props = defineProps({
  modelValue: propTypes.string.def('')
})
const emit = defineEmits(['update:modelValue'])
const color = computed({
  get: () => {
    return props.modelValue
  },
  set: (val: string) => {
    emit('update:modelValue', val)
  }
})
</script>

<style scoped lang="scss">
:deep(.arco-input-prepend) {
  padding: 0;
}
</style>
