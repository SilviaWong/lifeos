import { request } from '../utils/request'
import type { ApiResponse, PageResult } from '../types/api'
import type {
  AbortPomodoroDto,
  FinishPomodoroDto,
  PomodoroQueryParams,
  PomodoroRecordItem,
  PomodoroSetting,
  PomodoroSummary,
  StartPomodoroDto
} from '../types/pomodoro'

/**
 * 开启一轮番茄钟专注会话
 */
export const startPomodoroApi = (data: StartPomodoroDto): Promise<ApiResponse<PomodoroRecordItem>> => {
  return request<PomodoroRecordItem>({
    url: '/pomodoros/start',
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在开启专注...'
  })
}

/**
 * 暂停当前进行中的会话
 */
export const pausePomodoroApi = (id: number): Promise<ApiResponse<PomodoroRecordItem>> => {
  return request<PomodoroRecordItem>({
    url: `/pomodoros/${id}/pause`,
    method: 'POST'
  })
}

/**
 * 恢复继续已暂停的会话
 */
export const resumePomodoroApi = (id: number): Promise<ApiResponse<PomodoroRecordItem>> => {
  return request<PomodoroRecordItem>({
    url: `/pomodoros/${id}/resume`,
    method: 'POST'
  })
}

/**
 * 完成番茄钟专注会话
 */
export const finishPomodoroApi = (
  id: number,
  data?: FinishPomodoroDto
): Promise<ApiResponse<PomodoroRecordItem>> => {
  return request<PomodoroRecordItem>({
    url: `/pomodoros/${id}/finish`,
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在结算...'
  })
}

/**
 * 中途放弃/中止番茄钟会话
 */
export const abortPomodoroApi = (
  id: number,
  data?: AbortPomodoroDto
): Promise<ApiResponse<PomodoroRecordItem>> => {
  return request<PomodoroRecordItem>({
    url: `/pomodoros/${id}/abort`,
    method: 'POST',
    data,
    showLoading: true,
    loadingTitle: '正在中止...'
  })
}

/**
 * 获取用户当前活动中的会话（供刷新或退出后恢复计时状态）
 */
export const getCurrentPomodoroApi = (): Promise<ApiResponse<PomodoroRecordItem | null>> => {
  return request<PomodoroRecordItem | null>({
    url: '/pomodoros/current',
    method: 'GET'
  })
}

/**
 * 获取番茄钟专注统计摘要（今日/本周/累计）
 */
export const getPomodoroSummaryApi = (): Promise<ApiResponse<PomodoroSummary>> => {
  return request<PomodoroSummary>({
    url: '/pomodoros/summary',
    method: 'GET'
  })
}

/**
 * 分页查询番茄钟历史记录
 */
export const getPomodoroRecordsApi = (
  params?: PomodoroQueryParams
): Promise<ApiResponse<PageResult<PomodoroRecordItem>>> => {
  return request<PageResult<PomodoroRecordItem>>({
    url: '/pomodoros/records',
    method: 'GET',
    data: params
  })
}

/**
 * 获取用户番茄钟偏好设置
 */
export const getPomodoroSettingApi = (): Promise<ApiResponse<PomodoroSetting>> => {
  return request<PomodoroSetting>({
    url: '/pomodoros/settings',
    method: 'GET'
  })
}

/**
 * 修改用户番茄钟偏好设置
 */
export const updatePomodoroSettingApi = (
  data: Partial<PomodoroSetting>
): Promise<ApiResponse<PomodoroSetting>> => {
  return request<PomodoroSetting>({
    url: '/pomodoros/settings',
    method: 'PUT',
    data,
    showLoading: true,
    loadingTitle: '正在保存设置...'
  })
}

