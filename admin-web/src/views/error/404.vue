<template>
  <div class="error-container">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-desc">抱歉，您访问的页面不存在</h2>
      <p class="error-hint">请检查网址输入是否正确，或点击下方按钮返回系统首页。</p>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
        <el-button @click="$router.back()">返回上一页</el-button>
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
  color: #1677ff;
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

