<template>
  <PageContainer title="数据看板" description="平台运行总览、活跃指标监控与多维业务图表分析">
    <!-- 顶部时间范围筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="filter-label">统计范围：</span>
          <el-radio-group v-model="quickRange" size="default" @change="handleQuickRangeChange">
            <el-radio-button label="TODAY">今日</el-radio-button>
            <el-radio-button label="LAST_7_DAYS">近 7 天</el-radio-button>
            <el-radio-button label="LAST_30_DAYS">近 30 天</el-radio-button>
            <el-radio-button label="CUSTOM">自定义</el-radio-button>
          </el-radio-group>

          <el-date-picker
            v-if="quickRange === 'CUSTOM'"
            v-model="customDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            size="default"
            class="custom-picker"
            :disabled-date="disabledDate"
            @change="handleCustomDateChange"
          />
        </div>

        <div class="filter-right">
          <span v-if="overviewData?.filter" class="date-hint">
            统计区间：{{ overviewData.filter.startDate }} ~ {{ overviewData.filter.endDate }}（共 {{ overviewData.filter.dayCount }} 天）
          </span>
          <el-button type="primary" :loading="loading" @click="fetchData">
            刷新数据
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="stat-cards" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">总注册用户</span>
            <el-tag size="small" type="primary">累计数据</el-tag>
          </div>
          <div class="stat-value">{{ formatNumber(overviewData?.overviewCards?.totalUserCount) }}</div>
          <div class="stat-footer">
            区间新增 <span class="highlight-text">+{{ overviewData?.overviewCards?.rangeNewUserCount || 0 }}</span> 人
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">区间活跃用户</span>
            <el-tag size="small" type="success">去重活跃</el-tag>
          </div>
          <div class="stat-value">{{ formatNumber(overviewData?.overviewCards?.rangeActiveUserCount) }}</div>
          <div class="stat-footer">
            今日活跃 (DAU)：<span class="highlight-text">{{ overviewData?.overviewCards?.todayActiveUserCount || 0 }}</span> 人
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">健康记录数</span>
            <el-tag size="small" type="warning">生活体征</el-tag>
          </div>
          <div class="stat-value">{{ formatNumber(overviewData?.overviewCards?.totalHealthRecordCount) }}</div>
          <div class="stat-footer">
            区间记录 <span class="highlight-text">+{{ overviewData?.overviewCards?.rangeHealthRecordCount || 0 }}</span> 条
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span class="stat-title">财务净结余</span>
            <el-tag
              size="small"
              :type="(overviewData?.overviewCards?.rangeNetBalance || 0) >= 0 ? 'success' : 'danger'"
            >
              区间汇总
            </el-tag>
          </div>
          <div
            class="stat-value"
            :class="(overviewData?.overviewCards?.rangeNetBalance || 0) >= 0 ? 'text-success' : 'text-danger'"
          >
            {{ (overviewData?.overviewCards?.rangeNetBalance || 0) >= 0 ? '+' : '' }}¥{{ formatMoney(overviewData?.overviewCards?.rangeNetBalance) }}
          </div>
          <div class="stat-footer">
            收 ¥{{ formatMoney(overviewData?.overviewCards?.rangeIncomeAmount) }} / 支 ¥{{ formatMoney(overviewData?.overviewCards?.rangeExpenseAmount) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 次要指标横栏 -->
    <el-row :gutter="16" class="sub-stat-cards">
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-box">
          <span class="mini-label">累计专注时长</span>
          <span class="mini-val">
            {{ formatHours(overviewData?.overviewCards?.totalFocusMinutes) }}
            <small class="unit">小时</small>
          </span>
          <span class="mini-sub">区间专注 {{ overviewData?.overviewCards?.rangeFocusMinutes || 0 }} 分钟</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-box">
          <span class="mini-label">累计待办任务</span>
          <span class="mini-val">{{ formatNumber(overviewData?.overviewCards?.totalTodoCount) }}</span>
          <span class="mini-sub">已完成 {{ overviewData?.overviewCards?.completedTodoCount || 0 }} 件</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-box">
          <span class="mini-label">累计习惯打卡</span>
          <span class="mini-val">{{ formatNumber(overviewData?.overviewCards?.totalHabitCheckinCount) }}</span>
          <span class="mini-sub">健康坚持日常</span>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="mini-stat-box">
          <span class="mini-label">累计财务总收支</span>
          <span class="mini-val">¥{{ formatMoney(overviewData?.overviewCards?.totalExpenseAmount) }}</span>
          <span class="mini-sub">累计收入 ¥{{ formatMoney(overviewData?.overviewCards?.totalIncomeAmount) }}</span>
        </div>
      </el-col>
    </el-row>

    <!-- 图表第 1 行：用户趋势折线图 + 模块调用趋势堆叠图 -->
    <el-row :gutter="16" class="mt-4">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📈 用户新增与日活跃趋势 (DAU)</span>
              <el-tag size="small" type="info">按日序列</el-tag>
            </div>
          </template>
          <div ref="userTrendChartRef" class="chart-container" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📊 业务模块每日使用量趋势</span>
              <el-tag size="small" type="info">堆叠面积</el-tag>
            </div>
          </template>
          <div ref="moduleTrendChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表第 2 行：模块调用占比环形图 + 健康指标类型分布柱状图 -->
    <el-row :gutter="16" class="mt-4">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>🍩 业务模块调用频次占比</span>
              <el-tag size="small" type="info">操作构成</el-tag>
            </div>
          </template>
          <div ref="modulePieChartRef" class="chart-container" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>🏃 健康体征记录类型分布</span>
              <el-tag size="small" type="info">数据分类</el-tag>
            </div>
          </template>
          <div ref="healthBarChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 指标释义与口径说明卡片 -->
    <el-card shadow="never" class="mt-4 audit-note-card">
      <template #header>
        <div class="card-header">
          <span>ℹ️ 统计指标口径与运营说明</span>
        </div>
      </template>
      <div class="note-content">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="统计时间范围">
            默认支持“今日”、“近 7 天”、“近 30 天”与“自定义日期范围”（最大跨度 90 天，以防大数据量扫描）。
          </el-descriptions-item>
          <el-descriptions-item label="活跃用户 (DAU)">
            统计区间内在业务模块（待办、番茄钟、打卡、健康、收支等）产生过关键操作日志的去重用户数。
          </el-descriptions-item>
          <el-descriptions-item label="财务净结余定义">
            严格定义为：净结余 = 收入总额 - 支出总额。本统计为个人记账统计，不冒充银行实时账户余额。
          </el-descriptions-item>
          <el-descriptions-item label="权限隔离安全机制">
            所有管理端统计接口均严格校验管理员角色（`ROLE_ADMIN` / `ROLE_SUPER_ADMIN`），普通用户无权访问。
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import PageContainer from '../../components/PageContainer.vue'
import { getDashboardOverviewApi, getModuleTrendsApi } from '../../api/dashboard'
import type { AdminDashboardOverview, AdminModuleTrend } from '../../types/dashboard'

// 筛选状态
const quickRange = ref<'TODAY' | 'LAST_7_DAYS' | 'LAST_30_DAYS' | 'CUSTOM'>('LAST_7_DAYS')
const customDateRange = ref<[string, string] | null>(null)
const loading = ref(false)

// 数据状态
const overviewData = ref<AdminDashboardOverview | null>(null)
const moduleTrendData = ref<AdminModuleTrend | null>(null)

// 图表 DOM 引用
const userTrendChartRef = ref<HTMLElement | null>(null)
const moduleTrendChartRef = ref<HTMLElement | null>(null)
const modulePieChartRef = ref<HTMLElement | null>(null)
const healthBarChartRef = ref<HTMLElement | null>(null)

// 图表实例（不使用响应式 ref，避免 proxy 干扰 echarts）
let userTrendChart: echarts.ECharts | null = null
let moduleTrendChart: echarts.ECharts | null = null
let modulePieChart: echarts.ECharts | null = null
let healthBarChart: echarts.ECharts | null = null

// 格式化辅助函数
const formatNumber = (val?: number) => {
  if (val === undefined || val === null) return '0'
  return val.toLocaleString('zh-CN')
}

const formatMoney = (val?: number) => {
  if (val === undefined || val === null) return '0.00'
  return Number(val).toFixed(2)
}

const formatHours = (minutes?: number) => {
  if (!minutes) return '0.0'
  return (minutes / 60).toFixed(1)
}

const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

// 快捷范围切换
const handleQuickRangeChange = (val: string) => {
  if (val === 'CUSTOM') {
    return
  }
  fetchData()
}

const handleCustomDateChange = () => {
  if (customDateRange.value && customDateRange.value.length === 2) {
    fetchData()
  }
}

// 计算起止日期
const getDateParams = () => {
  const today = new Date()
  const formatDate = (d: Date) => {
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  if (quickRange.value === 'TODAY') {
    const dateStr = formatDate(today)
    return { startDate: dateStr, endDate: dateStr }
  } else if (quickRange.value === 'LAST_7_DAYS') {
    const past = new Date(today.getTime() - 6 * 24 * 3600 * 1000)
    return { startDate: formatDate(past), endDate: formatDate(today) }
  } else if (quickRange.value === 'LAST_30_DAYS') {
    const past = new Date(today.getTime() - 29 * 24 * 3600 * 1000)
    return { startDate: formatDate(past), endDate: formatDate(today) }
  } else if (quickRange.value === 'CUSTOM' && customDateRange.value) {
    return { startDate: customDateRange.value[0], endDate: customDateRange.value[1] }
  }
  return undefined
}

// 拉取看板与图表数据
const fetchData = async () => {
  try {
    loading.value = true
    const params = getDateParams()
    const [overviewRes, trendRes] = await Promise.all([
      getDashboardOverviewApi(params),
      getModuleTrendsApi(params)
    ])

    overviewData.value = overviewRes.data
    moduleTrendData.value = trendRes.data

    await nextTick()
    renderAllCharts()
  } catch (error) {
    console.error('拉取看板数据失败', error)
  } finally {
    loading.value = false
  }
}

// 渲染所有图表
const renderAllCharts = () => {
  renderUserTrendChart()
  renderModuleTrendChart()
  renderModulePieChart()
  renderHealthBarChart()
}

// 1. 用户新增与活跃折线图
const renderUserTrendChart = () => {
  if (!userTrendChartRef.value) return
  if (!userTrendChart) {
    userTrendChart = echarts.init(userTrendChartRef.value)
  }

  const userTrends = overviewData.value?.userTrend || []
  const xData = userTrends.map(item => item.date)
  const newUsersData = userTrends.map(item => item.newUsers)
  const activeUsersData = userTrends.map(item => item.activeUsers)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['新增注册用户', '活跃用户 (DAU)'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '新增注册用户',
        type: 'line',
        smooth: true,
        data: newUsersData,
        itemStyle: { color: '#1677ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 119, 255, 0.3)' },
            { offset: 1, color: 'rgba(22, 119, 255, 0.02)' }
          ])
        }
      },
      {
        name: '活跃用户 (DAU)',
        type: 'line',
        smooth: true,
        data: activeUsersData,
        itemStyle: { color: '#52c41a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.02)' }
          ])
        }
      }
    ]
  }

  userTrendChart.setOption(option)
}

