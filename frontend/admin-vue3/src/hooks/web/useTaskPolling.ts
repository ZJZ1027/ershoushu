import { getCurrentInstance, onBeforeUnmount, ref } from 'vue'

/**
 * 异步任务状态轮询统一 hook。
 *
 * 页面需要「提交后等后台跑完」时用它，别再各写一套 setInterval：
 * - 递归 setTimeout 节拍：上一轮请求返回后才排下一轮，慢网络下不会请求叠加；
 * - 终态停止：tick 返回 false 即停（如 status 2 成功 / 3 失败）；
 * - 页面不可见暂停（默认开）：后台标签页不再打 API，回到前台立即补一轮；
 * - 组件卸载自动清理，无需各页面再写 onUnmounted。
 *
 * 用法：
 *   const { start, stop } = useTaskPolling({
 *     intervalMs: 3000,
 *     tick: async () => {
 *       const data = await Api.get(id)
 *       render(data)
 *       return data.status === 0 || data.status === 1 // false = 终态停止
 *     }
 *   })
 */
export interface TaskPollingOptions {
  /** 轮询间隔（毫秒） */
  intervalMs: number
  /**
   * 每轮执行的任务。返回 false 表示到达终态、停止轮询；
   * 返回 true / undefined 继续下一轮。抛错不终止（视为网络抖动，下一轮重试）。
   */
  tick: () => Promise<boolean | void> | boolean | void
  /** 页面不可见时暂停轮询，回前台立即补一轮（默认 true） */
  pauseWhenHidden?: boolean
  /** start() 时立即执行一轮而非等第一个间隔（默认 false） */
  immediate?: boolean
}

export function useTaskPolling(options: TaskPollingOptions) {
  const { intervalMs, tick, pauseWhenHidden = true, immediate = false } = options

  /** 是否处于轮询中（外部可用于展示状态） */
  const active = ref(false)
  let timer: ReturnType<typeof setTimeout> | null = null
  let running = false // 单轮进行中标记：防止慢请求与下一轮叠加
  let pendingResume = false // 后台暂停期间错过了轮次，回前台需立即补一轮

  const clear = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  const schedule = () => {
    clear()
    if (!active.value) return
    timer = setTimeout(run, intervalMs)
  }

  const run = async () => {
    if (!active.value) return
    if (pauseWhenHidden && document.visibilityState === 'hidden') {
      // 不排下一轮，挂起等 visibilitychange 唤醒
      pendingResume = true
      return
    }
    if (running) {
      schedule()
      return
    }
    running = true
    try {
      const res = await tick()
      if (res === false) {
        stop()
        return
      }
    } catch {
      // 轮询失败不终止：下一轮重试
    } finally {
      running = false
    }
    schedule()
  }

  const onVisibility = () => {
    if (document.visibilityState === 'visible' && active.value && pendingResume) {
      pendingResume = false
      run()
    }
  }

  /** 开始轮询；已在轮询中则重置节拍（幂等，可在「重新发起任务」后直接再调） */
  const start = () => {
    if (!active.value) {
      active.value = true
      if (pauseWhenHidden) {
        document.addEventListener('visibilitychange', onVisibility)
      }
    }
    if (immediate) {
      run()
    } else {
      schedule()
    }
  }

  /** 停止轮询并清理监听 */
  const stop = () => {
    if (pauseWhenHidden && active.value) {
      document.removeEventListener('visibilitychange', onVisibility)
    }
    active.value = false
    pendingResume = false
    clear()
  }

  // 组件内使用时随卸载自动停止；组件外（如纯工具模块）需自行 stop
  if (getCurrentInstance()) {
    onBeforeUnmount(stop)
  }

  return { start, stop, polling: active }
}
