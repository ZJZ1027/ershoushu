// 全屏加载遮罩：命令式调用，自行注入样式与 DOM，不依赖组件库
// 用法：const inst = showFullLoading({ text: '加载中...' }); inst.close()

export interface FullLoadingOptions {
  text?: string
  background?: string
}

export interface FullLoadingInstance {
  close: () => void
}

const STYLE_ID = 'bm-full-loading-style'

const ensureStyle = () => {
  if (document.getElementById(STYLE_ID)) return
  const style = document.createElement('style')
  style.id = STYLE_ID
  style.textContent = `
.bm-full-loading {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #fff;
}
.bm-full-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(255, 255, 255, 0.25);
  border-top-color: var(--color-primary-6, #165dff);
  border-radius: 50%;
  animation: bm-full-loading-spin 0.8s linear infinite;
}
.bm-full-loading__text {
  font-size: 14px;
}
@keyframes bm-full-loading-spin {
  to {
    transform: rotate(360deg);
  }
}
`
  document.head.appendChild(style)
}

export const showFullLoading = (options: FullLoadingOptions = {}): FullLoadingInstance => {
  const { text = '', background = 'rgba(0, 0, 0, 0.7)' } = options
  ensureStyle()
  const mask = document.createElement('div')
  mask.className = 'bm-full-loading'
  mask.style.background = background

  const spinner = document.createElement('div')
  spinner.className = 'bm-full-loading__spinner'
  mask.appendChild(spinner)

  if (text) {
    const label = document.createElement('div')
    label.className = 'bm-full-loading__text'
    label.textContent = text
    mask.appendChild(label)
  }

  document.body.appendChild(mask)

  let closed = false
  return {
    close() {
      if (closed) return
      closed = true
      mask.remove()
    }
  }
}
