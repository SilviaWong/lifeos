/**
 * 全局统一接口响应结构
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  traceId?: string
  timestamp?: number
}

/**
 * 统一分页查询请求基类
 */
export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

/**
 * 统一分页结果包装
 */
export interface PageResult<T = any> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

