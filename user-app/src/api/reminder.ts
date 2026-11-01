import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  ReminderRule,
  ReminderMessage,
  ReminderExecutionSummary
} from '../types/reminder'

// ==================== 提醒规则 ====================

/**
 * 分页查询提醒规则列表
 */
export const getReminderRulesApi = (params?: {
  triggerType?: string
  status?: number
  keyword?: string
  current?: number
  size?: number
}): Promise<ApiResponse<PageResult<ReminderRule>>> => {
  return request<PageResult<ReminderRule>>({
    url: '/reminders/rules',
    method: 'GET',
    data: params
  })
}

/**
 * 查询提醒规则详情
 */
export const getReminderRuleDetailApi = (id: number): Promise<ApiResponse<ReminderRule>> => {
  return request<ReminderRule>({
    url: `/reminders/rules/${id}`,
    method: 'GET'
  })
}

/**
 * 新增提醒规则
 */
export const createReminderRuleApi = (data: {
  ruleName: string
  triggerType: string
  businessRefId?: number
  remindAt?: string
  advanceMinutes?: number
  repeatRule?: string
  startDate?: string
  endDate?: string
  status?: number
}): Promise<ApiResponse<ReminderRule>> => {
  return request<ReminderRule>({
    url: '/reminders/rules',
    method: 'POST',
    data
  })
}

/**
 * 更新提醒规则
 */
export const updateReminderRuleApi = (id: number, data: {
  ruleName: string
  triggerType: string
  businessRefId?: number
  remindAt?: string
  advanceMinutes?: number
  repeatRule?: string
  startDate?: string
  endDate?: string
  status?: number
}): Promise<ApiResponse<ReminderRule>> => {
  return request<ReminderRule>({
    url: `/reminders/rules/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除提醒规则
 */
export const deleteReminderRuleApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/reminders/rules/${id}`,
    method: 'DELETE'
  })
}

/**
 * 启用 / 停用切换
 */
export const toggleReminderRuleStatusApi = (id: number): Promise<ApiResponse<ReminderRule>> => {
  return request<ReminderRule>({
    url: `/reminders/rules/${id}/status`,
    method: 'PUT'
  })
}

// ==================== 站内提醒消息 ====================

/**
 * 分页查询站内消息列表
 */
export const getReminderMessagesApi = (params?: {
  status?: string
  messageType?: string
  keyword?: string
  current?: number
  size?: number
}): Promise<ApiResponse<PageResult<ReminderMessage>>> => {
  return request<PageResult<ReminderMessage>>({
    url: '/reminders/messages',
    method: 'GET',
    data: params
  })
}

/**
 * 获取未读消息数量
 */
export const getUnreadCountApi = (): Promise<ApiResponse<number>> => {
  return request<number>({
    url: '/reminders/messages/unread-count',
    method: 'GET'
  })
}

/**
 * 获取提醒与消息概览统计
 */
export const getReminderSummaryApi = (): Promise<ApiResponse<ReminderExecutionSummary>> => {
  return request<ReminderExecutionSummary>({
    url: '/reminders/messages/summary',
    method: 'GET'
  })
}

/**
 * 获取单条消息详情（自动标记为已读）
 */
export const getReminderMessageDetailApi = (id: number): Promise<ApiResponse<ReminderMessage>> => {
  return request<ReminderMessage>({
    url: `/reminders/messages/${id}`,
    method: 'GET'
  })
}

/**
 * 单条标为已读
 */
export const markAsReadApi = (id: number): Promise<ApiResponse<ReminderMessage>> => {
  return request<ReminderMessage>({
    url: `/reminders/messages/${id}/read`,
    method: 'PUT'
  })
}

/**
 * 一键全部标为已读
 */
export const markAllAsReadApi = (): Promise<ApiResponse<number>> => {
  return request<number>({
    url: '/reminders/messages/read-all',
    method: 'PUT'
  })
}

/**
 * 删除单条消息
 */
export const deleteReminderMessageApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/reminders/messages/${id}`,
    method: 'DELETE'
  })
}

/**
 * 手动触发扫描提醒规则（即时调试或轮询同步）
 */
export const triggerReminderScanApi = (): Promise<ApiResponse<number>> => {
  return request<number>({
    url: '/reminders/messages/trigger-scan',
    method: 'POST'
  })
}

