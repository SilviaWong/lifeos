import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  CreateTodoDto,
  TodoItem,
  TodoQueryParams,
  TodoSummary,
  UpdateTodoDto
} from '../types/todo'

/**
 * 分页查询待办列表
 */
export const getTodoListApi = (params?: TodoQueryParams): Promise<ApiResponse<PageResult<TodoItem>>> => {
  return request<PageResult<TodoItem>>({
    url: '/todos',
    method: 'GET',
    data: params
  })
}

/**
 * 获取待办任务详情
 */
export const getTodoDetailApi = (id: number): Promise<ApiResponse<TodoItem>> => {
  return request<TodoItem>({
    url: `/todos/${id}`,
    method: 'GET'
  })
}

/**
 * 创建新待办任务
 */
export const createTodoApi = (data: CreateTodoDto): Promise<ApiResponse<TodoItem>> => {
  return request<TodoItem>({
    url: '/todos',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在保存...'
  })
}

/**
 * 修改待办任务
 */
export const updateTodoApi = (id: number, data: UpdateTodoDto): Promise<ApiResponse<TodoItem>> => {
  return request<TodoItem>({
    url: `/todos/${id}`,
    method: 'PUT',
    data,
    showLoading: true,
    loadingTitle: '正在更新...'
  })
}

/**
 * 更新待办状态（完成、取消、重新打开）
 */
export const updateTodoStatusApi = (id: number, status: string): Promise<ApiResponse<TodoItem>> => {
  return request<TodoItem>({
    url: `/todos/${id}/status`,
    method: 'PATCH',
    data: { status }
  })
}

/**
 * 删除待办任务（软删除）
 */
export const deleteTodoApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/todos/${id}`,
    method: 'DELETE',
    showLoading: true,
    loadingTitle: '正在删除...'
  })
}

/**
 * 获取用户待办专项统计摘要
 */
export const getTodoSummaryApi = (): Promise<ApiResponse<TodoSummary>> => {
  return request<TodoSummary>({
    url: '/todos/summary',
    method: 'GET'
  })
}

