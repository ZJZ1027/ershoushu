import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '@/utils/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL,
  timeout: 20000
})

/** 未登录也可访问：刷新失败后可清掉失效 Token，匿名重试 */
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

type RetryConfig = InternalAxiosRequestConfig & {
  _retry?: boolean
  _retryWithoutAuth?: boolean
}

let refreshing = false
let waitQueue: Array<(token: string | null) => void> = []

function isPublicUrl(url?: string) {
  if (!url) return false
  const path = url.split('?')[0]
  return PUBLIC_PATHS.some((p) => path === p || path.endsWith(p))
}

function isAuthUrl(url?: string) {
  if (!url) return false
  return url.includes('/auth/login') || url.includes('/auth/register') || url.includes('/auth/refresh-token')
}

function clearAuth() {
  clearTokens()
  try {
    useUserStore().clear()
  } catch {
    /* Pinia 未就绪时忽略 */
  }
}

function redirectLogin(msg?: string) {
  if (location.pathname.startsWith('/login') || location.pathname.startsWith('/register')) {
    return
  }
  Message.error(msg || '登录已过期，请重新登录')
  location.href = '/login?redirect=' + encodeURIComponent(location.pathname + location.search)
}

/** 用独立 axios，避免走进自身拦截器造成死循环 */
async function requestNewAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null
  try {
    const base = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL
    const res = await axios.post(
      `${base}/auth/refresh-token`,
      null,
      {
        params: { refreshToken },
        headers: { 'tenant-id': '1' },
        timeout: 20000
      }
    )
    const body = res.data
    if (!body || body.code !== 200 || !body.data?.accessToken) {
      return null
    }
    const accessToken = String(body.data.accessToken)
    const nextRefresh = String(body.data.refreshToken || refreshToken)
    setTokens(accessToken, nextRefresh)
    try {
      useUserStore().setToken(accessToken, nextRefresh)
    } catch {
      /* ignore */
    }
    return accessToken
  } catch {
    return null
  }
}

function enqueueWhileRefreshing(config: RetryConfig) {
  return new Promise((resolve, reject) => {
    waitQueue.push((token) => {
      if (!token) {
        reject(new Error('unauthorized'))
        return
      }
      config._retry = true
      config.headers = config.headers || {}
      config.headers.Authorization = 'Bearer ' + token
      resolve(http.request(config))
    })
  })
}

async function retryAsGuest(config: RetryConfig) {
  clearAuth()
  config._retryWithoutAuth = true
  if (config.headers) {
    delete config.headers.Authorization
  }
  return http.request(config)
}

/**
 * 401 处理：优先用 refreshToken 换新 accessToken 并重放请求；
 * 公开接口刷新失败则匿名重试；受保护接口刷新失败才跳登录。
 */
async function handle401(config?: RetryConfig) {
  if (!config || isAuthUrl(config.url)) {
    clearAuth()
    redirectLogin()
    return Promise.reject(new Error('unauthorized'))
  }

  // 已带新 token 重试仍 401
  if (config._retry) {
    if (isPublicUrl(config.url) && !config._retryWithoutAuth) {
      return retryAsGuest(config)
    }
    clearAuth()
    redirectLogin()
    return Promise.reject(new Error('unauthorized'))
  }

  if (getRefreshToken() && !config._retryWithoutAuth) {
    if (refreshing) {
      return enqueueWhileRefreshing(config)
    }
    refreshing = true
    const token = await requestNewAccessToken()
    refreshing = false
    waitQueue.forEach((cb) => cb(token))
    waitQueue = []

    if (token) {
      config._retry = true
      config.headers = config.headers || {}
      config.headers.Authorization = 'Bearer ' + token
      return http.request(config)
    }
    // 刷新失败
    if (isPublicUrl(config.url)) {
      return retryAsGuest(config)
    }
    clearAuth()
    redirectLogin()
    return Promise.reject(new Error('unauthorized'))
  }

  if (isPublicUrl(config.url) && !config._retryWithoutAuth) {
    return retryAsGuest(config)
  }

  clearAuth()
  redirectLogin()
  return Promise.reject(new Error('unauthorized'))
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
        return handle401(res.config as RetryConfig)
      }
      Message.error(body.msg || '请求失败')
      return Promise.reject(body)
    }
    return body.data
  },
  async (err: AxiosError<any>) => {
    const status = err.response?.status
    const cfg = err.config as RetryConfig | undefined
    const msg = err.response?.data?.msg || err.message || '网络异常'

    if (status === 401) {
      return handle401(cfg)
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
