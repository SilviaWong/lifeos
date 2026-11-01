import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '管理员登录', public: true }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'Odometer', roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/users/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('../views/health/index.vue'),
        meta: { title: '健康管理', icon: 'FirstAidKit', roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('../views/finance/index.vue'),
        meta: { title: '财务管理', icon: 'Money', roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'operations',
        name: 'Operations',
        component: () => import('../views/operations/index.vue'),
        meta: { title: '运营管理', icon: 'TrendCharts', roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('../views/system/index.vue'),
        meta: { title: '系统管理', icon: 'Setting', roles: ['ADMIN', 'SUPER_ADMIN'] }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/error/403.vue'),
    meta: { title: '403 无权限访问', public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '404 页面未找到', public: true }
  }
]

