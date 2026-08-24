/** 同一浏览器共用一份登录态（localStorage），不允许多账号并存 */
const ACCESS = 'accessToken'
const REFRESH = 'refreshToken'

function migrateFromSessionOnce() {
  if (localStorage.getItem(ACCESS)) return
  const sessionAccess = sessionStorage.getItem(ACCESS)
  if (!sessionAccess) return
  localStorage.setItem(ACCESS, sessionAccess)
  const sessionRefresh = sessionStorage.getItem(REFRESH)
  if (sessionRefresh) {
    localStorage.setItem(REFRESH, sessionRefresh)
  }
}

function purgeSessionTokens() {
  sessionStorage.removeItem(ACCESS)
  sessionStorage.removeItem(REFRESH)
}

migrateFromSessionOnce()
purgeSessionTokens()

export function getAccessToken() {
  return localStorage.getItem(ACCESS) || ''
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH) || ''
}

export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(ACCESS, accessToken)
  localStorage.setItem(REFRESH, refreshToken)
  purgeSessionTokens()
}

export function clearTokens() {
  localStorage.removeItem(ACCESS)
  localStorage.removeItem(REFRESH)
  purgeSessionTokens()
}

export const TOKEN_STORAGE_KEY = ACCESS

/** 上次登录表单（账号/密码），供登录页回填 */
const LOGIN_FORM_KEY = 'loginForm'

export type LoginFormSaved = {
  username: string
  password: string
}

export function getLoginForm(): LoginFormSaved | null {
  try {
    const raw = localStorage.getItem(LOGIN_FORM_KEY)
    if (!raw) return null
    const data = JSON.parse(raw) as LoginFormSaved
    if (!data || typeof data.username !== 'string') return null
    return {
      username: data.username,
      password: typeof data.password === 'string' ? data.password : ''
    }
  } catch {
    return null
  }
}

export function setLoginForm(form: LoginFormSaved) {
  localStorage.setItem(
    LOGIN_FORM_KEY,
    JSON.stringify({
      username: form.username.trim(),
      password: form.password
    })
  )
}
