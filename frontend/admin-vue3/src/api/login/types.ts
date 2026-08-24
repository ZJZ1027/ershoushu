export type UserLoginVO = {
  tenantName?: string
  username: string
  password: string
}

export type TokenType = {
  accessToken: string // 访问令牌
  refreshToken: string // 刷新令牌
  userId: number // 用户编号
  /** 登录/刷新令牌时由后端返回，用于同步请求头 tenant-id */
  tenantId?: number
  expiresTime: number // 访问令牌过期时间
}

export type UserVO = {
  id: number
  username: string
  nickname: string
  deptId: number
  email: string
  mobile: string
  sex: number
  avatar: string
  loginIp: string
  loginDate: string
}
