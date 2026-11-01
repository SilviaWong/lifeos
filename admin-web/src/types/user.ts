/**
 * 用户基础信息视图
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
 * 登录参数
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * 管理端用户列表查询参数
 */
export interface AdminUserQueryDTO {
  pageNum?: number
  pageSize?: number
  username?: string
  phone?: string
  status?: number
  roleCode?: string
}

