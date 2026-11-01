import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  CreateFinanceRecordDTO,
  FinanceCategoryItem,
  FinanceRecordItem,
  FinanceSummaryData,
  FinanceTrendData,
  UpdateFinanceRecordDTO
} from '../types/finance'

/**
 * 查询用户可用分类列表
 */
export const getFinanceCategoriesApi = (categoryType?: string): Promise<ApiResponse<FinanceCategoryItem[]>> => {
  return request<FinanceCategoryItem[]>({
    url: '/finance/categories',
    method: 'GET',
    data: { categoryType }
  })
}

/**
 * 分页多条件查询收支流水明细
 */
export const getFinanceRecordsApi = (params?: {
  recordType?: string
  categoryId?: number
  yearMonth?: string
  startDate?: string
  endDate?: string
  keyword?: string
  pageNo?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<FinanceRecordItem>>> => {
  return request<PageResult<FinanceRecordItem>>({
    url: '/finance/records',
    method: 'GET',
    data: params
  })
}

/**
 * 查询单笔收支详情
 */
export const getFinanceRecordDetailApi = (id: number): Promise<ApiResponse<FinanceRecordItem>> => {
  return request<FinanceRecordItem>({
    url: `/finance/records/${id}`,
    method: 'GET'
  })
}

/**
 * 新增收支明细
 */
export const createFinanceRecordApi = (data: CreateFinanceRecordDTO): Promise<ApiResponse<FinanceRecordItem>> => {
  return request<FinanceRecordItem>({
    url: '/finance/records',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在记账...'
  })
}

/**
 * 编辑收支明细
 */
export const updateFinanceRecordApi = (id: number, data: UpdateFinanceRecordDTO): Promise<ApiResponse<FinanceRecordItem>> => {
  return request<FinanceRecordItem>({
    url: `/finance/records/${id}`,
    method: 'PUT',
    data,
    showLoading: true,
    loadingTitle: '正在保存...'
  })
}

/**
 * 删除收支明细
 */
export const deleteFinanceRecordApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/finance/records/${id}`,
    method: 'DELETE',
    showLoading: true,
    loadingTitle: '正在删除...'
  })
}

/**
 * 查询月度收支汇总（收入、支出、结余及分类占比）
 */
export const getFinanceSummaryApi = (year?: number, month?: number): Promise<ApiResponse<FinanceSummaryData>> => {
  return request<FinanceSummaryData>({
    url: '/finance/summary',
    method: 'GET',
    data: { year, month }
  })
}

/**
 * 查询收支趋势统计
 */
export const getFinanceTrendsApi = (year?: number, months = 6): Promise<ApiResponse<FinanceTrendData>> => {
  return request<FinanceTrendData>({
    url: '/finance/trends',
    method: 'GET',
    data: { year, months }
  })
}

