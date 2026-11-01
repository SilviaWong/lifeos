/**
 * 健康数据与趋势类型定义
 */

export type HealthMetricCode = 'STEPS' | 'SLEEP_HOURS' | 'WEIGHT' | 'WATER_ML' | 'CALORIES'

export interface HealthRecordItem {
  id: number
  userId: number
  metricCode: HealthMetricCode
  metricName: string
  metricValue: number
  unit: string
  recordTime: string
  note?: string | null
  source: string
  standardStatus?: 'NORMAL' | 'BELOW' | 'ABOVE' | 'UNKNOWN'
  standardHint?: string
  createdAt: string
}

export interface CreateHealthRecordDto {
  metricCode: HealthMetricCode
  metricValue: number
  unit?: string
  recordTime?: string
  note?: string
  source?: string
}

export interface UpdateHealthRecordDto {
  metricValue?: number
  unit?: string
  recordTime?: string
  note?: string
}

export interface HealthRecordQueryParams {
  pageNum?: number
  pageSize?: number
  metricCode?: string
  startDate?: string
  endDate?: string
}

export interface HealthTrendPoint {
  date: string
  value: number | null
  recordCount: number
}

export interface HealthTrendResult {
  metricCode: string
  metricName: string
  unit: string
  standardMinValue?: number | null
  standardMaxValue?: number | null
  aggregateMethod: string
  points: HealthTrendPoint[]
  disclaimer: string
}

export interface HealthTrendQueryParams {
  metricCode: string
  startDate?: string
  endDate?: string
  period?: string
}

export interface HealthDashboardSummary {
  todaySteps: number
  stepsStatus: string
  todaySleepHours: number
  sleepStatus: string
  todayWaterMl: number
  waterStatus: string
  latestWeight: number | null
  weightStatus: string
  todayCalories: number
  caloriesStatus: string
  disclaimer: string
}

export interface HealthStandardItem {
  id: number
  metricCode: string
  metricName: string
  unit: string
  minValue?: number | null
  maxValue?: number | null
  applicableScope: string
  description?: string
  effectiveFrom?: string
  effectiveTo?: string
  status: number
}

