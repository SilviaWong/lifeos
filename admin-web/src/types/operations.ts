export interface ModuleUsageLogItem {
  id: number
  userId: number
  username: string
  nickname: string
  moduleCode: string
  moduleName: string
  actionCode: string
  actionName: string
  businessRefId?: number
  occurredAt: string
  requestId?: string
}

export interface ModuleUsageLogQuery {
  moduleCode?: string
  actionCode?: string
  userId?: number
  requestId?: string
  startDate?: string
  endDate?: string
  current?: number
  size?: number
}

