import type { NProgressOptions } from 'nprogress'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 进度条配色统一在 styles/index.scss 的 #nprogress 段里用 Arco 主色变量写死，这里只管开关
export const useNProgress = () => {
  NProgress.configure({ showSpinner: false } as NProgressOptions)

  const start = () => {
    NProgress.start()
  }

  const done = () => {
    NProgress.done()
  }

  return {
    start,
    done
  }
}
