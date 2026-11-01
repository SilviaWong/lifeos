export interface ReminderRule {
  id: number
  userId: number
  ruleName: string
  triggerType: 'TODO_DUE' | 'HABIT_CHECKIN' | 'GOAL_DUE' | 'CUSTOM'
  triggerTypeDesc?: string
  businessRefId?: number
  remindAt?: string
  advanceMinutes: number
  repeatRule: 'DAILY' | 'WORKDAY' | 'WEEKEND' | 'ONCE'
  repeatRuleDesc?: string
  startDate?: string
  endDate?: string
  status: number
  statusDesc?: string
  createdAt: string
  updatedAt: string
}

export interface ReminderMessage {
  id: number
  userId: number
  ruleId?: number
  title: string
  content?: string
  messageType: 'TODO' | 'HABIT' | 'GOAL' | 'SYSTEM' | 'ITEM_DUE'
  messageTypeDesc?: string
  businessRefId?: number
  scheduledAt?: string
  sentAt?: string
  readAt?: string
  status: 'PENDING' | 'SENT' | 'READ' | 'FAILED'
  isRead: boolean
  createdAt: string
}

export interface ReminderExecutionSummary {
  totalRulesCount: number
  activeRulesCount: number
  unreadMessagesCount: number
  todayMessagesCount: number
}

