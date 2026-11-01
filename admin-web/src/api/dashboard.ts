import { request } from './request'
import type { ApiResponse } from '../types/api'
import type { AdminDashboardOverview, AdminModuleTrend, DashboardQueryParam } from '../types/dashboard'

/**
 * 获取管理端综合数据看板概览指标与聚合数据
 */
export const getDashboardOverviewApi = (params?: DashboardQueryParam): Promise<ApiResponse<AdminDashboardOverview>> => {
  return request<AdminDashboardOverview>({
    url: '/admin/dashboard/overview',
    method: 'GET',
    params
  })
}

/**
 * 获取各业务模块每日使用量趋势（堆叠时间序列）
 */
export const getModuleTrendsApi = (params?: DashboardQueryParam): Promise<ApiResponse<AdminModuleTrend>> => {
  return request<AdminModuleTrend>({
    url: '/admin/dashboard/module-trends',
    method: 'GET',
    params
  })
}

