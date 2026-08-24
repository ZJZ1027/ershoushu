import axios, { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

import { Message, Modal, Notification } from '@arco-design/web-vue'
import qs from 'qs'
import { config } from '@/config/axios/config'
import { getAccessToken, getRefreshToken, getTenantId, removeToken, setToken } from '@/utils/auth'
import errorCode from './errorCode'

import { resetRouter } from '@/router'
import { deleteUserCache } from '@/hooks/web/useCache'

const tenantEnable = import.meta.env.VITE_APP_TENANT_ENABLE
const { result_code, base_url, request_timeout } = config

// 是否显示重新登录
export const isRelogin = { show: false }
// Axios 无感知刷新令牌：401 时先用刷新令牌换新的访问令牌，期间到来的请求排进队列，换到后统一重放
// 请求队列
let requestList: any[] = []
// 是否正在刷新中
let isRefreshToken = false
// 请求白名单，无须 token 的接口
const whiteList: string[] = ['/login', '/refresh-token']

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: base_url, // api 的 base_url
  timeout: request_timeout, // 请求超时时间
  withCredentials: false, // 禁用 Cookie 等信息
  // 自定义参数序列化函数
  paramsSerializer: (params) => {
    return qs.stringify(params, { allowDots: true })
  }
})

// request拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 是否需要设置 token
    let isToken = (config!.headers || {}).isToken === false
    whiteList.some((v) => {
      if (config.url && config.url.indexOf(v) > -1) {
        return (isToken = false)
      }
    })
    if (getAccessToken() && !isToken) {
      config.headers.Authorization = 'Bearer ' + getAccessToken() // 让每个请求携带自定义token
    }
    // 设置租户
    if (tenantEnable && tenantEnable === 'true') {
      const tenantId = getTenantId()
      if (tenantId) config.headers['tenant-id'] = tenantId
    }
    const method = config.method?.toUpperCase()
    // 防止 GET 请求缓存
    if (method === 'GET') {
      config.headers['Cache-Control'] = 'no-cache'
      config.headers['Pragma'] = 'no-cache'
    }
    // 自定义参数序列化函数
    else if (method === 'POST') {
      const contentType = config.headers['Content-Type'] || config.headers['content-type']
      if (contentType === 'application/x-www-form-urlencoded') {
        if (config.data && typeof config.data !== 'string') {
          config.data = qs.stringify(config.data)
        }
      }
    }
    return config
  },
  (error: AxiosError) => {
    // Do something with request error
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  async (response: AxiosResponse<any>) => {
    let { data } = response
    if (!data) {
      // 返回“[HTTP]请求没有返回值”;
      throw new Error()
    }

    const { t } = useI18n()
    // 未设置状态码则默认成功状态
    // 二进制数据则直接返回，例如说 Excel 导出
    if (
      response.request.responseType === 'blob' ||
      response.request.responseType === 'arraybuffer'
    ) {
      // 注意：如果导出的响应为 json，说明可能失败了，不直接返回进行下载
      if (response.data.type !== 'application/json') {
        return response.data
      }
      data = await new Response(response.data).json()
    }
    const code = data.code || result_code
    // 获取错误信息
    const msg = data.msg || errorCode[code] || errorCode['default']
    if (code === 500) {
      Message.error(t('sys.api.errMsg500'))
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      // 业务异常：后端用 HTTP 200 + code != 200 表达（鉴权类异常走 HTTP 状态码，见下方 onRejected）
      // Arco NotificationConfig 要求 content：错误文案挪到正文，标题用统一的「错误提示」
      Notification.error({ title: t('sys.api.errorTip'), content: msg })
      return Promise.reject('error')
    } else {
      return data
    }
  },
  async (error: AxiosError<any>) => {
    const { t } = useI18n()
    const status = error.response?.status
    const config = error.config as InternalAxiosRequestConfig
    // 401 未认证：访问令牌过期或失效。先尝试用刷新令牌换新的访问令牌，再重放原请求
    if (status === 401 && config) {
      return handleUnauthorized(config)
    }
    // 403 无权限：账号已登录，但缺少该操作的权限标识，重试也没用，直接提示
    if (status === 403) {
      Notification.error({
        title: t('sys.api.errorTip'),
        content: error.response?.data?.msg || t('sys.api.errMsg403')
      })
      return Promise.reject('error')
    }
    console.log('err' + error) // for debug
    let { message } = error
    if (message === 'Network Error') {
      message = t('sys.api.errorMessage')
    } else if (message.includes('timeout')) {
      message = t('sys.api.apiTimeoutMessage')
    } else if (message.includes('Request failed with status code')) {
      message = t('sys.api.apiRequestFailed') + message.substr(message.length - 3)
    }
    Message.error(error.response?.data?.msg || message)
    return Promise.reject(error)
  }
)

/** 401 处理：单飞刷新令牌 + 请求重放 */
const handleUnauthorized = async (config: InternalAxiosRequestConfig) => {
  // 刷新令牌本身 401，说明refreshToken也失效了，只能重新登录（否则会递归刷新）
  if (config.url?.includes('/refresh-token')) {
    return handleAuthorized()
  }
  // 已有刷新在进行中：排队等新令牌，拿到后重放
  if (isRefreshToken) {
    return new Promise((resolve) => {
      requestList.push(() => {
        config.headers!.Authorization = 'Bearer ' + getAccessToken()
        resolve(service(config))
      })
    })
  }
  // 没有刷新令牌，只能重新登录
  if (!getRefreshToken()) {
    return handleAuthorized()
  }
  isRefreshToken = true
  try {
    const refreshTokenRes = await refreshToken()
    setToken(refreshTokenRes.data.data)
    config.headers!.Authorization = 'Bearer ' + getAccessToken()
    requestList.forEach((cb: any) => cb())
    return service(config)
  } catch {
    // 刷新失败：回放队列（让它们各自失败结束），当前请求不重放，避免递归
    requestList.forEach((cb: any) => cb())
    return handleAuthorized()
  } finally {
    requestList = []
    isRefreshToken = false
  }
}

const refreshToken = async () => {
  axios.defaults.headers.common['tenant-id'] = getTenantId()
  return await axios.post(base_url + '/system/auth/refresh-token?refreshToken=' + getRefreshToken())
}
const handleAuthorized = () => {
  const { t } = useI18n()
  if (!isRelogin.show) {
    // 如果已经到登录页面则不进行弹窗提示
    if (window.location.href.includes('login')) {
      return Promise.reject(t('sys.api.timeoutMessage'))
    }
    isRelogin.show = true
    // 仅「重新登录」单按钮：隐藏取消与关闭按钮，点遮罩、按 ESC 都不关闭，避免用户绕过重新登录
    Modal.warning({
      title: t('common.confirmTitle'),
      content: t('sys.api.timeoutMessage'),
      okText: t('login.relogin'),
      hideCancel: true,
      closable: false,
      maskClosable: false,
      escToClose: false,
      onOk: () => {
        resetRouter() // 重置静态路由表
        deleteUserCache() // 删除用户缓存
        removeToken()
        isRelogin.show = false
        // 干掉token后再走一次路由让它过router.beforeEach的校验
        window.location.href = window.location.href
      }
    })
  }
  return Promise.reject(t('sys.api.timeoutMessage'))
}
export { service }
