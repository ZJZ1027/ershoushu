import { useUserStore } from '@/store/modules/user'

/**
 * 新手引导串联状态（按登录用户维度存 localStorage）。
 *
 * 设计：首页「使用向导」弹窗是总览，各功能页的聚光灯引导（TourGuide）串成一条链。
 * - dismissed：用户在首页勾选「不再自动弹出」并关闭 = 整条引导彻底关闭，后续各页都不再自动跑。
 * - done：已完成/看过的阶段（'home' / 'company' / 'notice' / ...），完成后该页不再自动弹。
 *
 * 各页自动开跑的判定统一走 shouldRun(stage) = 未 dismissed 且该 stage 未 done。
 */
export type OnboardingStage = 'home' | 'company' | 'notice' | 'project' | 'score' | 'bs'

interface OnboardingState {
  dismissed: boolean
  done: string[]
}

const PREFIX = 'bid_onboarding_'

export function useOnboarding() {
  const uid: string | number = (() => {
    try {
      return useUserStore().getUser?.id ?? 'anon'
    } catch {
      return 'anon'
    }
  })()
  const key = PREFIX + uid

  const read = (): OnboardingState => {
    try {
      const raw = localStorage.getItem(key)
      if (raw) {
        const o = JSON.parse(raw)
        return { dismissed: !!o.dismissed, done: Array.isArray(o.done) ? o.done : [] }
      }
    } catch {
      /* localStorage 不可用 / 解析失败时回落默认 */
    }
    return { dismissed: false, done: [] }
  }
  const write = (s: OnboardingState) => {
    try {
      localStorage.setItem(key, JSON.stringify(s))
    } catch {
      /* 忽略写入失败 */
    }
  }

  const isDismissed = () => read().dismissed
  const dismiss = () => write({ ...read(), dismissed: true })
  // stage 允许子阶段键（如 'company_list'），故用 string
  const isDone = (stage: string) => read().done.includes(stage)
  const markDone = (stage: string) => {
    const s = read()
    if (!s.done.includes(stage)) {
      s.done.push(stage)
      write(s)
    }
  }
  /** 重新开始整条引导（首页手动点「使用向导」时用） */
  const reset = () => write({ dismissed: false, done: [] })
  /** 该阶段是否应自动开跑：未整体关闭且本阶段未完成 */
  const shouldRun = (stage: string) => {
    const s = read()
    return !s.dismissed && !s.done.includes(stage)
  }

  return { isDismissed, dismiss, isDone, markDone, reset, shouldRun }
}
