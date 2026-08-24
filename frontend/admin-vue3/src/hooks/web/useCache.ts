/**
 * 配置浏览器本地存储的方式，可直接存储对象数组。
 */

import WebStorageCache from 'web-storage-cache'

type CacheType = 'localStorage' | 'sessionStorage'

export const CACHE_KEY = {
  // 用户相关
  ROLE_ROUTERS: 'roleRouters',
  USER: 'user',
  // 系统设置
  // 换键作废旧缓存：老逻辑会拿系统 prefers-color-scheme 把 isDark=true 写进来
  // （用户并没选过深色），只改默认值救不了这批已被写脏的浏览器
  IS_DARK: 'isDark.v2',
  LANG: 'lang',
  THEME: 'theme.v2', // Arco Pro 改版后启用新主题键，自动覆盖旧缓存
  LAYOUT: 'layout.v2',
  DICT_CACHE: 'dictCache',
  // 登录表单
  LoginForm: 'loginForm',
  TenantId: 'tenantId'
}

export const useCache = (type: CacheType = 'localStorage') => {
  const wsCache: WebStorageCache = new WebStorageCache({
    storage: type
  })

  return {
    wsCache
  }
}

/** 登录态、用户信息、菜单：按标签页隔离，同浏览器可多账号 */
export const useAuthCache = () => useCache('sessionStorage')

export const deleteUserCache = () => {
  const { wsCache } = useAuthCache()
  wsCache.delete(CACHE_KEY.USER)
  wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
}
