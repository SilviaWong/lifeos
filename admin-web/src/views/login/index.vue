<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧：插画与理念文案 -->
      <div class="illustration-side">
        <div class="quote-header">
          <h1 class="main-slogan">更好的自己<br />从管理开始</h1>
          <div class="sub-slogan-wrap">
            <span class="sub-slogan">记录 · 规划 · 成长</span>
          </div>
        </div>

        <!-- 治愈系书桌生活插画 -->
        <div class="scene-illustration">
          <div class="sticky-note">
            <span class="note-text">A<br />Better<br />Me<br />◡̈</span>
          </div>
          <div class="desk-surface">
            <!-- 盆栽植物 -->
            <div class="plant-pot">
              <div class="plant-leaves">🌿</div>
              <div class="pot-body"></div>
            </div>

            <!-- 书堆与趴睡小猫 -->
            <div class="book-stack">
              <div class="cat-body">
                <span class="cat-face">🐱💤</span>
              </div>
              <div class="book-spine book-blue">PLAN</div>
              <div class="book-spine book-white">GOOD LIFE</div>
            </div>

            <!-- 笔筒与笔记本电脑 -->
            <div class="desk-laptop">
              <div class="laptop-screen">
                <span class="screen-logo"></span>
              </div>
              <div class="laptop-base"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：登录卡片 -->
      <div class="form-side">
        <div class="login-card">
          <div class="card-header">
            <div class="sprout-icon-badge">
              <span class="sprout-emoji">🌱</span>
            </div>
            <h2 class="sys-title">个人综合管理系统</h2>
            <p class="sys-sub">遇见更有条理的自己</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="form"
            :rules="rules"
            size="large"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名 (如 admin)"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入登录密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <!-- 验证码输入行 -->
            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="form.captcha"
                  placeholder="请输入验证码"
                  :prefix-icon="Key"
                  class="captcha-input"
                />
                <div class="captcha-badge" @click="generateCaptcha" title="点击刷新验证码">
                  <span class="captcha-text">{{ captchaCode }}</span>
                  <el-icon class="refresh-icon"><RefreshRight /></el-icon>
                </div>
              </div>
            </el-form-item>

            <div class="login-actions">
              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                {{ loading ? '登录验证中...' : '登录' }}
              </el-button>
            </div>

            <div class="form-footer-links">
              <span class="forgot-link" @click="handleForgot">忘记密码？</span>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key, RefreshRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaCode = ref('796V')

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const generateCaptcha = () => {
  const chars = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ'
  let res = ''
  for (let i = 0; i < 4; i++) {
    res += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaCode.value = res
}

onMounted(() => {
  generateCaptcha()
  // 预填默认体验账号
  if (!form.username) {
    form.username = 'admin'
    form.password = 'admin123'
  }
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 2, max: 64, message: '账号长度在 2 到 64 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符之间', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handleForgot = () => {
  ElMessage.info('初始管理员账号为 admin，默认密码为 admin123')
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (form.captcha.trim().toUpperCase() !== captchaCode.value.toUpperCase()) {
      ElMessage.error('验证码输入有误，请重新输入')
      generateCaptcha()
      form.captcha = ''
      return
    }

    loading.value = true
    try {
      await userStore.login({
        username: form.username.trim(),
        password: form.password
      })

      if (!userStore.isAdmin) {
        ElMessage.error('该账号非管理员角色，无权访问管理系统')
        await userStore.logout()
        return
      }

      ElMessage.success('管理员登录成功，欢迎进入控制台！')
      const redirectPath = (route.query.redirect as string) || '/dashboard'
      router.push(redirectPath)
    } catch (error: any) {
      generateCaptcha()
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 50%, #BFDBFE 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  box-sizing: border-box;
}

.login-container {
  width: 1000px;
  max-width: 95vw;
  min-height: 560px;
  display: flex;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(16px);
  border-radius: 32px;
  box-shadow: 0 20px 50px rgba(37, 99, 235, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

/* 左侧插画与理念文案 */
.illustration-side {
  flex: 1.1;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.6) 0%, rgba(219, 234, 254, 0.3) 100%);
}

.quote-header {
  position: relative;
  z-index: 2;
}
.main-slogan {
  font-size: 40px;
  font-weight: 800;
  color: #1E3A8A;
  line-height: 1.25;
  letter-spacing: 2px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
  margin: 0;
}
.sub-slogan-wrap {
  margin-top: 16px;
}
.sub-slogan {
  font-size: 16px;
  color: #3B82F6;
  letter-spacing: 3px;
  font-weight: 500;
  display: inline-block;
  padding: 4px 14px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 20px;
}

/* 场景插画 */
.scene-illustration {
  position: relative;
  height: 280px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.sticky-note {
  position: absolute;
  top: 10px;
  right: 50px;
  width: 70px;
  height: 70px;
  background-color: #FEF08A;
  box-shadow: 2px 4px 10px rgba(0, 0, 0, 0.08);
  transform: rotate(6deg);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-radius: 4px;
}
.note-text {
  font-size: 12px;
  color: #854D0E;
  font-weight: 700;
  line-height: 1.1;
}

.desk-surface {
  width: 100%;
  height: 12px;
  background: #94A3B8;
  border-radius: 6px;
  position: relative;
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
}

/* 盆栽 */
.plant-pot {
  position: absolute;
  left: 30px;
  bottom: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.plant-leaves {
  font-size: 38px;
  line-height: 1;
}
.pot-body {
  width: 32px;
  height: 26px;
  background: #E2E8F0;
  border-radius: 0 0 10px 10px;
  border-top: 2px solid #CBD5E1;
}

/* 书本与趴睡小猫 */
.book-stack {
  position: absolute;
  left: 120px;
  bottom: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cat-body {
  font-size: 48px;
  line-height: 1;
  margin-bottom: -6px;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}
.book-spine {
  width: 110px;
  height: 20px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.06);
}
.book-blue {
  background: #3B82F6;
  color: #FFFFFF;
}
.book-white {
  background: #FFFFFF;
  color: #475569;
}

/* 笔记本电脑 */
.desk-laptop {
  position: absolute;
  right: 40px;
  bottom: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.laptop-screen {
  width: 90px;
  height: 60px;
  background: #F1F5F9;
  border: 4px solid #CBD5E1;
  border-radius: 6px 6px 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}
.screen-logo {
  font-size: 14px;
  color: #94A3B8;
}
.laptop-base {
  width: 110px;
  height: 6px;
  background: #94A3B8;
  border-radius: 0 0 4px 4px;
}

/* 右侧表单卡片 */
.form-side {
  flex: 0.9;
  background: #FFFFFF;
  padding: 50px 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 100%;
  max-width: 360px;
}
.card-header {
  text-align: center;
  margin-bottom: 32px;
}
.sprout-icon-badge {
  width: 58px;
  height: 58px;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.25);
  margin-bottom: 16px;
}
.sprout-emoji {
  font-size: 30px;
}
.sys-title {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
  letter-spacing: 0.5px;
}
.sys-sub {
  font-size: 13px;
  color: #94A3B8;
  margin-top: 6px;
  letter-spacing: 1px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 14px;
  box-shadow: 0 0 0 1px #E2E8F0 inset;
  background-color: #F8FAFC;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #3B82F6 inset;
  background-color: #FFFFFF;
}

/* 验证码组合 */
.captcha-row {
  display: flex;
  width: 100%;
  gap: 12px;
}
.captcha-input {
  flex: 1;
}
.captcha-badge {
  width: 110px;
  height: 40px;
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  border: 1px solid #BFDBFE;
  border-radius: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
}
.captcha-badge:hover {
  background: #BFDBFE;
}
.captcha-text {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #1D4ED8;
  font-family: "Courier New", monospace;
}
.refresh-icon {
  font-size: 14px;
  color: #3B82F6;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.28);
  margin-top: 8px;
  transition: all 0.2s ease;
}
.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.35);
}

.form-footer-links {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
.forgot-link {
  font-size: 13px;
  color: #3B82F6;
  cursor: pointer;
  transition: color 0.15s;
}
.forgot-link:hover {
  color: #1D4ED8;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .illustration-side {
    display: none;
  }
  .login-container {
    max-width: 420px;
    min-height: auto;
  }
  .form-side {
    padding: 40px 24px;
  }
}
</style>
