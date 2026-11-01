import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  PersonalItem,
  ItemLending,
  ItemDetail,
  Memo,
  Diary,
  LifeRecordSummary
} from '../types/life'

// ==================== 概览 ====================

/**
 * 获取生活记录汇总统计
 */
export const getLifeSummaryApi = (): Promise<ApiResponse<LifeRecordSummary>> => {
  return request<LifeRecordSummary>({
    url: '/items/summary',
    method: 'GET'
  })
}

// ==================== 个人物品 ====================

/**
 * 分页查询物品列表
 */
export const getItemsApi = (params?: {
  keyword?: string
  category?: string
  status?: string
  isLent?: boolean
  current?: number
  size?: number
}): Promise<ApiResponse<PageResult<PersonalItem>>> => {
  return request<PageResult<PersonalItem>>({
    url: '/items',
    method: 'GET',
    data: params
  })
}

/**
 * 获取物品详情及借还记录
 */
export const getItemDetailApi = (id: number): Promise<ApiResponse<ItemDetail>> => {
  return request<ItemDetail>({
    url: `/items/${id}`,
    method: 'GET'
  })
}

/**
 * 新增物品
 */
export const createItemApi = (data: {
  itemName: string
  category?: string
  quantity?: number
  unit?: string
  storageLocation?: string
  purchaseDate?: string
  remark?: string
}): Promise<ApiResponse<PersonalItem>> => {
  return request<PersonalItem>({
    url: '/items',
    method: 'POST',
    data
  })
}

/**
 * 更新物品
 */
export const updateItemApi = (id: number, data: {
  itemName: string
  category?: string
  quantity?: number
  unit?: string
  storageLocation?: string
  purchaseDate?: string
  remark?: string
}): Promise<ApiResponse<PersonalItem>> => {
  return request<PersonalItem>({
    url: `/items/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除物品
 */
export const deleteItemApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/items/${id}`,
    method: 'DELETE'
  })
}

/**
 * 归档 / 取消归档物品
 */
export const toggleArchiveItemApi = (id: number): Promise<ApiResponse<PersonalItem>> => {
  return request<PersonalItem>({
    url: `/items/${id}/archive`,
    method: 'PUT'
  })
}

// ==================== 物品借还 ====================

/**
 * 登记借出
 */
export const lendItemApi = (itemId: number, data: {
  borrowerName: string
  lentAt?: string
  expectedReturnAt?: string
  remark?: string
}): Promise<ApiResponse<ItemLending>> => {
  return request<ItemLending>({
    url: `/items/${itemId}/lend`,
    method: 'POST',
    data
  })
}

/**
 * 登记归还
 */
export const returnItemApi = (lendingId: number, data?: {
  returnedAt?: string
  remark?: string
}): Promise<ApiResponse<ItemLending>> => {
  return request<ItemLending>({
    url: `/lendings/${lendingId}/return`,
    method: 'POST',
    data: data || {}
  })
}

/**
 * 查询物品借还历史
 */
export const getItemLendingsApi = (itemId: number): Promise<ApiResponse<ItemLending[]>> => {
  return request<ItemLending[]>({
    url: `/items/${itemId}/lendings`,
    method: 'GET'
  })
}

/**
 * 分页查询借还总表
 */
export const getLendingsApi = (params?: {
  itemId?: number
  borrowerName?: string
  status?: string
  current?: number
  size?: number
}): Promise<ApiResponse<PageResult<ItemLending>>> => {
  return request<PageResult<ItemLending>>({
    url: '/lendings',
    method: 'GET',
    data: params
  })
}

// ==================== 备忘录 ====================

/**
 * 分页查询备忘录列表
 */
export const getMemosApi = (params?: {
  keyword?: string
  tag?: string
  status?: string
  isPinned?: number
  current?: number
  size?: number
}): Promise<ApiResponse<PageResult<Memo>>> => {
  return request<PageResult<Memo>>({
    url: '/memos',
    method: 'GET',
    data: params
  })
}

/**
 * 获取备忘录详情
 */
export const getMemoDetailApi = (id: number): Promise<ApiResponse<Memo>> => {
  return request<Memo>({
    url: `/memos/${id}`,
    method: 'GET'
  })
}

/**
 * 新增备忘录
 */
export const createMemoApi = (data: {
  title: string
  content?: string
  tags?: string
  isPinned?: number
}): Promise<ApiResponse<Memo>> => {
  return request<Memo>({
    url: '/memos',
    method: 'POST',
    data
  })
}

/**
 * 更新备忘录
 */
export const updateMemoApi = (id: number, data: {
  title: string
  content?: string
  tags?: string
  isPinned?: number
}): Promise<ApiResponse<Memo>> => {
  return request<Memo>({
    url: `/memos/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除备忘录
 */
export const deleteMemoApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/memos/${id}`,
    method: 'DELETE'
  })
}

/**
 * 置顶 / 取消置顶备忘录
 */
export const togglePinMemoApi = (id: number): Promise<ApiResponse<Memo>> => {
  return request<Memo>({
    url: `/memos/${id}/pin`,
    method: 'PUT'
  })
}

/**
 * 归档 / 取消归档备忘录
 */
export const toggleArchiveMemoApi = (id: number): Promise<ApiResponse<Memo>> => {
  return request<Memo>({
    url: `/memos/${id}/archive`,
    method: 'PUT'
  })
}

// ==================== 心情日记 ====================

/**
 * 分页查询日记列表
 */
export const getDiariesApi = (params?: {
  startDate?: string
  endDate?: string
  mood?: string
  weather?: string
  keyword?: string
  current?: number
  size?: number
}): Promise<ApiResponse<PageResult<Diary>>> => {
  return request<PageResult<Diary>>({
    url: '/diaries',
    method: 'GET',
    data: params
  })
}

/**
 * 获取日记详情
 */
export const getDiaryDetailApi = (id: number): Promise<ApiResponse<Diary>> => {
  return request<Diary>({
    url: `/diaries/${id}`,
    method: 'GET'
  })
}

/**
 * 新增日记
 */
export const createDiaryApi = (data: {
  diaryDate: string
  title?: string
  content: string
  mood?: string
  weather?: string
  coverUrl?: string
}): Promise<ApiResponse<Diary>> => {
  return request<Diary>({
    url: '/diaries',
    method: 'POST',
    data
  })
}

/**
 * 更新日记
 */
export const updateDiaryApi = (id: number, data: {
  title?: string
  content: string
  mood?: string
  weather?: string
  coverUrl?: string
}): Promise<ApiResponse<Diary>> => {
  return request<Diary>({
    url: `/diaries/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除日记
 */
export const deleteDiaryApi = (id: number): Promise<ApiResponse<void>> => {
  return request<void>({
    url: `/diaries/${id}`,
    method: 'DELETE'
  })
}

