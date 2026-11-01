/**
 * 首页综合看板摘要类型定义
 */

export interface HomeSummary {
  greeting: string
  currentDate: string
  dayOfWeekDesc: string
  todayTodoCount: number
  todayCompletedTodoCount: number
  pendingTodoCount: number
  totalTodoCount: number
  overdueTodoCount: number
  todayHabitCheckinCount: number
  activeGoalCount: number
  todayFocusMinutes: number
}

