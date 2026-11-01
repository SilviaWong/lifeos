/**
 * 目标与子任务前端类型定义
 */

export interface GoalTaskItem {
  id: number
  userId: number
  goalId: number
  title: string
  description?: string | null
  sortNo: number
  status: 'TODO' | 'DONE'
  statusDesc: string
  dueAt?: string | null
  completedAt?: string | null
  createdAt: string
  updatedAt: string
}

export interface GoalItem {
  id: number
  userId: number
  title: string
  description?: string | null
  startDate?: string | null
  dueDate?: string | null
  priority: number // 1: 高, 2: 中, 3: 低
  priorityDesc: string
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED'
  statusDesc: string
  progress: number // 0.00 ~ 100.00
  progressText: string // "尚未拆解" 或 "50%"
  totalTaskCount: number
  completedTaskCount: number
  completedAt?: string | null
  tasks?: GoalTaskItem[]
  createdAt: string
  updatedAt: string
}

export interface GoalSummary {
  totalCount: number
  activeCount: number
  completedCount: number
  archivedCount: number
  avgProgress: number
}

export interface CreateGoalDto {
  title: string
  description?: string
  startDate?: string | null
  dueDate?: string | null
  priority?: number
}

export interface UpdateGoalDto {
  title: string
  description?: string
  startDate?: string | null
  dueDate?: string | null
  priority?: number
}

export interface GoalQueryParams {
  pageNum?: number
  pageSize?: number
  status?: string
  priority?: number
  keyword?: string
  filterType?: string // ALL, ACTIVE, COMPLETED, ARCHIVED
}

export interface CreateGoalTaskDto {
  title: string
  description?: string
  sortNo?: number
  dueAt?: string | null
}

export interface UpdateGoalTaskDto {
  title: string
  description?: string
  sortNo?: number
  dueAt?: string | null
}

