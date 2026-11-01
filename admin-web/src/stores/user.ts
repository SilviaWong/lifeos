import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, logoutApi, getMyInfoApi } from '../api/auth'
import { getToken, setToken, getUser, setUser, clearAuthStorage } from '../utils/storage'
import type { LoginDTO, UserVO } from '../types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserVO | null>(getUser())

  const roles = computed<string[]>(() => {
    return userInfo.value?.roles || []
  })

  const isLoggedIn = computed<boolean>(() => {
    return Boolean(token.value)
  })

  const isAdmin = computed<boolean>(() => {
    const list = roles.value
    return list.includes('ADMIN') || list.includes('SUPER_ADMIN')
  })

  const login = async (form: LoginDTO): Promise<void> => {
    const res = await loginApi(form)
    const { token: jwtToken, userInfo: user } = res.data

    // 严格检查是否具备管理端权限
    const hasAdminRole = user.roles && (user.roles.includes('ADMIN') || user.roles.includes('SUPER_ADMIN'))
    if (!hasAdminRole) {
      throw new Error('当前账号非管理员，无权登录后台管理系统')
    }

    token.value = jwtToken
    userInfo.value = user
    setToken(jwtToken)
    setUser(user)
  }

  const fetchUserInfo = async (): Promise<UserVO> => {
    const res = await getMyInfoApi()
    userInfo.value = res.data
    setUser(res.data)
    return res.data
  }

  const logout = async (): Promise<void> => {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch (e) {
      // 忽略登出请求网络异常
    } finally {
      token.value = null
      userInfo.value = null
      clearAuthStorage()
    }
  }

  return {
    token,
    userInfo,
    roles,
    isLoggedIn,
    isAdmin,
    login,
    fetchUserInfo,
    logout
  }
})

