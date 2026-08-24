export interface TourStep {
  /** 目标元素选择器，如 [data-tour="company-create"] */
  target: string
  title: string
  content: string
  /** 进入该步前执行（打开抽屉、切视图、建测试数据等）；可 async */
  before?: () => void | Promise<void>
  /** 离开该步（点下一步）时执行（如点「进入」跳转）；可 async */
  onNext?: () => void | Promise<void>
  /** 需要额外等待出现的元素（默认等 target 本身） */
  waitFor?: string
  /** 下一步按钮文案 */
  nextText?: string
  /** 目标四周留白（聚光灯外扩），默认 6 */
  padding?: number
}
