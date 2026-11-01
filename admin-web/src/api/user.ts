import { request } from './request'
import type { ApiResponse, PageResult } from '../types/api'
import type { AdminUserQueryDTO, UserVO } from '../types/user'

/**
 * 分页检索平台用户列表
 */
export const getUserPageApi = (params: AdminUserQueryDTO): Promise<ApiResponse<PageResult<UserVO>>> => {
  return request<PageResult<UserVO>>({
    url: '/admin/users',
    method: 'GET',
    params
  })
}

/**
 * 修改目标用户状态（启用/禁用）
 */
export const updateUserStatusApi = (userId: number, status: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/admin/users/${userId}/status`,
    method: 'PUT',
    data: { status }
  })
}

