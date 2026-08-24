import { useRouter } from 'vue-router'

/** 路由表兜底项：任何没注册的路径都会落到它，最终渲染 404 页 */
const CATCH_ALL = '/:pathMatch(.*)*'

/**
 * 判断某个路径当前用户是否真的能打开。
 *
 * 菜单是按租户/角色授权的，未授权页面的路由压根不会注册，
 * `router.push` 过去只会落到 404 兜底页——所以凡是「引导用户去另一个页面」的入口
 * （空态里的跳转按钮、提示里的链接）都要先问一句可不可达，不可达就别给这个入口。
 *
 * @example
 * const reachable = useRouteReachable()
 * const canGoTask = computed(() => reachable('/biz/task'))
 */
export const useRouteReachable = () => {
  const router = useRouter()
  return (path: string) => router.resolve(path).matched.some((r) => r.path !== CATCH_ALL)
}
