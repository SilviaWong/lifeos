/**
 * 财务收支与记账类型定义
 */

export interface FinanceCategoryItem {
  id: number
  userId?: number
  categoryName: string
  categoryType: 'INCOME' | 'EXPENSE'
  categoryTypeDesc: string
  icon?: string
  sortNo: number
  status: number
  isSystem: boolean
}

export interface FinanceRecordItem {
  id: number
  userId: number
  categoryId?: number
  categoryName?: string
  categoryIcon?: string
  recordType: 'INCOME' | 'EXPENSE'
  recordTypeDesc: string
  amount: number
  currencyCode: string
  recordDate: string
  title?: string
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface CreateFinanceRecordDTO {
  recordType: 'INCOME' | 'EXPENSE'
  amount: number
  categoryId?: number
  recordDate: string
  title?: string
  remark?: string
  currencyCode?: string
}

export interface UpdateFinanceRecordDTO {
  recordType: 'INCOME' | 'EXPENSE'
  amount: number
  categoryId?: number
  recordDate: string
  title?: string
  remark?: string
  currencyCode?: string
}

export interface FinanceCategoryStat {
  categoryId?: number
  categoryName: string
  categoryIcon?: string
  recordType: 'INCOME' | 'EXPENSE'
  totalAmount: number
  recordCount: number
  percentage: number
}

export interface FinanceSummaryData {
  year: number
  month: number
  totalIncome: number
  totalExpense: number
  balance: number
  recordCount: number
  expenseCategoryStats: FinanceCategoryStat[]
  incomeCategoryStats: FinanceCategoryStat[]
}

export interface FinanceTrendPoint {
  month: string
  income: number
  expense: number
  balance: number
}

export interface FinanceTrendData {
  points: FinanceTrendPoint[]
  totalPeriodIncome: number
  totalPeriodExpense: number
  totalPeriodBalance: number
}

