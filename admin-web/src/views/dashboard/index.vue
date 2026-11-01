<template>
  <PageContainer title="数据看板" description="平台运行总览与关键运营指标监控">
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">总注册用户</span>
            <el-tag size="small" type="primary">实时</el-tag>
          </div>
          <div class="stat-value">1,280</div>
          <div class="stat-footer">较上周 <span class="trend-up">+12.5% ↑</span></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">今日活跃用户 (DAU)</span>
            <el-tag size="small" type="success">日活</el-tag>
          </div>
          <div class="stat-value">342</div>
          <div class="stat-footer">较昨日 <span class="trend-up">+4.8% ↑</span></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">今日完成待办数</span>
            <el-tag size="small" type="warning">执行</el-tag>
          </div>
          <div class="stat-value">856</div>
          <div class="stat-footer">待办完成率 78.4%</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">累计专注时长</span>
            <el-tag size="small" type="info">番茄钟</el-tag>
          </div>
          <div class="stat-value">1,420 <span class="stat-unit">小时</span></div>
          <div class="stat-footer">平均单次专注 25 分钟</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>模块趋势与图表总览 (阶段 11 ECharts 集成)</span>
        </div>
      </template>
      <div class="chart-placeholder">
        <el-empty description="ECharts 图表分析模块将在阶段 11 接入真实聚合统计数据" />
      </div>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '../../components/PageContainer.vue'
</script>

<style scoped>
.stat-cards {
  margin-bottom: 8px;
}

.stat-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #8c8c8c;
  font-size: 13px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2d3d;
  margin: 12px 0 8px 0;
}

.stat-unit {
  font-size: 14px;
  font-weight: normal;
  color: #8c8c8c;
}

.stat-footer {
  font-size: 12px;
  color: #8c8c8c;
}

.trend-up {
  color: #52c41a;
  font-weight: 500;
}

.mt-4 {
  margin-top: 16px;
}

.card-header {
  font-weight: 600;
  font-size: 15px;
}

.chart-placeholder {
  min-height: 260px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>

