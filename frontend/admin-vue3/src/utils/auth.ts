import { CACHE_KEY, useAuthCache, useCache } from '@/hooks/web/useCache'
import { TokenType } from '@/api/login/types'

/** 令牌与用户缓存：sessionStorage，每个标签页独立 */
const { wsCache: authCache } = useAuthCache()
/** 记住登录表单：localStorage，跨标签共享无妨 */
const { wsCache: localCache } = useCache('localStorage')

const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

function purgeLegacyLocalTokens() {
  localCache.delete(AccessTokenKey)
  localCache.delete(RefreshTokenKey)
}

// ========== 令牌相关 ==========

export const getAccessToken = () => {
  return authCache.get(AccessTokenKey)
}

export const getRefreshToken = () => {
  return authCache.get(RefreshTokenKey)
}

export const setToken = (token: TokenType) => {
  authCache.set(RefreshTokenKey, token.refreshToken)
  authCache.set(AccessTokenKey, token.accessToken)
  purgeLegacyLocalTokens()
  // 与令牌里的租户保持一致：登录后请求不再带 tenant-id 头，服务端从 JWT 的 tenantId 取值，
  // 这里存下来只为登录页回填、以及退出后仍能定位到上次的租户
  const tid = token.tenantId
  if (tid != null) {
    authCache.set(CACHE_KEY.TenantId, Number(tid))
  }
}

export const removeToken = () => {
  authCache.delete(AccessTokenKey)
  authCache.delete(RefreshTokenKey)
}

/** 拼成 Authorization 头的值（后端是 OAuth2 资源服务器，只认 Bearer） */
export const formatToken = (token: string): string => {
  return 'Bearer ' + token
}

// ========== 登录表单相关 ==========

export type LoginFormType = {
  tenantName: string
  username: string
  rememberMe: boolean
}

/** 记住我只回填租户与账号，密码不落本地 */
export const getLoginForm = (): LoginFormType | undefined => {
  return localCache.get(CACHE_KEY.LoginForm)
}

export const setLoginForm = (loginForm: LoginFormType) => {
  localCache.set(CACHE_KEY.LoginForm, loginForm, { exp: 30 * 24 * 60 * 60 })
}

export const removeLoginForm = () => {
  localCache.delete(CACHE_KEY.LoginForm)
}

// ========== 租户相关 ==========

export const getTenantId = () => {
  return authCache.get(CACHE_KEY.TenantId)
}

export const setTenantId = (tenantId: number) => {
  authCache.set(CACHE_KEY.TenantId, tenantId)
}
