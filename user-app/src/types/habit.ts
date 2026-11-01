/**
 * 习惯打卡类型定义
 */

export interface HabitItem {
  id: number
  name: string
  description?: string
  frequency: 'DAILY' | 'WEEKLY'
  frequencyDesc: string
  targetCount: number
  startDate: string
  endDate?: string
  status: 'ACTIVE' | 'PAUSED' | 'ARCHIVED'
  statusDesc: string
  reminderTime?: string
  createdAt: string
  updatedAt: string
  todayChecked: boolean
  currentStreak: number
  maxStreak: number
  totalCheckins: number
  completionRate: number
}

export interface CreateHabitDTO {
  name: string
  description?: string
  frequency?: 'DAILY' | 'WEEKLY'
  targetCount?: number
  startDate: string
  endDate?: string
  reminderTime?: string
}

export interface UpdateHabitDTO {
  name: string
  description?: string
  frequency?: 'DAILY' | 'WEEKLY'
  targetCount?: number
  startDate: string
  endDate?: string
  reminderTime?: string
}

export interface CheckinDTO {
  checkinDate?: string
  note?: string
}

export interface HabitCheckinItem {
  id: number
  habitId: number
  habitName: string
  checkinDate: string
  note?: string
  isMakeup: number
  createdAt: string
}

export interface HabitCalendarData {
  habitId: number
  habitName: string
  year: number
  month: number
  daysInMonth: number
  monthCheckins: number
  monthCompletionRate: number
  currentStreak: number
  maxStreak: number
  totalCheckins: number
  checkinDates: string[]
  checkinDetails: HabitCheckinItem[]
}

export interface HabitSummaryData {
  totalHabits: number
  activeHabits: number
  todayCheckedHabits: number
  overallCompletionRate: number
}

