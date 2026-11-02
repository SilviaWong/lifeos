import { request } from './request'
import type { ApiResponse, PageResult } from '../types/api'

export interface ReminderRule {
  id?: number
  ruleName: string
  ruleType: string
  triggerCondition: string
  conditionValue: number
  monitorWindowDays: number
  contentTemplate: string
  enabled: boolean
  triggerTime?: string
  createdAt?: string
}

// 分页查询规则列表
export function getReminderRules(params?: {
  pageNum?: number
  pageSize?: number
  ruleType?: string
  enabled?: boolean
}): Promise<ApiResponse<PageResult<ReminderRule>>> {
  return request<PageResult<ReminderRule>>({
    url: '/reminders/rules',
    method: 'GET',
    params
  })
}

// 新增规则
export function createReminderRule(data: Partial<ReminderRule>): Promise<ApiResponse<ReminderRule>> {
  return request<ReminderRule>({
    url: '/reminders/rules',
    method: 'POST',
    data
  })
}

// 修改规则
export function updateReminderRule(id: number, data: Partial<ReminderRule>): Promise<ApiResponse<ReminderRule>> {
  return request<ReminderRule>({
    url: `/reminders/rules/${id}`,
    method: 'PUT',
    data
  })
}

// 切换规则状态
export function toggleReminderRuleStatus(id: number): Promise<ApiResponse<ReminderRule>> {
  return request<ReminderRule>({
    url: `/reminders/rules/${id}/status`,
    method: 'PUT'
  })
}

// 删除规则
export function deleteReminderRule(id: number): Promise<ApiResponse<void>> {
  return request<void>({
    url: `/reminders/rules/${id}`,
    method: 'DELETE'
  })
}
