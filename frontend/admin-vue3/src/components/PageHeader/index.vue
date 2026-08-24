<template>
  <div class="bm-page-head" :class="{ 'is-title-hidden': !showLeft }">
    <div v-if="showLeft" class="bm-head-left">
      <div v-if="!hideTitle" class="bm-page-title">{{ title }}</div>
      <div v-if="hasSubtitle" class="bm-page-sub">
        <slot name="subtitle">{{ subtitle }}</slot>
      </div>
    </div>
    <div v-if="$slots.default" class="bm-head-right">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, useSlots } from 'vue'

/**
 * 统一页头。
 * 左侧标题(可带副标题)，右侧默认插槽放操作区(按钮/搜索/视图切换等)。
 * 用 bm- 前缀类名 + 组件级 scoped 样式，避免与页面里同名的 .page-head 冲突。
 *
 * hideTitle：隐藏「大标题」那一行（页面已有全局面包屑定位，省去重复大标题，顶部更清爽）。
 *   默认 true —— 全站统一不再显示页面级大标题。副标题(subtitle)不受影响，仍会展示；
 *   若某页确需显示大标题，显式传 :hide-title="false"。
 *   当既无大标题、也无副标题时，头部只余右侧操作区并右对齐。
 */
defineOptions({ name: 'PageHeader' })

const props = withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    hideTitle?: boolean
  }>(),
  { subtitle: '', hideTitle: true }
)

const slots = useSlots()
/** 是否有副标题（prop 或 具名插槽） */
const hasSubtitle = computed(() => !!props.subtitle || !!slots.subtitle)
/** 左侧是否有内容：显示大标题时恒有；隐藏标题时仅在有副标题时保留左块 */
const showLeft = computed(() => !props.hideTitle || hasSubtitle.value)
</script>

<style scoped lang="scss">
.bm-page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--bm-gap, 12px);
  flex-wrap: wrap;
}

/* 隐藏标题时头部只余右侧操作区，整体右对齐 */
.bm-page-head.is-title-hidden {
  justify-content: flex-end;
}

.bm-head-left {
  min-width: 0;
}

.bm-page-title {
  font-size: var(--bm-fs-2xl, 20px);
  font-weight: 700;
  line-height: var(--bm-lh-tight, 1.35);

  /* 大号中文标题略收字距：系统中文字体在 20px+ 下字面显松，收 0.2px 更「立」 */
  letter-spacing: var(--bm-track-tight, -0.2px);
  color: var(--bm-text-1, #1d2129);
}

.bm-page-sub {
  margin-top: var(--bm-gap-xs, 4px);
  font-size: 13px;
  color: var(--bm-text-3, #86909c);
}

/* 副标题单独成行（大标题被隐藏）时去掉相对标题的上间距 */
.bm-page-sub:first-child {
  margin-top: 0;
}

.bm-head-right {
  display: flex;
  align-items: center;
  gap: var(--bm-gap, 12px);
  flex-wrap: wrap;
}
</style>
