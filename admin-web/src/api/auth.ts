import { request } from './request'
import type { ApiResponse } from '../types/api'
import type { AuthTokenVO, LoginDTO, UserVO } from '../types/user'

/**
 * 管理员登录接口
 */
export const loginApi = (data: LoginDTO): Promise<ApiResponse<AuthTokenVO>> => {
  return request<AuthTokenVO>({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

/**
 * 退出登录接口
 */
export const logoutApi = (): Promise<ApiResponse<void>> => {
  return request<void>({
    url: '/auth/logout',
    method: 'POST'
  })
}

/**
 * 获取当前已登录管理员个人信息
 */
export const getMyInfoApi = (): Promise<ApiResponse<UserVO>> => {
  return request<UserVO>({
    url: '/auth/me',
    method: 'GET'
  })
}

