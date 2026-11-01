import { request } from '../utils/request'
import type { ApiResponse } from '../types/api'
import type { AuthTokenVO, LoginDTO, RegisterDTO, UserVO } from '../types/user'

/**
 * 用户登录
 */
export const loginApi = (data: LoginDTO): Promise<ApiResponse<AuthTokenVO>> => {
  return request<AuthTokenVO>({
    url: '/auth/login',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在登录...'
  })
}

/**
 * 用户注册
 */
export const registerApi = (data: RegisterDTO): Promise<ApiResponse<UserVO>> => {
  return request<UserVO>({
    url: '/auth/register',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在注册...'
  })
}

/**
 * 退出登录
 */
export const logoutApi = (): Promise<ApiResponse<void>> => {
  return request<void>({
    url: '/auth/logout',
    method: 'POST'
  })
}

/**
 * 查询当前已登录用户信息
 */
export const getMyInfoApi = (): Promise<ApiResponse<UserVO>> => {
  return request<UserVO>({
    url: '/auth/me',
    method: 'GET'
  })
}

