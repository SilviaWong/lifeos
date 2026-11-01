/**
 * 待办任务数据类型定义
 */

export interface TodoItem {
  id: number
  userId: number
  goalId?: number | null
  title: string
  description?: string | null
  priority: number // 1: 低, 2: 中, 3: 高, 4: 紧急
  priorityDesc: string
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'
  statusDesc: string
  startAt?: string | null
  dueAt?: string | null
  remindAt?: string | null
  completedAt?: string | null
  isOverdue?: boolean
  createdAt: string
  updatedAt: string
}

export interface TodoSummary {
  totalCount: number
  todayTotalCount: number
  todayCompletedCount: number
  pendingCount: number
  completedCount: number
  overdueCount: number
}

export interface CreateTodoDto {
  title: string
  description?: string
  priority?: number
  goalId?: number | null
  startAt?: string | null
  dueAt?: string | null
  remindAt?: string | null
}

export interface UpdateTodoDto {
  title: string
  description?: string
  priority?: number
  goalId?: number | null
  startAt?: string | null
  dueAt?: string | null
  remindAt?: string | null
}

export interface TodoQueryParams {
  pageNum?: number
  pageSize?: number
  status?: string
  priority?: number
  keyword?: string
  goalId?: number
  filterType?: string // ALL, TODAY, WEEK, OVERDUE, PENDING, COMPLETED
  startDate?: string
  endDate?: string
}

