<template>
  <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar-container">
    <!-- 顶部 Logo 盾牌标识 -->
    <div class="logo-box">
      <div class="shield-logo">
        <span class="shield-icon">🛡️</span>
      </div>
      <span v-if="!appStore.sidebarCollapsed" class="logo-text">个人综合管理</span>
    </div>

    <!-- 侧边栏分组菜单 -->
    <el-scrollbar class="menu-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="#FFFFFF"
        text-color="#64748B"
        active-text-color="#FFFFFF"
        router
        class="sidebar-menu"
      >
        <!-- 数据分析看板 -->
        <div v-if="!appStore.sidebarCollapsed" class="group-title">数据分析</div>
        <el-menu-item index="/dashboard" class="menu-pill-item">
          <el-icon><Odometer /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>

        <!-- 系统管理 -->
        <div v-if="!appStore.sidebarCollapsed" class="group-title">系统管理</div>
        <el-menu-item index="/users" class="menu-pill-item">
          <el-icon><User /></el-icon>
          <template #title>账号管理</template>
        </el-menu-item>

        <!-- 健康管理 -->
        <div v-if="!appStore.sidebarCollapsed" class="group-title">健康管理</div>
        <el-menu-item index="/health" class="menu-pill-item">
          <el-icon><FirstAidKit /></el-icon>
          <template #title>健康管理</template>
        </el-menu-item>

        <!-- 收支分类管理 -->
        <div v-if="!appStore.sidebarCollapsed" class="group-title">收支管理</div>
        <el-menu-item index="/finance" class="menu-pill-item">
          <el-icon><Money /></el-icon>
          <template #title>收支分类</template>
        </el-menu-item>

        <!-- 运营分析与日志 -->
        <div v-if="!appStore.sidebarCollapsed" class="group-title">运营审计</div>
        <el-menu-item index="/operations" class="menu-pill-item">
          <el-icon><TrendCharts /></el-icon>
          <template #title>模块统计与日志</template>
        </el-menu-item>

        <!-- 智能联动规则 -->
        <div v-if="!appStore.sidebarCollapsed" class="group-title">智能联动</div>
        <el-menu-item index="/system" class="menu-pill-item">
          <el-icon><Operation /></el-icon>
          <template #title>联动规则配置</template>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>

    <!-- 底部退出登录入口 -->
    <div class="sidebar-footer" @click="handleLogout">
      <el-icon class="logout-icon"><SwitchButton /></el-icon>
      <span v-if="!appStore.sidebarCollapsed" class="logout-text">退出登录</span>
    </div>
  </el-aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Odometer,
  User,
  FirstAidKit,
  Money,
  TrendCharts,
  Operation,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前管理员登录状态吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    ElMessage.success('已安全退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.sidebar-container {
  height: 100vh;
  background-color: #FFFFFF;
  border-right: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  user-select: none;
}

/* 顶部 Logo */
.logo-box {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  gap: 12px;
  border-bottom: 1px solid #F1F5F9;
}
.shield-logo {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.25);
  flex-shrink: 0;
}
.shield-icon {
  font-size: 20px;
}
.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

/* 菜单分组标题 */
.group-title {
  font-size: 11px;
  font-weight: 700;
  color: #94A3B8;
  padding: 16px 20px 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-scrollbar {
  flex: 1;
}

.sidebar-menu {
  border-right: none;
  padding: 8px 10px;
}

/* 胶囊状高亮选中样式 (对标微信文章截图) */
.menu-pill-item {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.2s ease;
}
.menu-pill-item:hover {
  background-color: #F1F5F9 !important;
  color: #1E293B !important;
}
.menu-pill-item.is-active {
  background: #2563EB !important;
  color: #FFFFFF !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}
.menu-pill-item.is-active :deep(.el-icon) {
  color: #FFFFFF !important;
}

/* 底部退出登录 */
.sidebar-footer {
  height: 52px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-top: 1px solid #F1F5F9;
  cursor: pointer;
  color: #EF4444;
  transition: background-color 0.15s ease;
}
.sidebar-footer:hover {
  background-color: #FEF2F2;
}
.logout-icon {
  font-size: 18px;
}
.logout-text {
  font-size: 14px;
  font-weight: 500;
}
</style>
