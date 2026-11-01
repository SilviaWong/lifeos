<template>
  <div class="page-container">
    <el-card shadow="never" class="page-card">
      <div v-if="title || $slots.extra" class="page-header">
        <div class="page-title">
          <span class="title-text">{{ title }}</span>
          <span v-if="description" class="title-desc">{{ description }}</span>
        </div>
        <div class="page-actions">
          <slot name="extra" />
        </div>
      </div>
      <div class="page-content">
        <slot />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<style scoped>
.page-container {
  padding: 16px;
  min-height: calc(100vh - 120px);
}

.page-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #f2f3f5;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.title-desc {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}

.page-actions {
  display: flex;
  gap: 10px;
}
</style>

