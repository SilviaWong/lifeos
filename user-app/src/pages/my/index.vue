<template>
  <view class="my-page">
    <!-- 用户头像昵称信息栏 -->
    <view class="profile-header">
      <view class="profile-box" @click="handleProfileClick">
        <view class="avatar-circle">
          <text class="avatar-text">👤</text>
        </view>
        <view class="user-meta">
          <text class="nickname">{{ userStore.isLoggedIn ? (userStore.userInfo?.nickname || userStore.userInfo?.username) : '未登录用户' }}</text>
          <text class="username">{{ userStore.isLoggedIn ? `用户名: ${userStore.userInfo?.username}` : '点击立即登录/注册' }}</text>
        </view>
        <text class="arrow">></text>
      </view>
    </view>

    <!-- 个人生活记录模块导航 -->
    <view class="life-card">
      <view class="menu-list">
        <view class="menu-item" @click="goToHabits">
          <view class="item-left">
            <text class="menu-icon">🌱</text>
            <text class="menu-title">习惯打卡</text>
          </view>
          <text class="item-arrow">></text>
        </view>

        <view class="menu-item" @click="showFeatureTip('收支账本')">
          <view class="item-left">
            <text class="menu-icon">💰</text>
            <text class="menu-title">收支记账</text>
          </view>
          <text class="item-arrow">></text>
        </view>

        <view class="menu-item" @click="showFeatureTip('个人物品')">
          <view class="item-left">
            <text class="menu-icon">📦</text>
            <text class="menu-title">物品与借还</text>
          </view>
          <text class="item-arrow">></text>
        </view>

        <view class="menu-item" @click="showFeatureTip('备忘录')">
          <view class="item-left">
            <text class="menu-icon">📌</text>
            <text class="menu-title">备忘录</text>
          </view>
          <text class="item-arrow">></text>
        </view>

        <view class="menu-item" @click="showFeatureTip('个人日记')">
          <view class="item-left">
            <text class="menu-icon">📔</text>
            <text class="menu-title">心情日记</text>
          </view>
          <text class="item-arrow">></text>
        </view>
      </view>
    </view>

    <!-- 退出登录按钮 / 登录引导 -->
    <view class="action-section">
      <button v-if="userStore.isLoggedIn" class="logout-btn" @click="handleLogout">
        退出登录
      </button>
      <button v-else class="life-btn-primary" @click="goToLogin">
        登录账号
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const handleProfileClick = () => {
  if (!userStore.isLoggedIn) {
    goToLogin()
  }
}

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const goToHabits = () => {
  if (!userStore.isLoggedIn) {
    goToLogin()
    return
  }
  uni.navigateTo({ url: '/pages/habits/index' })
}

const handleLogout = () => {
  uni.showModal({
    title: '退出确认',
    content: '确定要退出当前已登录账号吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    }
  })
}

const showFeatureTip = (feature: string) => {
  uni.showToast({
    title: `${feature}将在后续阶段发布`,
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.my-page {
  padding-bottom: 40rpx;
}

.profile-header {
  background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%);
  padding: 40rpx 32rpx 50rpx;
  color: #ffffff;
}

.profile-box {
  display: flex;
  align-items: center;
}

.avatar-circle {
  width: 110rpx;
  height: 110rpx;
  border-radius: 55rpx;
  background-color: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.avatar-text {
  font-size: 54rpx;
}

.user-meta {
  flex: 1;
}

.nickname {
  font-size: 34rpx;
  font-weight: 700;
  display: block;
}

.username {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 6rpx;
  display: block;
}

.arrow {
  font-size: 30rpx;
  opacity: 0.8;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f2f3f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.menu-title {
  font-size: 28rpx;
  color: #1f2d3d;
}

.item-arrow {
  color: #bfbfbf;
  font-size: 26rpx;
}

.action-section {
  padding: 40rpx 24rpx;
}

.logout-btn {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1rpx solid #ffa39e;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>

