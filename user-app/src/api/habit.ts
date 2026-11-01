import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  CheckinDTO,
  CreateHabitDTO,
  HabitCalendarData,
  HabitCheckinItem,
  HabitItem,
  HabitSummaryData,
  UpdateHabitDTO
} from '../types/habit'

/**
 * 查询习惯列表
 */
export const getHabitListApi = (params?: {
  status?: string
  frequency?: string
  keyword?: string
}): Promise<ApiResponse<HabitItem[]>> => {
  return request<HabitItem[]>({
    url: '/habits',
    method: 'GET',
    data: params
  })
}

/**
 * 获取习惯总览统计
 */
export const getHabitSummaryApi = (): Promise<ApiResponse<HabitSummaryData>> => {
  return request<HabitSummaryData>({
    url: '/habits/summary',
    method: 'GET'
  })
}

/**
 * 获取习惯详情
 */
export const getHabitDetailApi = (id: number): Promise<ApiResponse<HabitItem>> => {
  return request<HabitItem>({
    url: `/habits/${id}`,
    method: 'GET'
  })
}

/**
 * 创建新习惯
 */
export const createHabitApi = (data: CreateHabitDTO): Promise<ApiResponse<HabitItem>> => {
  return request<HabitItem>({
    url: '/habits',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在创建习惯...'
  })
}

/**
 * 修改习惯
 */
export const updateHabitApi = (id: number, data: UpdateHabitDTO): Promise<ApiResponse<HabitItem>> => {
  return request<HabitItem>({
    url: `/habits/${id}`,
    method: 'PUT',
    data,
    showLoading: true,
    loadingTitle: '正在保存修改...'
  })
}

/**
 * 修改习惯状态（ACTIVE, PAUSED, ARCHIVED）
 */
export const updateHabitStatusApi = (id: number, status: string): Promise<ApiResponse<HabitItem>> => {
  return request<HabitItem>({
    url: `/habits/${id}/status`,
    method: 'PUT',
    data: { status }
  })
}

/**
 * 删除习惯
 */
export const deleteHabitApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/habits/${id}`,
    method: 'DELETE',
    showLoading: true,
    loadingTitle: '正在删除...'
  })
}

/**
 * 习惯打卡
 */
export const checkinHabitApi = (id: number, data?: CheckinDTO): Promise<ApiResponse<HabitItem>> => {
  return request<HabitItem>({
    url: `/habits/${id}/checkins`,
    method: 'POST',
    data: data || {},
    showLoading: true,
    loadingTitle: '正在打卡...'
  })
}

/**
 * 取消打卡
 */
export const cancelCheckinApi = (id: number, date?: string): Promise<ApiResponse<HabitItem>> => {
  const url = date ? `/habits/${id}/checkins/${date}` : `/habits/${id}/checkins`
  return request<HabitItem>({
    url,
    method: 'DELETE',
    showLoading: true,
    loadingTitle: '正在取消打卡...'
  })
}

/**
 * 获取习惯月历打卡数据
 */
export const getHabitCalendarApi = (
  id: number,
  year?: number,
  month?: number
): Promise<ApiResponse<HabitCalendarData>> => {
  return request<HabitCalendarData>({
    url: `/habits/${id}/calendar`,
    method: 'GET',
    data: { year, month }
  })
}

/**
 * 分页查询打卡记录明细
 */
export const getHabitCheckinRecordsApi = (
  id: number,
  pageNo = 1,
  pageSize = 20
): Promise<ApiResponse<PageResult<HabitCheckinItem>>> => {
  return request<PageResult<HabitCheckinItem>>({
    url: `/habits/${id}/checkins`,
    method: 'GET',
    data: { pageNo, pageSize }
  })
}

