/**
 * 番茄钟专注会话与偏好设置类型定义
 */

export type PomodoroSessionType = 'FOCUS' | 'SHORT_BREAK' | 'LONG_BREAK'
export type PomodoroStatus = 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'ABORTED'

export interface PomodoroRecordItem {
  id: number
  userId: number
  goalId?: number | null
  goalTitle?: string | null
  todoId?: number | null
  todoTitle?: string | null
  sessionType: PomodoroSessionType
  sessionTypeDesc: string
  plannedSeconds: number
  actualSeconds: number
  status: PomodoroStatus
  statusDesc: string
  startedAt: string
  endedAt?: string | null
  endReason?: string | null
  createdAt: string
}

export interface StartPomodoroDto {
  sessionType?: PomodoroSessionType
  plannedSeconds: number
  goalId?: number | null
  todoId?: number | null
}

export interface FinishPomodoroDto {
  clientActualSeconds?: number
}

export interface AbortPomodoroDto {
  reason?: string
  clientActualSeconds?: number
}

export interface PomodoroSetting {
  focusDuration: number
  shortBreakDuration: number
  longBreakDuration: number
  longBreakInterval: number
  autoStartBreaks: boolean
  autoStartPomodoros: boolean
}

export interface PomodoroSummary {
  todayFocusSeconds: number
  todayFocusMinutes: number
  todayCompletedCount: number
  weekFocusSeconds: number
  weekFocusMinutes: number
  weekCompletedCount: number
  totalCompletedCount: number
  totalFocusMinutes: number
}

export interface PomodoroQueryParams {
  pageNum?: number
  pageSize?: number
  sessionType?: string
  status?: string
  goalId?: number
  todoId?: number
  startDate?: string
  endDate?: string
}

