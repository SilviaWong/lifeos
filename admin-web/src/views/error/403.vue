<template>
  <div class="error-container">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <h2 class="error-desc">访问受限：您没有权限访问此管理页面</h2>
      <p class="error-hint">仅限具备系统管理员权限的账号访问，请尝试切换账号重新登录。</p>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/login')">重新登录</el-button>
        <el-button @click="$router.push('/dashboard')">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 80px;
  font-weight: 700;
  color: #ff4d4f;
  margin: 0;
}

.error-desc {
  font-size: 20px;
  color: #262626;
  margin: 16px 0 8px 0;
}

.error-hint {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>

