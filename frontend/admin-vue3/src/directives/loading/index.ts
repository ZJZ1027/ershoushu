import type { App, Directive, DirectiveBinding } from 'vue'

// v-loading 指令：在目标元素上盖一层加载遮罩（样式随用随注入，不依赖组件库）。
// 用法：v-loading="bool"，遮罩文案取元素上的 element-loading-text 属性。
// 模板 v-loading 与 JSX v-loading={x} 都会解析到全局注册的 'loading' 指令。

const STYLE_ID = 'bm-v-loading-style'

const ensureStyle = () => {
  if (document.getElementById(STYLE_ID)) return
  const style = document.createElement('style')
  style.id = STYLE_ID
  style.textContent = `
.bm-loading-mask {
  position: absolute;
  inset: 0;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: rgba(255, 255, 255, 0.9);
}
.bm-loading-mask__spinner {
  width: 30px;
  height: 30px;
  border: 3px solid var(--color-fill-3, rgba(0, 0, 0, 0.1));
  border-top-color: var(--color-primary-6, #165dff);
  border-radius: 50%;
  animation: bm-loading-spin 0.8s linear infinite;
}
.bm-loading-mask__text {
  font-size: 14px;
  color: var(--color-primary-6, #165dff);
}
@keyframes bm-loading-spin {
  to {
    transform: rotate(360deg);
  }
}
`
  document.head.appendChild(style)
}

interface LoadingEl extends HTMLElement {
  _bmLoading?: {
    mask: HTMLElement
    textEl: HTMLElement
    originalPosition: string
  }
}

const createMask = (el: LoadingEl, text: string) => {
  ensureStyle()
  const mask = document.createElement('div')
  mask.className = 'bm-loading-mask'

  const spinner = document.createElement('div')
  spinner.className = 'bm-loading-mask__spinner'
  mask.appendChild(spinner)

  const textEl = document.createElement('div')
  textEl.className = 'bm-loading-mask__text'
  textEl.textContent = text
  textEl.style.display = text ? '' : 'none'
  mask.appendChild(textEl)

  const position = getComputedStyle(el).position
  const originalPosition = el.style.position
  if (position === 'static' || position === '') {
    el.style.position = 'relative'
  }
  el.appendChild(mask)
  el._bmLoading = { mask, textEl, originalPosition }
}

const removeMask = (el: LoadingEl) => {
  if (!el._bmLoading) return
  el._bmLoading.mask.remove()
  el.style.position = el._bmLoading.originalPosition
  delete el._bmLoading
}

const toggle = (el: LoadingEl, binding: DirectiveBinding) => {
  const text = el.getAttribute('element-loading-text') || ''
  if (binding.value) {
    if (el._bmLoading) {
      el._bmLoading.textEl.textContent = text
      el._bmLoading.textEl.style.display = text ? '' : 'none'
    } else {
      createMask(el, text)
    }
  } else {
    removeMask(el)
  }
}

export const vLoading: Directive = {
  mounted(el: LoadingEl, binding) {
    if (binding.value) {
      toggle(el, binding)
    }
  },
  updated(el: LoadingEl, binding) {
    if (binding.value !== binding.oldValue) {
      toggle(el, binding)
    }
  },
  unmounted(el: LoadingEl) {
    removeMask(el)
  }
}

export const setupLoading = (app: App<Element>) => {
  app.directive('loading', vLoading)
}
