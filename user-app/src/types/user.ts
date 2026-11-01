/**
 * 用户信息视图
 */
export interface UserVO {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
  gender?: number
  status: number
  roles: string[]
  lastLoginAt?: string
  createdAt?: string
}

/**
 * 登录认证响应
 */
export interface AuthTokenVO {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserVO
}

/**
 * 登录入参
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * 注册入参
 */
export interface RegisterDTO {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
  gender?: number
}

