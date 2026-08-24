import request from '@/config/axios'

export interface ConfigVO {
  id: number | undefined
  category: string
  name: string
  /** 与后端字段同名：key / value 是 MySQL 保留字，列名与字段名统一带 config 前缀 */
  configKey: string
  configValue: string
  type: number
  visible: boolean
  remark: string
  createTime: Date
}

export interface ConfigPageReqVO extends PageParam {
  name?: string
  configKey?: string
  type?: number
  createTime?: string[]
}

// 查询参数列表
export const getConfigPage = (params: ConfigPageReqVO) => {
  return request.get({ url: '/infra/config/page', params })
}

// 查询参数详情
export const getConfig = (id: number) => {
  return request.get({ url: '/infra/config/get?id=' + id })
}

// 根据参数键名查询参数值
export const getConfigKey = (configKey: string) => {
  return request.get({ url: '/infra/config/get-value-by-key?configKey=' + configKey })
}

// 新增参数
export const createConfig = (data: ConfigVO) => {
  return request.post({ url: '/infra/config/create', data })
}

// 修改参数
export const updateConfig = (data: ConfigVO) => {
  return request.put({ url: '/infra/config/update', data })
}

// 删除参数
export const deleteConfig = (id: number) => {
  return request.delete({ url: '/infra/config/delete?id=' + id })
}

// 批量删除参数
export const deleteConfigList = (ids: number[]) => {
  return request.delete({ url: '/infra/config/delete-list', params: { ids: ids.join(',') } })
}

// 导出参数
export const exportConfig = (params: ConfigPageReqVO) => {
  return request.download({ url: '/infra/config/export-excel', params })
}