// 2. 模块每日使用量堆叠图
const renderModuleTrendChart = () => {
  if (!moduleTrendChartRef.value) return
  if (!moduleTrendChart) {
    moduleTrendChart = echarts.init(moduleTrendChartRef.value)
  }

  const trend = moduleTrendData.value
  const dates = trend?.dates || []
  const seriesList = trend?.series || []

  const colors = ['#1677ff', '#722ed1', '#13c2c2', '#52c41a', '#fa8c16', '#faad14']

  const echartsSeries = seriesList.map((item, idx) => ({
    name: item.moduleName,
    type: 'line' as const,
    stack: 'Total',
    smooth: true,
    areaStyle: {},
    emphasis: { focus: 'series' as const },
    itemStyle: { color: colors[idx % colors.length] },
    data: item.counts
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } }
    },
    legend: {
      data: seriesList.map(s => s.moduleName),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: echartsSeries
  }

  moduleTrendChart.setOption(option)
}

// 3. 业务模块调用占比环形图
const renderModulePieChart = () => {
  if (!modulePieChartRef.value) return
  if (!modulePieChart) {
    modulePieChart = echarts.init(modulePieChartRef.value)
  }

  const ranks = overviewData.value?.moduleUsageRank || []
  const pieData = ranks.map(item => ({
    name: item.moduleName,
    value: item.count
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} 次 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      bottom: 0
    },
    series: [
      {
        name: '模块调用次数',
        type: 'pie',
        radius: ['42%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData.length > 0 ? pieData : [{ name: '暂无操作', value: 0 }]
      }
    ]
  }

  modulePieChart.setOption(option)
}

// 4. 健康指标类型柱状图
const renderHealthBarChart = () => {
  if (!healthBarChartRef.value) return
  if (!healthBarChart) {
    healthBarChart = echarts.init(healthBarChartRef.value)
  }

  const dist = overviewData.value?.healthDistribution || []
  const xData = dist.map(item => item.recordTypeName)
  const yData = dist.map(item => item.count)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '记录条数',
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#13c2c2' },
            { offset: 1, color: '#006d75' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: yData
      }
    ]
  }

  healthBarChart.setOption(option)
}

