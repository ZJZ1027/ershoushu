<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'

defineOptions({ name: 'Tooltip' })

defineProps({
  title: propTypes.string.def(''),
  message: propTypes.string.def(''),
  icon: propTypes.string.def('ep:question-filled')
})
</script>
<template>
  <span>{{ title }}</span>
  <a-tooltip :content="message" position="top">
    <Icon :icon="icon" class="relative top-1px ml-1px" />
  </a-tooltip>
</template>
