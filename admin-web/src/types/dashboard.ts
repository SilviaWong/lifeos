export interface DateRangeFilter {
  startDate: string
  endDate: string
  dayCount: number
}

export interface DashboardCardMetrics {
  totalUserCount: number
  rangeNewUserCount: number
  rangeActiveUserCount: number
  todayActiveUserCount: number
  totalHealthRecordCount: number
  rangeHealthRecordCount: number
  totalExpenseAmount: number
  rangeExpenseAmount: number
  totalIncomeAmount: number
  rangeIncomeAmount: number
  rangeNetBalance: number
  totalFocusMinutes: number
  rangeFocusMinutes: number
  totalTodoCount: number
  completedTodoCount: number
  totalHabitCheckinCount: number
}

export interface UserTrendPoint {
  date: string
  newUsers: number
  activeUsers: number
}

export interface ModuleUsageRank {
  moduleCode: string
  moduleName: string
  count: number
  uniqueUsers: number
  percentage: number
}

export interface HealthTypeStat {
  recordType: string
  recordTypeName: string
  count: number
}

export interface FinanceStatSummary {
  totalExpense: number
  totalIncome: number
  netBalance: number
  recordCount: number
}

export interface AdminDashboardOverview {
  filter: DateRangeFilter
  overviewCards: DashboardCardMetrics
  userTrend: UserTrendPoint[]
  moduleUsageRank: ModuleUsageRank[]
  healthDistribution: HealthTypeStat[]
  financeSummary: FinanceStatSummary
}

export interface ModuleDailyStat {
  moduleCode: string
  moduleName: string
  counts: number[]
}

export interface AdminModuleTrend {
  dates: string[]
  series: ModuleDailyStat[]
}

export interface DashboardQueryParam {
  startDate?: string
  endDate?: string
}

