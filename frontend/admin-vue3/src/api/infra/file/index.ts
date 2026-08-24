import request from '@/config/axios'

export interface FileVO {
  id: number
  name: string
  path: string
  url: string
  type: string
  size: number
  createTime: Date
}

export interface FilePageReqVO extends PageParam {
  name?: string
  type?: string
  createTime?: string[]
}

// 查询文件列表
export const getFilePage = (params: FilePageReqVO) => {
  return request.get({ url: '/infra/file/page', params })
}

// 删除文件
export const deleteFile = (id: number) => {
  return request.delete({ url: '/infra/file/delete?id=' + id })
}

// 批量删除文件
export const deleteFileList = (ids: number[]) => {
  return request.delete({ url: '/infra/file/delete-list', params: { ids: ids.join(',') } })
}

// 上传文件：directory 为可选的子目录，返回 { url }
export const uploadFile = (data: any, onUploadProgress?: Function) => {
  return request.upload({ url: '/infra/file/upload', data, onUploadProgress })
}
