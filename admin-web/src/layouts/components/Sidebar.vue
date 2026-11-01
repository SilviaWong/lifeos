<template>
  <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar-container">
    <div class="logo-box">
      <div class="logo-icon">🌿</div>
      <span v-if="!appStore.sidebarCollapsed" class="logo-text">LifeOS Admin</span>
    </div>

    <el-scrollbar class="menu-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>

        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>

        <el-menu-item index="/health">
          <el-icon><FirstAidKit /></el-icon>
          <template #title>健康管理</template>
        </el-menu-item>

        <el-menu-item index="/finance">
          <el-icon><Money /></el-icon>
          <template #title>财务管理</template>
        </el-menu-item>

        <el-menu-item index="/operations">
          <el-icon><TrendCharts /></el-icon>
          <template #title>运营管理</template>
        </el-menu-item>

        <el-menu-item index="/system">
          <el-icon><Setting /></el-icon>
          <template #title>系统管理</template>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>
  </el-aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '../../stores/app'
import {
  Odometer,
  User,
  FirstAidKit,
  Money,
  TrendCharts,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped>
.sidebar-container {
  background-color: #001529;
  height: 100vh;
  transition: width 0.28s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.logo-box {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: #002140;
  color: #ffffff;
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
  gap: 10px;
}

.logo-icon {
  font-size: 20px;
}

.logo-text {
  letter-spacing: 0.5px;
}

.menu-scrollbar {
  flex: 1;
}

.sidebar-menu {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  background-color: #1677ff !important;
}

:deep(.el-menu-item:hover) {
  color: #ffffff !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
}
</style>

