/**
 * 新手指引「聚焦高亮」：在目标元素周围画一个会呼吸闪烁的包围圈，引导用户点击。
 *
 * 自包含：不依赖目标元素改类名，只在 body 上叠一个跟随目标位置的环形层，
 * 结束或点击目标后自动移除。目标可能在路由切换后才渲染，故内置轮询等待。
 */
const STYLE_ID = 'ob-guide-spotlight-style'

const ensureStyle = () => {
  if (document.getElementById(STYLE_ID)) return
  const style = document.createElement('style')
  style.id = STYLE_ID
  style.textContent = `
@keyframes ob-guide-pulse {
  0%   { box-shadow: 0 0 0 0 color-mix(in srgb, var(--bm-brand, #165dff) 50%, transparent); }
  100% { box-shadow: 0 0 0 16px color-mix(in srgb, var(--bm-brand, #165dff) 0%, transparent); }
}
.ob-guide-ring {
  position: fixed;
  z-index: 2000;
  border: 2px solid var(--bm-brand, #165dff);
  border-radius: 10px;
  pointer-events: none;
  opacity: 1;
  transition: opacity 0.3s ease;
  animation: ob-guide-pulse 1.35s ease-out infinite;
}
`
  document.head.appendChild(style)
}

interface SpotlightOptions {
  /** 高亮持续时长（ms），到点自动淡出移除 */
  duration?: number
  /** 环相对目标外扩的内边距（px） */
  padding?: number
}

/**
 * 高亮某个选择器命中的元素。若元素尚未渲染（路由刚跳转），会轮询等待最多 ~2.5s。
 */
export function spotlightTarget(selector: string, options: SpotlightOptions = {}) {
  const duration = options.duration ?? 4200
  const padding = options.padding ?? 6
  ensureStyle()

  let tries = 0
  const tryFind = () => {
    const el = document.querySelector<HTMLElement>(selector)
    if (!el) {
      // 目标可能在路由跳转 + 详情异步加载 + tab 渲染后才出现，最多轮询 ~5s
      if (tries++ < 50) {
        window.setTimeout(tryFind, 100)
      }
      return
    }
    el.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' })
    // 等平滑滚动基本到位再画环，避免环追不上
    window.setTimeout(() => showRing(selector, duration, padding), 380)
  }
  tryFind()
}

/**
 * 画环并逐帧跟随目标。
 *
 * ⚠ 不持有元素引用而是每帧按选择器重新解析：布局层 router-view 以 fullPath 为 key，
 * 引导组件清除 ?guide 参数会触发页面组件重挂载（叠加 out-in 页面过渡动画），
 * 目标按钮会销毁重建一次。死绑旧元素会在重挂载瞬间测得 0 尺寸而误收环——
 * 这里对短暂缺失最多容忍 GRACE_MS，期间隐藏环，目标回来后继续跟随。
 */
const showRing = (selector: string, duration: number, padding: number) => {
  const GRACE_MS = 1800
  const ring = document.createElement('div')
  ring.className = 'ob-guide-ring'
  document.body.appendChild(ring)

  const start = performance.now()
  let el: HTMLElement | null = document.querySelector<HTMLElement>(selector)
  let missingSince = 0
  let rafId = 0
  let finished = false

  const onDocClick = (e: MouseEvent) => {
    // 用户点了目标（或其内部）就立刻收圈；用文档级监听，元素重建后依然有效
    if (el && e.target instanceof Node && el.contains(e.target)) cleanup()
  }

  const cleanup = () => {
    if (finished) return
    finished = true
    cancelAnimationFrame(rafId)
    ring.style.opacity = '0'
    window.setTimeout(() => ring.remove(), 320)
    document.removeEventListener('click', onDocClick, true)
  }

  const tick = (now: number) => {
    if (!el || !el.isConnected) {
      el = document.querySelector<HTMLElement>(selector)
    }
    const rect = el?.getBoundingClientRect()
    if (!rect || rect.width === 0 || rect.height === 0) {
      // 目标暂时不存在/不可见（页面重挂载、过渡动画中）：先隐环等待，超时才放弃
      if (!missingSince) missingSince = now
      if (now - missingSince > GRACE_MS) {
        cleanup()
        return
      }
      ring.style.opacity = '0'
      rafId = requestAnimationFrame(tick)
      return
    }
    missingSince = 0
    ring.style.opacity = '1'
    ring.style.top = `${rect.top - padding}px`
    ring.style.left = `${rect.left - padding}px`
    ring.style.width = `${rect.width + padding * 2}px`
    ring.style.height = `${rect.height + padding * 2}px`
    if (now - start < duration) {
      rafId = requestAnimationFrame(tick)
    } else {
      cleanup()
    }
  }
  rafId = requestAnimationFrame(tick)
  document.addEventListener('click', onDocClick, true)
}
