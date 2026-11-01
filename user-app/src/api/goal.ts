import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  CreateGoalDto,
  CreateGoalTaskDto,
  GoalItem,
  GoalQueryParams,
  GoalSummary,
  GoalTaskItem,
  UpdateGoalDto,
  UpdateGoalTaskDto
} from '../types/goal'

/**
 * 分页查询目标列表
 */
export const getGoalListApi = (params?: GoalQueryParams): Promise<ApiResponse<PageResult<GoalItem>>> => {
  return request<PageResult<GoalItem>>({
    url: '/goals',
    method: 'GET',
    data: params
  })
}

/**
 * 获取目标统计概要
 */
export const getGoalSummaryApi = (): Promise<ApiResponse<GoalSummary>> => {
  return request<GoalSummary>({
    url: '/goals/summary',
    method: 'GET'
  })
}

/**
 * 获取指定目标详情（包含子任务列表）
 */
export const getGoalDetailApi = (id: number): Promise<ApiResponse<GoalItem>> => {
  return request<GoalItem>({
    url: `/goals/${id}`,
    method: 'GET'
  })
}

/**
 * 创建新目标
 */
export const createGoalApi = (data: CreateGoalDto): Promise<ApiResponse<GoalItem>> => {
  return request<GoalItem>({
    url: '/goals',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在创建目标...'
  })
}

/**
 * 更新目标基础信息
 */
export const updateGoalApi = (id: number, data: UpdateGoalDto): Promise<ApiResponse<GoalItem>> => {
  return request<GoalItem>({
    url: `/goals/${id}`,
    method: 'PUT',
    data,
    showLoading: true,
    loadingTitle: '正在保存...'
  })
}

/**
 * 更新目标状态（未开始、进行中、已完成、已归档）
 */
export const updateGoalStatusApi = (id: number, status: string): Promise<ApiResponse<GoalItem>> => {
  return request<GoalItem>({
    url: `/goals/${id}/status`,
    method: 'PATCH',
    data: { status }
  })
}

/**
 * 删除目标
 */
export const deleteGoalApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/goals/${id}`,
    method: 'DELETE',
    showLoading: true,
    loadingTitle: '正在删除...'
  })
}

// ==================== 子任务 API ====================

/**
 * 为指定目标创建子任务
 */
export const createGoalTaskApi = (goalId: number, data: CreateGoalTaskDto): Promise<ApiResponse<GoalTaskItem>> => {
  return request<GoalTaskItem>({
    url: `/goals/${goalId}/tasks`,
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在添加子任务...'
  })
}

/**
 * 获取目标所有子任务
 */
export const getGoalTasksApi = (goalId: number): Promise<ApiResponse<GoalTaskItem[]>> => {
  return request<GoalTaskItem[]>({
    url: `/goals/${goalId}/tasks`,
    method: 'GET'
  })
}

/**
 * 修改目标子任务
 */
export const updateGoalTaskApi = (goalId: number, taskId: number, data: UpdateGoalTaskDto): Promise<ApiResponse<GoalTaskItem>> => {
  return request<GoalTaskItem>({
    url: `/goals/${goalId}/tasks/${taskId}`,
    method: 'PUT',
    data
  })
}

/**
 * 更新子任务完成状态（触发服务端自动重算目标进度）
 */
export const updateGoalTaskStatusApi = (goalId: number, taskId: number, status: string): Promise<ApiResponse<GoalTaskItem>> => {
  return request<GoalTaskItem>({
    url: `/goals/${goalId}/tasks/${taskId}/status`,
    method: 'PATCH',
    data: { status }
  })
}

/**
 * 删除目标子任务
 */
export const deleteGoalTaskApi = (goalId: number, taskId: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/goals/${goalId}/tasks/${taskId}`,
    method: 'DELETE'
  })
}

