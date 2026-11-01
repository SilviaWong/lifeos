<template>
  <div class="login-wrapper">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-circle">🌿</div>
        <h2 class="login-title">LifeOS 管理控制台</h2>
        <p class="login-subtitle">个人综合生活管理系统 · 运维与运营中心</p>
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
            placeholder="管理员账号 / 手机号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="管理员登录密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <div class="login-actions">
          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录验证中...' : '安全登录' }}
          </el-button>
        </div>
      </el-form>

      <div class="login-footer">
        <span>© 2026 LifeOS. 仅限已授权管理员访问</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref<boolean>(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入管理员账号或手机号', trigger: 'blur' },
    { min: 3, max: 32, message: '账号长度在 3 到 32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('管理员认证成功，欢迎进入控制台')
      const redirect = (route.query.redirect as string) || '/dashboard'
      router.push(redirect)
    } catch (err: any) {
      ElMessage.error(err.message || '登录认证失败，请检查账号密码与角色')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0b1a30 0%, #1e3a5f 100%);
  position: relative;
  overflow: hidden;
}

.login-box {
  width: 400px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
  padding: 36px 32px;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-circle {
  font-size: 36px;
  margin-bottom: 8px;
}

.login-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
  margin: 0 0 6px 0;
}

.login-subtitle {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.login-form {
  margin-top: 10px;
}

.login-actions {
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
  font-size: 15px;
  font-weight: 500;
  border-radius: 6px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: #bfbfbf;
}
</style>

