import { request } from './request'
import type { ApiResponse } from '../types/api'

export interface FinanceCategory {
  id?: number
  categoryName: string
  categoryType: 'INCOME' | 'EXPENSE'
  icon?: string
  sortOrder?: number
  isSystem?: number
  userId?: number
  createdAt?: string
}

// 查询系统收支分类列表
export function getFinanceCategories(categoryType?: string): Promise<ApiResponse<FinanceCategory[]>> {
  return request<FinanceCategory[]>({
    url: '/admin/finance-categories',
    method: 'GET',
    params: { categoryType }
  })
}

// 创建系统收支分类
export function createFinanceCategory(data: Partial<FinanceCategory>): Promise<ApiResponse<FinanceCategory>> {
  return request<FinanceCategory>({
    url: '/admin/finance-categories',
    method: 'POST',
    data
  })
}

// 修改系统收支分类
export function updateFinanceCategory(id: number, data: Partial<FinanceCategory>): Promise<ApiResponse<FinanceCategory>> {
  return request<FinanceCategory>({
    url: `/admin/finance-categories/${id}`,
    method: 'PUT',
    data
  })
}

// 删除系统收支分类
export function deleteFinanceCategory(id: number): Promise<ApiResponse<void>> {
  return request<void>({
    url: `/admin/finance-categories/${id}`,
    method: 'DELETE'
  })
}
