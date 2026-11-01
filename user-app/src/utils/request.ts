import { getToken, clearAuthStorage } from './storage'
import type { ApiResponse } from '../types/api'

// 环境配置：H5 开发代理为 /api，移动端/小程序为本地后端地址
const BASE_URL = (() => {
  // #ifdef H5
  return '/api'
  // #endif
  // #ifndef H5
  return 'http://localhost:8080/api'
  // #endif
})()

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  showLoading?: boolean
  loadingTitle?: string
}

export const request = <T = any>(options: RequestOptions): Promise<ApiResponse<T>> => {
  return new Promise((resolve, reject) => {
    if (options.showLoading) {
      uni.showLoading({
        title: options.loadingTitle || '加载中...',
        mask: true
      })
    }

    const token = getToken()
    const headers: Record<string, string> = {
      'Content-Type': 'application/json;charset=UTF-8',
      ...options.header
    }
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: options.url.startsWith('http') ? options.url : `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: headers,
      timeout: 15000,
      success: (res) => {
        if (options.showLoading) {
          uni.hideLoading()
        }

        const statusCode = res.statusCode
        const apiRes = res.data as ApiResponse<T>

        if (statusCode === 200) {
          if (apiRes.code === 0) {
            resolve(apiRes)
          } else if (apiRes.code === 401) {
            uni.showToast({
              title: '登录状态已失效，请重新登录',
              icon: 'none'
            })
            clearAuthStorage()
            uni.navigateTo({ url: '/pages/login/index' })
            reject(new Error(apiRes.message || '未登录'))
          } else {
            uni.showToast({
              title: apiRes.message || '操作失败',
              icon: 'none'
            })
            reject(new Error(apiRes.message || '业务异常'))
          }
        } else if (statusCode === 401) {
          uni.showToast({
            title: '请先登录账号',
            icon: 'none'
          })
          clearAuthStorage()
          uni.navigateTo({ url: '/pages/login/index' })
          reject(new Error('未登录'))
        } else if (statusCode === 403) {
          uni.showToast({
            title: '无权限执行该操作',
            icon: 'none'
          })
          reject(new Error('无权限'))
        } else {
          uni.showToast({
            title: apiRes?.message || `网络请求错误 (${statusCode})`,
            icon: 'none'
          })
          reject(new Error(apiRes?.message || '请求失败'))
        }
      },
      fail: (err) => {
        if (options.showLoading) {
          uni.hideLoading()
        }
        uni.showToast({
          title: '网络连接失败，请检查服务状态',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request

