<template>
  <el-header class="header-container">
    <div class="header-left">
      <el-button
        type="text"
        class="collapse-btn"
        @click="appStore.toggleSidebar"
      >
        <el-icon :size="20">
          <Expand v-if="appStore.sidebarCollapsed" />
          <Fold v-else />
        </el-icon>
      </el-button>

      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle !== '数据看板'">
          {{ currentTitle }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-profile">
          <el-avatar :size="32" :src="userInfo?.avatarUrl" class="user-avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="username">{{ userInfo?.nickname || userInfo?.username || '管理员' }}</span>
          <el-tag size="small" type="success" effect="plain" class="role-tag">
            {{ isSuperAdmin ? '超级管理员' : '管理员' }}
          </el-tag>
          <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><UserFilled /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold,
  Expand,
  ArrowDown,
  UserFilled,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const isSuperAdmin = computed(() => userStore.roles.includes('SUPER_ADMIN'))
const currentTitle = computed(() => (route.meta?.title as string) || '')

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出当前管理员登录状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await userStore.logout()
      ElMessage.success('已安全退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    ElMessage.info('个人设置功能正在完善中')
  }
}
</script>

<style scoped>
.header-container {
  height: 56px;
  background-color: #ffffff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  color: #595959;
  font-size: 18px;
  padding: 4px;
}

.collapse-btn:hover {
  color: #1677ff;
}

.breadcrumb {
  margin-left: 8px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-profile:hover {
  background-color: #f5f5f5;
}

.user-avatar {
  background-color: #1677ff;
}

.username {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.role-tag {
  font-size: 11px;
}

.dropdown-arrow {
  color: #8c8c8c;
  font-size: 12px;
}
</style>

