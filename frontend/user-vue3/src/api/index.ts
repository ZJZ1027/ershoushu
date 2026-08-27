import http from './http'

export const login = (data: { username: string; password: string }) => http.post('/auth/login', data)
export const register = (data: any) => http.post('/auth/register', data)
export const logout = () => http.post('/auth/logout')
export const profile = () => http.get('/auth/profile')
export const updateProfile = (data: any) => http.put('/auth/profile', data)

export const getCategories = () => http.get('/category/list')
export const getBookPage = (params: any) => http.get('/book/page', { params })
export const getBook = (id: number) => http.get('/book/get', { params: { id } })
export const getMyBooks = (params: any) => http.get('/book/mine', { params })
export const getFavorites = (params: any) => http.get('/book/favorites', { params })
export const publishBook = (data: any) => http.post('/book/publish', data)
export const updateBook = (id: number, data: any) => http.put('/book/update', data, { params: { id } })
export const offShelf = (id: number) => http.put('/book/offshelf', null, { params: { id } })
export const toggleFavorite = (bookId: number) => http.post('/favorite/toggle', null, { params: { bookId } })

export const wantBook = (data: any) => http.post('/order/want', data)
export const getOrders = (params: any) => http.get('/order/page', { params })
export const getPendingOrderCount = () => http.get('/order/pending-count')
export const agreeOrder = (id: number) => http.put('/order/agree', null, { params: { id } })
export const completeOrder = (id: number) => http.put('/order/complete', null, { params: { id } })
export const cancelOrder = (id: number, reason?: string) =>
  http.put('/order/cancel', null, { params: { id, reason } })

export const getInquiries = (params: any) => http.get('/inquiry/page', { params })
export const getUnreadInquiryCount = () => http.get('/inquiry/unread-count')
export const getMessages = (inquiryId: number) => http.get('/inquiry/messages', { params: { inquiryId } })
export const sendInquiry = (data: any) => http.post('/inquiry/send', data)
export const openInquiry = (bookId: number) =>
  http.post('/inquiry/open', null, { params: { bookId } })
export const replyInquiry = (inquiryId: number, content: string) =>
  http.post('/inquiry/reply', { content }, { params: { inquiryId } })
export const recallInquiryMsg = (msgId: number) =>
  http.put('/inquiry/recall', null, { params: { msgId } })

export const createReport = (data: any) => http.post('/report/create', data)
export const getNotices = () => http.get('/notice/list')
export const getDict = (type: string) => http.get('/dict/data', { params: { type } })

export const uploadFile = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/file/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
