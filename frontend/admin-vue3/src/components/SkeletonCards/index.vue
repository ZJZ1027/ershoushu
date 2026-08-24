<template>
  <a-skeleton v-for="i in count" :key="i" class="sk-card" animation>
    <div class="skc-head">
      <a-skeleton-shape v-if="avatar" shape="square" size="small" class="skc-avatar" />
      <a-skeleton-line :rows="1" :widths="[titleWidth(i)]" :line-height="14" />
    </div>
    <a-skeleton-line
      :rows="lines"
      :widths="bodyWidths(i)"
      :line-height="10"
      :line-spacing="12"
    />
    <div v-if="footer" class="skc-foot">
      <a-skeleton-line :rows="1" :widths="['32%']" :line-height="10" />
    </div>
  </a-skeleton>
</template>

<script lang="ts" setup>
/**
 * 卡片网格的占位骨架。
 *
 * 为什么不用 a-spin：转圈只说明「在转」，不说明「等下会出现什么」，
 * 卡片刷出来的瞬间整块区域高度突变，视觉上是一次跳变。骨架先把版式占住，
 * 内容到位时只是「填色」，没有布局位移。
 *
 * 组件是多根节点（fragment），直接放进各页面的 grid 容器里就能继承其列宽与间距，
 * 不需要每个页面各写一套占位。
 *
 * @example
 * <div class="card-grid">
 *   <SkeletonCards v-if="loading && !list.length" :count="6" />
 *   <div v-else v-for="row in list" ... />
 * </div>
 */
defineOptions({ name: 'SkeletonCards' })

const props = withDefaults(
  defineProps<{
    count?: number
    /** 卡片正文占位行数 */
    lines?: number
    /** 标题左侧是否有头像/图标位 */
    avatar?: boolean
    /** 卡片底部是否有一条操作/元信息占位 */
    footer?: boolean
  }>(),
  { count: 6, lines: 2, avatar: true, footer: true }
)

// 真实卡片的标题长短不一，占位若整齐划一反而假。按序号取几档宽度错开。
const TITLE_WIDTHS = ['68%', '52%', '76%', '44%', '62%', '58%']
const BODY_WIDTHS = [
  ['100%', '72%'],
  ['88%', '60%'],
  ['100%', '84%'],
  ['76%', '52%']
]

const titleWidth = (i: number) => TITLE_WIDTHS[(i - 1) % TITLE_WIDTHS.length]

const bodyWidths = (i: number) => {
  const base = BODY_WIDTHS[(i - 1) % BODY_WIDTHS.length]
  // lines 超过基准档位时，多出来的行统一收窄，模拟末行不满
  return props.lines <= base.length
    ? base.slice(0, props.lines)
    : [...base, ...Array(props.lines - base.length).fill('66%')]
}
</script>

<style scoped lang="scss">
.sk-card {
  padding: 16px;
  background: var(--bm-bg-card, #fff);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius, 10px);
}

.skc-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

/* Arco 的 shape 默认块级会把标题挤到下一行，这里固定成小方块并禁止收缩 */
.skc-avatar :deep(.arco-skeleton-shape) {
  flex: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.skc-head :deep(.arco-skeleton-line) {
  flex: 1;
  min-width: 0;
}

.skc-foot {
  padding-top: 12px;
  margin-top: 16px;
  border-top: 1px solid var(--bm-border);
}
</style>
