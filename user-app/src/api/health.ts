import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  CreateHealthRecordDto,
  HealthDashboardSummary,
  HealthRecordItem,
  HealthRecordQueryParams,
  HealthStandardItem,
  HealthTrendQueryParams,
  HealthTrendResult,
  UpdateHealthRecordDto
} from '../types/health'

/**
 * 获取今日健康看板快照汇总
 */
export const getHealthSummaryApi = (): Promise<ApiResponse<HealthDashboardSummary>> => {
  return request<HealthDashboardSummary>({
    url: '/health/summary',
    method: 'GET'
  })
}

/**
 * 获取健康指标多维趋势数据
 */
export const getHealthTrendsApi = (
  params: HealthTrendQueryParams
): Promise<ApiResponse<HealthTrendResult>> => {
  return request<HealthTrendResult>({
    url: '/health/trends',
    method: 'GET',
    data: params
  })
}

/**
 * 分页查询健康记录列表
 */
export const getHealthRecordsApi = (
  params?: HealthRecordQueryParams
): Promise<ApiResponse<PageResult<HealthRecordItem>>> => {
  return request<PageResult<HealthRecordItem>>({
    url: '/health/records',
    method: 'GET',
    data: params
  })
}

/**
 * 录入新的健康记录
 */
export const createHealthRecordApi = (
  data: CreateHealthRecordDto
): Promise<ApiResponse<HealthRecordItem>> => {
  return request<HealthRecordItem>({
    url: '/health/records',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在保存...'
  })
}

/**
 * 修改健康记录
 */
export const updateHealthRecordApi = (
  id: number,
  data: UpdateHealthRecordDto
): Promise<ApiResponse<HealthRecordItem>> => {
  return request<HealthRecordItem>({
    url: `/health/records/${id}`,
    method: 'PUT',
    data,
    showLoading: true,
    loadingTitle: '正在更新...'
  })
}

/**
 * 删除健康记录
 */
export const deleteHealthRecordApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/health/records/${id}`,
    method: 'DELETE',
    showLoading: true,
    loadingTitle: '正在删除...'
  })
}

/**
 * 获取生效中的健康生活参考标准列表
 */
export const getHealthStandardsApi = (): Promise<ApiResponse<HealthStandardItem[]>> => {
  return request<HealthStandardItem[]>({
    url: '/health/standards',
    method: 'GET'
  })
}

