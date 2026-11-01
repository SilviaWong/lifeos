import { request } from '../utils/request'
import type { ApiResponse } from '../types/api'
import type { HomeSummary } from '../types/home'

/**
 * 获取首页综合看板摘要数据
 */
export const getHomeSummaryApi = (): Promise<ApiResponse<HomeSummary>> => {
  return request<HomeSummary>({
    url: '/home/summary',
    method: 'GET'
  })
}

