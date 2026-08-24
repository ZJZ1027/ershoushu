import request from '@/config/axios'

export const getDashboard = async () => {
  return await request.get({ url: '/business/dashboard/get' })
}

export const getAdminBadges = async (memberSince?: number) => {
  return await request.get({ url: '/business/dashboard/badges', params: { memberSince } })
}

export const getCategoryPage = async (params: PageParam) => {
  return await request.get({ url: '/business/category/page', params })
}

export const getCategory = async (id: number) => {
  return await request.get({ url: '/business/category/get?id=' + id })
}

export const createCategory = async (data: any) => {
  return await request.post({ url: '/business/category/create', data })
}

export const updateCategory = async (data: any) => {
  return await request.put({ url: '/business/category/update', data })
}

export const deleteCategory = async (id: number) => {
  return await request.delete({ url: '/business/category/delete?id=' + id })
}

export const getBookPage = async (params: PageParam) => {
  return await request.get({ url: '/business/book/page', params })
}

export const getBook = async (id: number) => {
  return await request.get({ url: '/business/book/get?id=' + id })
}

export const auditBook = async (data: { id: number; pass: boolean; rejectReason?: string }) => {
  return await request.put({ url: '/business/book/audit', data })
}

export const offShelfBook = async (id: number) => {
  return await request.put({ url: '/business/book/offshelf?id=' + id })
}

export const getOrderPage = async (params: PageParam) => {
  return await request.get({ url: '/business/order/page', params })
}

export const closeOrder = async (id: number, reason?: string) => {
  return await request.put({ url: '/business/order/close', params: { id, reason } })
}

export const getInquiryPage = async (params: PageParam) => {
  return await request.get({ url: '/business/inquiry/page', params })
}

export const getInquiryMessages = async (inquiryId: number) => {
  return await request.get({ url: '/business/inquiry/messages', params: { inquiryId } })
}

export const getMemberPage = async (params: PageParam) => {
  return await request.get({ url: '/business/member/page', params })
}

export const updateMemberStatus = async (data: { id: number; status: number }) => {
  return await request.put({ url: '/business/member/update-status', data })
}

export const getReportPage = async (params: PageParam) => {
  return await request.get({ url: '/business/report/page', params })
}

export const handleReport = async (id: number, pass: boolean, remark?: string) => {
  return await request.put({ url: '/business/report/handle', params: { id, pass, remark } })
}
