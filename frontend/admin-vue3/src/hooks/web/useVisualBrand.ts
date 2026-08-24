import { ref, type Ref } from 'vue'

/**
 * 「视觉体验版」主题探测（body[data-brand='visual']）。
 *
 * 用途：让 JS 层的锦上添花动效（如 KPI 数字滚动增长）**只在视觉体验版启用**；
 * 其余 7 套主题（含线上默认）走原代码路径、行为零变化 —— 这是刻意的生产安全设计：
 * 组件里只需 `if (isVisual.value) { 动效 } else { 原样 }`，非视觉版分支与改动前完全一致。
 *
 * 实现：模块级单例 ref + 一个全局 MutationObserver（监听 body 的 data-brand 变化，
 * 支持运行时切换主题即时生效），整个应用生命周期只创建一次，开销可忽略。
 */
const isVisualBrand = ref(readBrand())

function readBrand(): boolean {
  return typeof document !== 'undefined' && document.body?.getAttribute('data-brand') === 'visual'
}

if (typeof document !== 'undefined' && document.body && typeof MutationObserver !== 'undefined') {
  const mo = new MutationObserver(() => {
    isVisualBrand.value = readBrand()
  })
  mo.observe(document.body, { attributes: true, attributeFilter: ['data-brand'] })
}

export function useVisualBrand(): Ref<boolean> {
  return isVisualBrand
}
