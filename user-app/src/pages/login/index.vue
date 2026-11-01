<template>
  <view class="login-page">
    <view class="brand-section">
      <text class="brand-logo">🌿</text>
      <text class="brand-name">LifeOS</text>
      <text class="brand-desc">掌控生活节奏 · 记录自律足迹</text>
    </view>

    <view class="form-card">
      <view class="form-item">
        <text class="input-icon">👤</text>
        <input
          v-model="form.username"
          class="input-box"
          placeholder="请输入用户名或手机号"
          placeholder-class="placeholder"
        />
      </view>

      <view class="form-item">
        <text class="input-icon">🔒</text>
        <input
          v-model="form.password"
          class="input-box"
          type="password"
          placeholder="请输入登录密码"
          placeholder-class="placeholder"
        />
      </view>

      <button
        class="login-submit-btn"
        :loading="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '立即登录' }}
      </button>

      <view class="form-footer">
        <text class="link-text" @click="goToRegister">没有账号？立即注册</text>
        <text class="link-text" @click="goHome">暂不登录，回首页</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username.trim()) {
    return uni.showToast({ title: '请输入用户名或手机号', icon: 'none' })
  }
  if (!form.password) {
    return uni.showToast({ title: '请输入登录密码', icon: 'none' })
  }

  loading.value = true
  try {
    await userStore.login(form)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 500)
  } catch (e: any) {
    // 错误信息在 request 中已有提示
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  uni.navigateTo({ url: '/pages/register/index' })
}

const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.login-page {
  padding: 80rpx 40rpx 40rpx;
  min-height: 100vh;
  background-color: #ffffff;
}

.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 70rpx;
}

.brand-logo {
  font-size: 80rpx;
}

.brand-name {
  font-size: 44rpx;
  font-weight: 700;
  color: #1f2d3d;
  margin-top: 10rpx;
}

.brand-desc {
  font-size: 26rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
}

.form-card {
  display: flex;
  flex-direction: column;
}

.form-item {
  display: flex;
  align-items: center;
  background-color: #f7f8fa;
  border-radius: 40rpx;
  padding: 0 30rpx;
  height: 90rpx;
  margin-bottom: 30rpx;
}

.input-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.input-box {
  flex: 1;
  font-size: 28rpx;
}

.placeholder {
  color: #bfbfbf;
}

.login-submit-btn {
  background-color: #1677ff;
  color: #ffffff;
  border-radius: 45rpx;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 32rpx;
  font-weight: 500;
  margin-top: 20rpx;
}

.login-submit-btn:active {
  background-color: #0958d9;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 36rpx;
  padding: 0 10rpx;
}

.link-text {
  font-size: 26rpx;
  color: #1677ff;
}
</style>

