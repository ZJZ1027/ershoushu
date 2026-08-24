<template>
  <ArcoLayout />
</template>

<script lang="ts" setup>
// Arco Design Pro 风外壳：取代原 Classic/TopLeft/Top/CutMenu 四种布局
// 原 useRenderLayout 体系已下架；如需切回旧布局，恢复本文件为 tsx 分发器即可
import ArcoLayout from '@/layout/arco/index.vue'

defineOptions({ name: 'Layout' })
</script>
