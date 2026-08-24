import request from '@/config/axios'

export type LoginLogVO = {
  id: number
  logType: number
  userId: number
  username: string
  result: number
  userIp: string
  userAgent: string
  createTime: Date
}

// 查询登录日志列表
export const getLoginLogPage = (params: PageParam) => {
  return request.get({ url: '/system/login-log/page', params })
}

// 导出登录日志
export const exportLoginLog = (params: any) => {
  return request.download({ url: '/system/login-log/export-excel', params })
}
