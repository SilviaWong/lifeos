import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuthStorage } from '../utils/storage'
import type { ApiResponse } from '../types/api'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse

    // 状态码 0 代表业务处理成功
    if (res.code === 0) {
      return response
    }

    // 业务错误统一提示
    ElMessage.error(res.message || '请求处理失败')

    // 401 身份过期
    if (res.code === 401) {
      clearAuthStorage()
      if (window.location.pathname !== '/login') {
        window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
      }
    }

    return Promise.reject(new Error(res.message || '业务错误'))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络连接异常'

    if (status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
      clearAuthStorage()
      if (window.location.pathname !== '/login') {
        window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
      }
    } else if (status === 403) {
      ElMessage.error(message || '您无权执行此操作')
    } else if (status === 404) {
      ElMessage.error('请求的目标资源不存在')
    } else if (status >= 500) {
      ElMessage.error('服务器内部繁忙，请稍后再试')
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

/**
 * 封装通用请求方法
 */
export const request = <T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return service.request<ApiResponse<T>>(config).then((res) => res.data as ApiResponse<T>)
}

export default service

