import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import { clearTokens, getAccessToken } from '@/utils/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL,
  timeout: 20000
})

/** 未登录也可访问：失效 Token 时清掉后重试一次，避免公开接口也被 401 */
const PUBLIC_PATHS = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh-token',
  '/book/page',
  '/book/get',
  '/category/list',
  '/notice/list',
  '/dict/data'
]

function isPublicUrl(url?: string) {
  if (!url) return false
  const path = url.split('?')[0]
  return PUBLIC_PATHS.some((p) => path === p || path.endsWith(p))
}

function clearAuth() {
  clearTokens()
  try {
    useUserStore().clear()
  } catch {
    /* Pinia 未就绪时忽略 */
  }
}

http.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  config.headers['tenant-id'] = '1'
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      if (body.code === 401) {
        clearAuth()
        if (!location.pathname.startsWith('/login') && !location.pathname.startsWith('/register')) {
          // 受保护页才跳登录；首页等公开页只清状态
          const needLogin = !['/', ''].includes(location.pathname.replace(/\/$/, '') || '/')
          if (needLogin && location.pathname !== '/') {
            Message.error(body.msg || '账号未登录')
            location.href = '/login?redirect=' + encodeURIComponent(location.pathname)
          }
        }
      } else {
        Message.error(body.msg || '请求失败')
      }
      return Promise.reject(body)
    }
    return body.data
  },
  async (err: AxiosError<any>) => {
    const status = err.response?.status
    const cfg = err.config as InternalAxiosRequestConfig & { _retryWithoutAuth?: boolean }
    const msg = err.response?.data?.msg || err.message || '网络异常'

    if (status === 401 && cfg && !cfg._retryWithoutAuth && isPublicUrl(cfg.url)) {
      // 本地残留失效 Token，公开接口被 Security 判 401：清掉后匿名重试
      clearAuth()
      cfg._retryWithoutAuth = true
      if (cfg.headers) {
        delete cfg.headers.Authorization
      }
      return http.request(cfg)
    }

    if (status === 401) {
      clearAuth()
      Message.error(msg || '账号未登录')
      if (!location.pathname.startsWith('/login')) {
        location.href = '/login?redirect=' + encodeURIComponent(location.pathname + location.search)
      }
      return Promise.reject(err)
    }

    Message.error(msg)
    return Promise.reject(err)
  }
)

export default http

export function fileUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_BASE_URL + url
}
