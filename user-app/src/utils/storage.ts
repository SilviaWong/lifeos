import type { UserVO } from '../types/user'

const TOKEN_KEY = 'lifeos_user_token'
const USER_KEY = 'lifeos_user_info'

export const getToken = (): string => {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export const setToken = (token: string): void => {
  uni.setStorageSync(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  uni.removeStorageSync(TOKEN_KEY)
}

export const getUser = (): UserVO | null => {
  return uni.getStorageSync(USER_KEY) || null
}

export const setUser = (user: UserVO): void => {
  uni.setStorageSync(USER_KEY, user)
}

export const removeUser = (): void => {
  uni.removeStorageSync(USER_KEY)
}

export const clearAuthStorage = (): void => {
  removeToken()
  removeUser()
}

