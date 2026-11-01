import type { Router } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

export const setupRouterGuard = (router: Router): void => {
  router.beforeEach(async (to, _from, next) => {
    // 动态设置网页标题
    const title = (to.meta?.title as string) || 'LifeOS 管理端'
    document.title = `${title} - LifeOS 管理控制台`

    const userStore = useUserStore()

    // 公开页面直接放行（如登录页、403、404）
    if (to.meta?.public) {
      // 若已登录访问登录页，直接重定向至仪表盘
      if (to.path === '/login' && userStore.isLoggedIn) {
        return next('/dashboard')
      }
      return next()
    }

    // 检查是否有 Token
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录管理员账号')
      return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }

    // 若本地没有用户信息，拉取最新当前用户信息
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        await userStore.logout()
        return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      }
    }

    // 校验管理端角色权限 (ADMIN / SUPER_ADMIN)
    if (!userStore.isAdmin) {
      ElMessage.error('当前账号无权限访问后台管理系统')
      return next('/403')
    }

    // 检查具体路由的角色权限要求
    const allowedRoles = to.meta?.roles as string[] | undefined
    if (allowedRoles && allowedRoles.length > 0) {
      const hasPermission = userStore.roles.some((r) => allowedRoles.includes(r))
      if (!hasPermission) {
        return next('/403')
      }
    }

    next()
  })
}

