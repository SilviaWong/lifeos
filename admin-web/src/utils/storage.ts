import type { UserVO } from '../types/user'

const TOKEN_KEY = 'lifeos_admin_token'
const USER_KEY = 'lifeos_admin_user'

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getUser = (): UserVO | null => {
  const userJson = localStorage.getItem(USER_KEY)
  if (!userJson) return null
  try {
    return JSON.parse(userJson) as UserVO
  } catch {
    return null
  }
}

export const setUser = (user: UserVO): void => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const removeUser = (): void => {
  localStorage.removeItem(USER_KEY)
}

export const clearAuthStorage = (): void => {
  removeToken()
  removeUser()
}

