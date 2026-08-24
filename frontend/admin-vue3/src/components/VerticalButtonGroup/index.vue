<template>
  <a-button-group v-bind="$attrs">
    <slot></slot>
  </a-button-group>
</template>

<script setup lang="ts">
/**
 * 垂直按钮组
 * Arco 官方按钮组只支持水平显示，通过重写样式实现垂直布局
 */
defineOptions({ name: 'VerticalButtonGroup' })
</script>

<style scoped lang="scss">
:deep(.arco-btn-group) {
  display: inline-flex;
  flex-direction: column;
}

:deep(.arco-btn-group .arco-btn) {
  border-radius: 0;
}

:deep(.arco-btn-group .arco-btn:first-child) {
  border-top-right-radius: 2px;
  border-top-left-radius: 2px;
}

:deep(.arco-btn-group .arco-btn:last-child) {
  border-bottom-right-radius: 2px;
  border-bottom-left-radius: 2px;
}

:deep(.arco-btn-group .arco-btn:not(:last-child)) {
  margin-right: 0;
  margin-bottom: -1px;
}
</style>
