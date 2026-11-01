import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { getToken } from './utils/storage'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()

  app.use(pinia)

  // 路由拦截器：保护需要登录的页面
  const whiteList = [
    '/pages/login/index',
    '/pages/register/index',
    '/pages/index/index'
  ]

  const navInterceptors = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab']
  navInterceptors.forEach((method) => {
    uni.addInterceptor(method, {
      invoke(e: any) {
        const url = (e.url || '').split('?')[0]
        const isWhite = whiteList.some((path) => url === path || url.endsWith(path))
        if (!isWhite && !getToken()) {
          uni.showToast({
            title: '请先登录个人账号',
            icon: 'none'
          })
          uni.navigateTo({
            url: `/pages/login/index?redirect=${encodeURIComponent(e.url)}`
          })
          return false
        }
        return true
      }
    })
  })

  return {
    app,
    pinia
  }
}

