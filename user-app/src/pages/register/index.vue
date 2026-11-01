<template>
  <view class="register-page">
    <view class="header-section">
      <text class="title">创建 LifeOS 新账号</text>
      <text class="subtitle">记录健康、管理目标、追踪时间与财务</text>
    </view>

    <view class="form-card">
      <view class="form-item">
        <text class="field-label">账号</text>
        <input
          v-model="form.username"
          class="input-box"
          placeholder="3-32位字母数字下划线"
          placeholder-class="placeholder"
        />
      </view>

      <view class="form-item">
        <text class="field-label">密码</text>
        <input
          v-model="form.password"
          class="input-box"
          type="password"
          placeholder="至少 6 位登录密码"
          placeholder-class="placeholder"
        />
      </view>

      <view class="form-item">
        <text class="field-label">确认密码</text>
        <input
          v-model="confirmPassword"
          class="input-box"
          type="password"
          placeholder="再次输入登录密码"
          placeholder-class="placeholder"
        />
      </view>

      <view class="form-item">
        <text class="field-label">昵称 (选填)</text>
        <input
          v-model="form.nickname"
          class="input-box"
          placeholder="展示的用户昵称"
          placeholder-class="placeholder"
        />
      </view>

      <view class="form-item">
        <text class="field-label">手机号 (选填)</text>
        <input
          v-model="form.phone"
          class="input-box"
          type="number"
          placeholder="便于找回密码"
          placeholder-class="placeholder"
        />
      </view>

      <button
        class="register-submit-btn"
        :loading="loading"
        @click="handleRegister"
      >
        {{ loading ? '注册中...' : '立即注册' }}
      </button>

      <view class="login-link-box">
        <text class="link-label">已有账号？</text>
        <text class="link-btn" @click="goToLogin">去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const confirmPassword = ref('')

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: ''
})

const handleRegister = async () => {
  if (!form.username.trim()) {
    return uni.showToast({ title: '请输入用户名', icon: 'none' })
  }
  if (!form.password || form.password.length < 6) {
    return uni.showToast({ title: '密码长度至少 6 位', icon: 'none' })
  }
  if (form.password !== confirmPassword.value) {
    return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
  }

  loading.value = true
  try {
    await userStore.register(form)
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/index' })
    }, 800)
  } catch (e: any) {
    // 错误在 request 拦截器中提示
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}
</script>

<style lang="scss" scoped>
.register-page {
  padding: 60rpx 40rpx 40rpx;
  min-height: 100vh;
  background-color: #ffffff;
}

.header-section {
  margin-bottom: 50rpx;
}

.title {
  font-size: 40rpx;
  font-weight: 700;
  color: #1f2d3d;
  display: block;
}

.subtitle {
  font-size: 26rpx;
  color: #8c8c8c;
  margin-top: 10rpx;
  display: block;
}

.form-item {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #ebeef5;
  padding: 24rpx 0;
  margin-bottom: 16rpx;
}

.field-label {
  width: 170rpx;
  font-size: 28rpx;
  color: #595959;
}

.input-box {
  flex: 1;
  font-size: 28rpx;
}

.placeholder {
  color: #bfbfbf;
}

.register-submit-btn {
  background-color: #1677ff;
  color: #ffffff;
  border-radius: 45rpx;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 32rpx;
  font-weight: 500;
  margin-top: 40rpx;
}

.register-submit-btn:active {
  background-color: #0958d9;
}

.login-link-box {
  display: flex;
  justify-content: center;
  margin-top: 36rpx;
  font-size: 26rpx;
}

.link-label {
  color: #8c8c8c;
}

.link-btn {
  color: #1677ff;
  margin-left: 8rpx;
}
</style>

