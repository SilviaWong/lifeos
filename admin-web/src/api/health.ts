import { request } from './request'
import type { ApiResponse, PageResult } from '../types/api'

export interface HealthStandard {
  id?: number
  metricCode: string
  metricName: string
  unit: string
  minValue?: number
  maxValue?: number
  applicableScope: string
  description?: string
  status: number
  createdAt?: string
}

export interface HealthRecord {
  id: number
  userId: number
  metricCode: string
  metricName: string
  metricValue: number
  unit: string
  recordTime: string
  note?: string
  source: string
  standardStatus: string
  standardHint: string
  createdAt: string
}

// 查询健康标准列表
export function getHealthStandards(params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<HealthStandard>>> {
  return request<PageResult<HealthStandard>>({
    url: '/admin/health-standards',
    method: 'GET',
    params
  })
}

// 创建健康标准
export function createHealthStandard(data: Partial<HealthStandard>): Promise<ApiResponse<HealthStandard>> {
  return request<HealthStandard>({
    url: '/admin/health-standards',
    method: 'POST',
    data
  })
}

// 更新健康标准
export function updateHealthStandard(id: number, data: Partial<HealthStandard>): Promise<ApiResponse<HealthStandard>> {
  return request<HealthStandard>({
    url: `/admin/health-standards/${id}`,
    method: 'PUT',
    data
  })
}

// 删除健康标准
export function deleteHealthStandard(id: number): Promise<ApiResponse<void>> {
  return request<void>({
    url: `/admin/health-standards/${id}`,
    method: 'DELETE'
  })
}

// 查询健康记录
export function getHealthRecords(params?: {
  pageNum?: number
  pageSize?: number
  metricCode?: string
  startDate?: string
  endDate?: string
}): Promise<ApiResponse<PageResult<HealthRecord>>> {
  return request<PageResult<HealthRecord>>({
    url: '/health/records',
    method: 'GET',
    params
  })
}

// 删除健康记录
export function deleteHealthRecord(id: number): Promise<ApiResponse<void>> {
  return request<void>({
    url: `/health/records/${id}`,
    method: 'DELETE'
  })
}
