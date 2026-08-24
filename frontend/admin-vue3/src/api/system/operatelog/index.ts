import request from '@/config/axios'

export type OperateLogVO = {
  id: number
  module: string
  name: string
  userId: number
  username: string
  requestMethod: string
  requestUrl: string
  requestParams: string
  javaMethod: string
  userIp: string
  userAgent: string
  duration: number
  resultCode: number
  resultMsg: string
  createTime: Date
}

// 查询操作日志列表
export const getOperateLogPage = (params: PageParam) => {
  return request.get({ url: '/system/operate-log/page', params })
}
// 导出操作日志
export const exportOperateLog = (params: any) => {
  return request.download({ url: '/system/operate-log/export-excel', params })
}