const handleResize = () => {
  userTrendChart?.resize()
  moduleTrendChart?.resize()
  modulePieChart?.resize()
  healthBarChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  userTrendChart?.dispose()
  moduleTrendChart?.dispose()
  modulePieChart?.dispose()
  healthBarChart?.dispose()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

.custom-picker {
  width: 260px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.date-hint {
  font-size: 13px;
  color: #8c8c8c;
}

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
  font-size: 26px;
  font-weight: 700;
  color: #1f2d3d;
  margin: 12px 0 8px 0;
}

.stat-footer {
  font-size: 12px;
  color: #8c8c8c;
}

.highlight-text {
  color: #1677ff;
  font-weight: 600;
}

.text-success {
  color: #52c41a !important;
}

.text-danger {
  color: #f5222d !important;
}

.sub-stat-cards {
  margin-bottom: 16px;
}

.mini-stat-box {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}

.mini-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.mini-val {
  font-size: 20px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 4px;

  .unit {
    font-size: 12px;
    font-weight: normal;
    color: #8c8c8c;
    margin-left: 2px;
  }
}

.mini-sub {
  font-size: 11px;
  color: #bfbfbf;
}

.mt-4 {
  margin-top: 16px;
}

.chart-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.audit-note-card {
  border-radius: 8px;
}

.note-content {
  padding: 4px 0;
}
</style>
