import request from '@/config/axios'
import type { UserLoginVO } from './types'

// 登录
export const login = (data: UserLoginVO) => {
  return request.post({ url: '/system/auth/login', data })
}

// 安全：不提供公开的「租户名 -> 租户编号」接口，避免登录前的未授权接口形成租户名枚举面。
// 用户手动输入租户名，随登录请求体交由后端内部解析；登录成功后由响应 tenantId 同步请求头。

// 登出
export const loginOut = () => {
  return request.post({ url: '/system/auth/logout' })
}

// 获取登录用户的权限信息（用户信息 + 角色 + 权限 + 菜单）
export const getInfo = () => {
  return request.get({ url: '/system/auth/get-permission-info' })
}
