import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, logoutApi, getMyInfoApi } from '../api/auth'
import { getToken, setToken, getUser, setUser, clearAuthStorage } from '../utils/storage'
import type { LoginDTO, RegisterDTO, UserVO } from '../types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const userInfo = ref<UserVO | null>(getUser())

  const isLoggedIn = computed<boolean>(() => {
    return Boolean(token.value)
  })

  const login = async (form: LoginDTO): Promise<void> => {
    const res = await loginApi(form)
    const { token: jwtToken, userInfo: user } = res.data

    token.value = jwtToken
    userInfo.value = user
    setToken(jwtToken)
    setUser(user)
  }

  const register = async (form: RegisterDTO): Promise<void> => {
    await registerApi(form)
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
    } catch {
      // 忽略登出网络报错
    } finally {
      token.value = ''
      userInfo.value = null
      clearAuthStorage()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    fetchUserInfo,
    logout
  }
})

