import { request } from './request'
import type { ApiResponse, PageResult } from '../types/api'
import type { ModuleUsageLogItem, ModuleUsageLogQuery } from '../types/operations'
import type { ModuleUsageRank, DashboardQueryParam } from '../types/dashboard'

/**
 * 分页查询模块操作使用审计日志
 */
export const getModuleUsageLogsApi = (params: ModuleUsageLogQuery): Promise<ApiResponse<PageResult<ModuleUsageLogItem>>> => {
  return request<PageResult<ModuleUsageLogItem>>({
    url: '/admin/operations/usage-logs',
    method: 'GET',
    params
  })
}

/**
 * 获取模块使用量与独立活跃用户数排行
 */
export const getModuleRankingApi = (params?: DashboardQueryParam): Promise<ApiResponse<ModuleUsageRank[]>> => {
  return request<ModuleUsageRank[]>({
    url: '/admin/operations/module-ranking',
    method: 'GET',
    params
  })
}

