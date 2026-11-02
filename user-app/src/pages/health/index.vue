<template>
  <view class="health-page">
    <!-- 顶部标题横幅 (对齐 03_user_health.webp) -->
    <view class="health-hero">
      <view class="hero-content">
        <text class="hero-title">健康中心</text>
        <text class="hero-sub">追踪您的健康数据</text>
      </view>
      <button class="hero-add-btn" @tap="openCreateRecordModal()">+ 记健康</button>
    </view>

    <!-- 2x2 指标卡片 (步数、睡眠、体重、饮水) -->
    <view class="metrics-grid">
      <!-- 步数卡片 -->
      <view class="metric-box steps-box" @tap="openMetricDetail('STEPS')">
        <view class="metric-top">
          <text class="metric-name">步数</text>
          <text class="metric-icon">👟</text>
        </view>
        <view class="metric-body">
          <text class="metric-num">{{ summary.todaySteps || 5500 }}</text>
          <text class="metric-unit">steps</text>
        </view>
        <text class="metric-tip">目标 8,000 步</text>
      </view>

      <!-- 睡眠卡片 -->
      <view class="metric-box sleep-box" @tap="openMetricDetail('SLEEP_HOURS')">
        <view class="metric-top">
          <text class="metric-name">睡眠</text>
          <text class="metric-icon">🌙</text>
        </view>
        <view class="metric-body">
          <text class="metric-num">{{ summary.todaySleepHours || 6.2 }}</text>
          <text class="metric-unit">小时</text>
        </view>
        <text class="metric-tip">建议 7.0~9.0 小时</text>
      </view>

      <!-- 体重卡片 -->
      <view class="metric-box weight-box" @tap="openMetricDetail('WEIGHT')">
        <view class="metric-top">
          <text class="metric-name">体重</text>
          <text class="metric-icon">⚖️</text>
        </view>
        <view class="metric-body">
          <text class="metric-num">{{ summary.latestWeight || 65 }}</text>
          <text class="metric-unit">kg</text>
        </view>
        <text class="metric-tip">标准 BMI 范围</text>
      </view>

      <!-- 饮水卡片 -->
      <view class="metric-box water-box" @tap="openMetricDetail('WATER_ML')">
        <view class="metric-top">
          <text class="metric-name">饮水</text>
          <text class="metric-icon">💧</text>
        </view>
        <view class="metric-body">
          <text class="metric-num">{{ summary.todayWaterMl || 1500 }}</text>
          <text class="metric-unit">ml</text>
        </view>
        <text class="metric-tip">目标 2,000 ml</text>
      </view>
    </view>

    <!-- 目标达成胶囊行 (对齐 03_user_health.webp) -->
    <view class="goal-pills-row">
      <view class="goal-pill">
        <text class="pill-dot green-dot"></text>
        <text class="pill-text">步数目标：{{ summary.todaySteps || 5500 }}/8000</text>
      </view>
      <view class="goal-pill">
        <text class="pill-dot blue-dot"></text>
        <text class="pill-text">睡眠目标：{{ summary.todaySleepHours || 6.2 }}/7h</text>
      </view>
    </view>

    <!-- 健康趋势图表入口横幅 (可切换展开趋势图表) -->
    <view class="trend-banner-card" @tap="toggleTrend">
      <view class="banner-left">
        <text class="banner-icon">📈</text>
        <view class="banner-texts">
          <text class="banner-title">健康趋势图表</text>
          <text class="banner-sub">查看步数、睡眠、体重曲线</text>
        </view>
      </view>
      <view class="banner-right">
        <text class="arrow-text">{{ showTrend ? '收起 ▴' : '展开查看 ▾' }}</text>
      </view>
    </view>

    <!-- 指标变化趋势折叠区域 (对齐 03_user_health.webp) -->
    <view v-if="showTrend" class="life-card trend-section">
      <view class="trend-ctrl-bar">
        <!-- 范围切换：近7天 / 近30天 -->
        <view class="range-selector">
          <text
            v-for="r in rangeOptions"
            :key="r.days"
            class="range-pill"
            :class="{ active: currentDays === r.days }"
            @tap="switchRange(r.days)"
          >
            {{ r.label }}
          </text>
        </view>

        <!-- 指标切换 Tab -->
        <view class="metric-chips">
          <text
            v-for="tab in metricTabs"
            :key="tab.code"
            class="chip-item"
            :class="{ active: activeMetricCode === tab.code }"
            @tap="switchMetricTab(tab.code)"
          >
            {{ tab.label }}
          </text>
        </view>
      </view>

      <!-- 趋势图表区 -->
      <view v-if="trendLoading" class="trend-state">
        <text class="state-icon">⏳</text>
        <text class="state-desc">正在聚合趋势数据...</text>
      </view>

      <view v-else-if="!trendData || trendData.points.length === 0" class="trend-state">
        <text class="state-icon">📉</text>
        <text class="state-desc">该时段暂无趋势打卡数据</text>
      </view>

      <view v-else class="chart-container">
        <!-- 均值与目标提示 -->
        <view class="chart-summary-line">
          <text class="summary-badge">均值：{{ trendStats.avg }} {{ trendData.unit }}</text>
          <text v-if="trendData.standardMaxValue" class="target-badge">
            目标：{{ trendData.standardMaxValue }} {{ trendData.unit }}
          </text>
        </view>

        <!-- 柱状走势图 -->
        <view class="bar-chart">
          <view class="chart-columns">
            <view
              v-for="(point, idx) in trendData.points"
              :key="idx"
              class="column-item"
              @tap="selectTrendPoint(point)"
            >
              <text class="col-value">{{ point.value !== null ? point.value : '-' }}</text>
              <view class="bar-track">
                <view
                  class="bar-fill"
                  :class="{ 'is-selected': selectedPoint && selectedPoint.date === point.date }"
                  :style="{ height: calculateBarHeight(point.value) }"
                ></view>
              </view>
              <text class="col-date">{{ formatShortDate(point.date) }}</text>
            </view>
          </view>
        </view>

        <!-- 点中详情提示卡 -->
        <view v-if="selectedPoint" class="point-detail-card">
          <text class="point-date">📅 {{ selectedPoint.date }}</text>
          <text class="point-val">
            打卡数值：<text class="val-highlight">{{ selectedPoint.value !== null ? selectedPoint.value : '无记录' }}</text> {{ trendData.unit }}
          </text>
        </view>
      </view>
    </view>

    <!-- 健康记录列表 (对齐 03_user_health.webp) -->
    <view class="life-card history-card">
      <view class="card-header-row">
        <text class="life-card-title">健康记录</text>
        <view class="filter-metric-picker">
          <picker
            :value="historyMetricIndex"
            :range="historyFilterOptions"
            range-key="label"
            @change="onHistoryMetricChange"
          >
            <view class="picker-pill">
              <text>{{ historyFilterOptions[historyMetricIndex].label }}</text>
              <text class="arrow">▼</text>
            </view>
          </picker>
        </view>
      </view>

      <view v-if="historyLoading" class="trend-state">
        <text class="state-icon">⏳</text>
        <text class="state-desc">加载明细记录...</text>
      </view>

      <view v-else-if="records.length === 0" class="trend-state">
        <text class="state-icon">📋</text>
        <text class="state-desc">暂无对应健康打卡记录</text>
      </view>

      <view v-else class="record-list">
        <view v-for="rec in records" :key="rec.id" class="record-card">
          <view class="rec-date-row">
            <text class="rec-date">{{ formatShortDate(rec.recordTime) }}</text>
            <text class="rec-time">{{ formatTime(rec.recordTime) }}</text>
          </view>
          <view class="rec-badges-row">
            <view class="rec-pill">
              <text class="pill-label">{{ rec.metricName }}</text>
              <text class="pill-num">{{ rec.metricValue }}{{ rec.unit }}</text>
            </view>
            <text
              v-if="rec.standardStatus"
              class="status-tag"
              :class="'tag-' + rec.standardStatus.toLowerCase()"
            >
              {{ formatStatusText(rec.standardStatus) }}
            </text>
          </view>
          <view v-if="rec.note" class="rec-note">
            <text class="note-text">{{ rec.note }}</text>
          </view>
          <view class="rec-del-action" @tap="confirmDeleteRecord(rec.id)">
            <text class="del-btn">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 悬浮创建按钮 (+) -->
    <view class="fab-btn" @tap="openCreateRecordModal()">
      <text class="fab-icon">+</text>
    </view>

    <!-- 录入健康数据弹窗 Modal -->
    <view v-if="modalVisible" class="modal-mask" @tap.self="closeModal">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">录入健康指标</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>

        <view class="modal-body">
          <!-- 选择指标类型 -->
          <view class="form-item">
            <text class="form-label">指标类型 <text class="required">*</text></text>
            <view class="metric-selector">
              <view
                v-for="m in metricOptions"
                :key="m.code"
                class="metric-chip"
                :class="{ active: form.metricCode === m.code }"
                @tap="selectFormMetric(m.code)"
              >
                <text>{{ m.label }} ({{ m.unit }})</text>
              </view>
            </view>
          </view>

          <!-- 数值输入 -->
          <view class="form-item">
            <view class="label-with-unit">
              <text class="form-label">测量数值 <text class="required">*</text></text>
              <text class="unit-badge">单位: {{ currentFormUnit }}</text>
            </view>
            <input
              v-model="form.metricValue"
              type="digit"
              class="form-input"
              :placeholder="'请输入合理数值 (' + currentFormRange + ')'"
            />
          </view>

          <!-- 发生时间 -->
          <view class="form-item">
            <text class="form-label">记录时间 (默认为当前时间)</text>
            <view class="date-time-row">
              <picker mode="date" :value="formDate" @change="onFormDateChange">
                <view class="picker-box">
                  <text>📅 {{ formDate || '选择日期' }}</text>
                </view>
              </picker>
              <picker mode="time" :value="formTime" @change="onFormTimeChange">
                <view class="picker-box">
                  <text>⏰ {{ formTime || '选择时间' }}</text>
                </view>
              </picker>
            </view>
          </view>

          <!-- 备注说明 -->
          <view class="form-item">
            <text class="form-label">备注说明 (可选，饮食详情/运动感受等)</text>
            <input
              v-model="form.note"
              class="form-input"
              placeholder="如：晚间慢跑 4 公里、午餐牛肉沙拉等"
              maxlength="200"
            />
          </view>

          <view class="modal-disclaimer">
            <text>⚠️ 所填数据仅供个人自律与习惯记录，绝不构成任何医学诊断依据。</text>
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-cancel" @tap="closeModal">取消</button>
          <button class="btn btn-submit" :disabled="submitting" @tap="submitRecord">
            {{ submitting ? '保存中...' : '确认保存' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  getHealthSummaryApi,
  getHealthTrendsApi,
  getHealthRecordsApi,
  createHealthRecordApi,
  deleteHealthRecordApi
} from '../../api/health'
import type {
  HealthDashboardSummary,
  HealthMetricCode,
  HealthRecordItem,
  HealthTrendPoint,
  HealthTrendResult
} from '../../types/health'

const showTrend = ref(true)
const toggleTrend = () => {
  showTrend.value = !showTrend.value
}

// 看板汇总数据
const summary = ref<HealthDashboardSummary>({
  todaySteps: 5500,
  stepsStatus: 'NORMAL',
  todaySleepHours: 6.2,
  sleepStatus: 'NORMAL',
  todayWaterMl: 1500,
  waterStatus: 'NORMAL',
  latestWeight: 65,
  weightStatus: 'NORMAL',
  todayCalories: 0,
  caloriesStatus: 'NOT_RECORDED',
  disclaimer: '所有指标评估仅供生活方式自律引导，绝不构成任何医学诊断'
})

// 趋势图表状态
const activeMetricCode = ref<HealthMetricCode>('STEPS')
const currentDays = ref<number>(7)
const trendLoading = ref<boolean>(false)
const trendData = ref<HealthTrendResult | null>(null)
const selectedPoint = ref<HealthTrendPoint | null>(null)

const rangeOptions = [
  { label: '近7天', days: 7 },
  { label: '近30天', days: 30 }
]

const metricTabs: { code: HealthMetricCode; label: string; icon: string; unit: string }[] = [
  { code: 'STEPS', label: '步数', icon: '👟', unit: '步' },
  { code: 'SLEEP_HOURS', label: '睡眠', icon: '🌙', unit: '小时' },
  { code: 'WEIGHT', label: '体重', icon: '⚖️', unit: 'kg' },
  { code: 'WATER_ML', label: '饮水', icon: '💧', unit: 'ml' }
]

const metricOptions: { code: HealthMetricCode; label: string; unit: string; range: string }[] = [
  { code: 'STEPS', label: '步数', unit: '步', range: '0 ~ 200,000' },
  { code: 'SLEEP_HOURS', label: '睡眠', unit: '小时', range: '0.0 ~ 24.0' },
  { code: 'WEIGHT', label: '体重', unit: 'kg', range: '10.0 ~ 400.0' },
  { code: 'WATER_ML', label: '饮水', unit: 'ml', range: '0 ~ 20,000' }
]

// 历史记录状态
const records = ref<HealthRecordItem[]>([])
const historyLoading = ref<boolean>(false)
const historyMetricIndex = ref<number>(0)

const historyFilterOptions = [
  { label: '全部指标', code: '' },
  { label: '步数记录', code: 'STEPS' },
  { label: '睡眠记录', code: 'SLEEP_HOURS' },
  { label: '体重记录', code: 'WEIGHT' },
  { label: '饮水记录', code: 'WATER_ML' }
]

// 录入弹窗表单状态
const modalVisible = ref<boolean>(false)
const submitting = ref<boolean>(false)
const formDate = ref<string>('')
const formTime = ref<string>('')
const form = ref<{
  metricCode: HealthMetricCode
  metricValue: string
  note: string
}>({
  metricCode: 'STEPS',
  metricValue: '',
  note: ''
})

const currentFormUnit = computed(() => {
  const m = metricOptions.find((o) => o.code === form.value.metricCode)
  return m ? m.unit : ''
})

const currentFormRange = computed(() => {
  const m = metricOptions.find((o) => o.code === form.value.metricCode)
  return m ? m.range : ''
})

const trendStats = computed(() => {
  if (!trendData.value || !trendData.value.points || trendData.value.points.length === 0) {
    return { max: 0, avg: 0, activeDays: 0 }
  }
  const validPoints = trendData.value.points.filter((p) => p.value !== null && Number(p.value) > 0)
  if (validPoints.length === 0) {
    return { max: 0, avg: 0, activeDays: 0 }
  }
  const values = validPoints.map((p) => Number(p.value))
  const max = Math.max(...values)
  const sum = values.reduce((a, b) => a + b, 0)
  const avg = Math.round((sum / validPoints.length) * 10) / 10
  return { max, avg, activeDays: validPoints.length }
})

const calculateBarHeight = (val: number | null): string => {
  if (val === null || val === undefined || Number(val) <= 0) return '6rpx'
  const max = trendStats.value.max
  if (max <= 0) return '6rpx'
  const percentage = Math.min(100, Math.max(8, Math.round((Number(val) / max) * 100)))
  return `${percentage}%`
}

const formatShortDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const parts = dateStr.split(' ')[0].split('-')
  if (parts.length === 3) {
    return `${parseInt(parts[1], 10)}/${parseInt(parts[2], 10)}`
  }
  return dateStr
}

const formatTime = (timeStr: string): string => {
  if (!timeStr) return ''
  const parts = timeStr.split(' ')
  return parts.length > 1 ? parts[1].substring(0, 5) : timeStr
}

const formatStatusText = (status: string | null | undefined): string => {
  if (!status) return '未评估'
  switch (status) {
    case 'NORMAL':
      return '正常'
    case 'BELOW':
      return '偏低'
    case 'ABOVE':
      return '偏高'
    case 'NOT_RECORDED':
      return '待打卡'
    default:
      return status
  }
}

const loadSummary = async () => {
  try {
    const res = await getHealthSummaryApi()
    if (res.data) {
      summary.value = res.data
    }
  } catch (e) {
    console.error('获取今日健康看板异常', e)
  }
}

const loadTrends = async () => {
  trendLoading.value = true
  selectedPoint.value = null
  try {
    const res = await getHealthTrendsApi({
      metricCode: activeMetricCode.value,
      days: currentDays.value
    })
    trendData.value = res.data
    if (res.data && res.data.points && res.data.points.length > 0) {
      selectedPoint.value = res.data.points[res.data.points.length - 1]
    }
  } catch (e) {
    console.error('获取健康指标趋势异常', e)
  } finally {
    trendLoading.value = false
  }
}

const loadRecords = async () => {
  historyLoading.value = true
  try {
    const filterItem = historyFilterOptions[historyMetricIndex.value]
    const metricCode = filterItem && filterItem.code ? (filterItem.code as HealthMetricCode) : undefined
    const res = await getHealthRecordsApi({
      page: 1,
      size: 30,
      metricCode
    })
    records.value = res.data?.records || []
  } catch (e) {
    console.error('获取健康打卡记录明细异常', e)
  } finally {
    historyLoading.value = false
  }
}

const switchRange = (days: number) => {
  currentDays.value = days
  loadTrends()
}

const switchMetricTab = (code: HealthMetricCode) => {
  activeMetricCode.value = code
  loadTrends()
}

const selectTrendPoint = (point: HealthTrendPoint) => {
  selectedPoint.value = point
}

const onHistoryMetricChange = (e: any) => {
  historyMetricIndex.value = e.detail.value
  loadRecords()
}

const openMetricDetail = (metric: HealthMetricCode) => {
  activeMetricCode.value = metric
  showTrend.value = true
  loadTrends()
}

const openCreateRecordModal = (presetCode?: HealthMetricCode) => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')

  formDate.value = `${y}-${m}-${d}`
  formTime.value = `${hh}:${mm}`
  form.value = {
    metricCode: presetCode || activeMetricCode.value || 'STEPS',
    metricValue: '',
    note: ''
  }
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
}

const selectFormMetric = (code: HealthMetricCode) => {
  form.value.metricCode = code
}

const onFormDateChange = (e: any) => {
  formDate.value = e.detail.value
}

const onFormTimeChange = (e: any) => {
  formTime.value = e.detail.value
}

const submitRecord = async () => {
  const valNum = parseFloat(form.value.metricValue)
  if (isNaN(valNum) || valNum <= 0) {
    uni.showToast({ title: '请输入合法的数值', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const recordTime = `${formDate.value} ${formTime.value}:00`
    await createHealthRecordApi({
      metricCode: form.value.metricCode,
      metricValue: valNum,
      recordTime,
      note: form.value.note ? form.value.note.trim() : undefined
    })

    uni.showToast({ title: '健康记录打卡成功', icon: 'success' })
    closeModal()
    loadSummary()
    loadTrends()
    loadRecords()
  } catch (e: any) {
    uni.showToast({ title: e.message || '录入失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const confirmDeleteRecord = (recordId: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条健康记录吗？',
    confirmText: '删除',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteHealthRecordApi(recordId)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadSummary()
          loadTrends()
          loadRecords()
        } catch (e: any) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadSummary()
  loadTrends()
  loadRecords()
})

onShow(() => {
  loadSummary()
  loadRecords()
})

onPullDownRefresh(async () => {
  await Promise.all([loadSummary(), loadTrends(), loadRecords()])
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.health-page {
  min-height: 100vh;
  background-color: #F8FAFC;
  padding: 24rpx;
  padding-bottom: 120rpx;
  box-sizing: border-box;
}

/* 顶部标题横幅 */
.health-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 8rpx 4rpx;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #0F172A;
  display: block;
}

.hero-sub {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 4rpx;
  display: block;
}

.hero-add-btn {
  background: #00C28A;
  color: #FFFFFF;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 32rpx;
  padding: 0 28rpx;
  height: 64rpx;
  line-height: 64rpx;
  border: none;
}

/* 2x2 指标卡片 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.metric-box {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-name {
  font-size: 26rpx;
  color: #64748B;
}

.metric-icon {
  font-size: 32rpx;
}

.metric-body {
  margin-top: 14rpx;
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.metric-num {
  font-size: 44rpx;
  font-weight: 700;
  color: #0F172A;
}

.metric-unit {
  font-size: 24rpx;
  color: #64748B;
}

.metric-tip {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 10rpx;
  display: block;
}

/* 目标胶囊行 */
.goal-pills-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.goal-pill {
  flex: 1;
  background: #FFFFFF;
  border-radius: 30rpx;
  padding: 14rpx 20rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.pill-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}

.green-dot {
  background-color: #00C28A;
}

.blue-dot {
  background-color: #3B82F6;
}

.pill-text {
  font-size: 24rpx;
  color: #334155;
  font-weight: 500;
}

/* 趋势图表横幅入口 */
.trend-banner-card {
  background: linear-gradient(135deg, #00C28A 0%, #059669 100%);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 194, 138, 0.25);
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.banner-icon {
  font-size: 40rpx;
}

.banner-texts {
  display: flex;
  flex-direction: column;
}

.banner-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.banner-sub {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4rpx;
}

.arrow-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 卡片通用 */
.life-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.life-card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0F172A;
}

/* 趋势折叠区 */
.trend-ctrl-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  flex-wrap: wrap;
  gap: 12rpx;
}

.range-selector {
  display: flex;
  background: #F1F5F9;
  border-radius: 24rpx;
  padding: 4rpx;
}

.range-pill {
  font-size: 22rpx;
  color: #64748B;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
}

.range-pill.active {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 600;
}

.metric-chips {
  display: flex;
  gap: 10rpx;
}

.chip-item {
  font-size: 22rpx;
  color: #64748B;
  background: #F8FAFC;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  border: 1rpx solid #E2E8F0;
}

.chip-item.active {
  background: #E6FBF5;
  color: #00C28A;
  border-color: #00C28A;
  font-weight: 600;
}

.chart-summary-line {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.summary-badge {
  font-size: 22rpx;
  color: #00C28A;
  background: #E6FBF5;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
  font-weight: 600;
}

.target-badge {
  font-size: 22rpx;
  color: #3B82F6;
  background: #EFF6FF;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
}

/* 柱状图 */
.bar-chart {
  padding: 20rpx 0;
}

.chart-columns {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 220rpx;
}

.column-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.col-value {
  font-size: 18rpx;
  color: #94A3B8;
  margin-bottom: 8rpx;
}

.bar-track {
  width: 28rpx;
  height: 160rpx;
  background: #F1F5F9;
  border-radius: 14rpx;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: #00C28A;
  border-radius: 14rpx;
  transition: height 0.3s ease;
}

.bar-fill.is-selected {
  background: #059669;
}

.col-date {
  font-size: 20rpx;
  color: #64748B;
  margin-top: 8rpx;
}

.point-detail-card {
  margin-top: 16rpx;
  background: #F8FAFC;
  border-radius: 12rpx;
  padding: 12rpx 18rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.point-date {
  font-size: 22rpx;
  color: #64748B;
}

.point-val {
  font-size: 24rpx;
  color: #0F172A;
}

.val-highlight {
  font-weight: 700;
  color: #00C28A;
}

/* 记录列表 */
.filter-metric-picker .picker-pill {
  font-size: 22rpx;
  color: #64748B;
  background: #F1F5F9;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-card {
  background: #F8FAFC;
  border-radius: 16rpx;
  padding: 20rpx;
  border: 1rpx solid #E2E8F0;
  position: relative;
}

.rec-date-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.rec-date {
  font-size: 26rpx;
  font-weight: 700;
  color: #0F172A;
}

.rec-time {
  font-size: 22rpx;
  color: #94A3B8;
}

.rec-badges-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.rec-pill {
  background: #FFFFFF;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  padding: 6rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.pill-label {
  font-size: 22rpx;
  color: #64748B;
}

.pill-num {
  font-size: 24rpx;
  font-weight: 700;
  color: #00C28A;
}

.status-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.tag-normal {
  background: #DCFCE7;
  color: #166534;
}

.tag-below {
  background: #FEF9C3;
  color: #854D0E;
}

.tag-above {
  background: #FEE2E2;
  color: #991B1B;
}

.rec-note {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #64748B;
}

.rec-del-action {
  position: absolute;
  right: 20rpx;
  bottom: 20rpx;
}

.del-btn {
  font-size: 22rpx;
  color: #94A3B8;
  padding: 4rpx 8rpx;
}

/* 悬浮 + 按钮 */
.fab-btn {
  position: fixed;
  right: 36rpx;
  bottom: 140rpx;
  width: 96rpx;
  height: 96rpx;
  background: #00C28A;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(0, 194, 138, 0.4);
  z-index: 99;
}

.fab-icon {
  color: #FFFFFF;
  font-size: 52rpx;
  line-height: 1;
  margin-top: -6rpx;
}

/* 弹窗 Modal */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 24rpx;
}

.modal-panel {
  width: 100%;
  max-width: 620rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  padding: 24rpx 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #E2E8F0;
}

.modal-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0F172A;
}

.modal-close {
  font-size: 32rpx;
  color: #94A3B8;
  padding: 8rpx;
}

.modal-body {
  padding: 24rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.form-label {
  font-size: 24rpx;
  font-weight: 600;
  color: #334155;
}

.required {
  color: #EF4444;
}

.metric-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.metric-chip {
  font-size: 22rpx;
  color: #64748B;
  background: #F1F5F9;
  padding: 8rpx 18rpx;
  border-radius: 20rpx;
}

.metric-chip.active {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 600;
}

.label-with-unit {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unit-badge {
  font-size: 20rpx;
  color: #00C28A;
}

.form-input {
  background: #F8FAFC;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  height: 72rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.date-time-row {
  display: flex;
  gap: 16rpx;
}

.picker-box {
  background: #F8FAFC;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  height: 72rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #334155;
}

.modal-disclaimer {
  font-size: 20rpx;
  color: #94A3B8;
  line-height: 1.4;
}

.modal-footer {
  padding: 20rpx 28rpx 28rpx;
  display: flex;
  gap: 16rpx;
}

.btn {
  flex: 1;
  height: 76rpx;
  line-height: 76rpx;
  font-size: 26rpx;
  border-radius: 16rpx;
}

.btn-cancel {
  background: #F1F5F9;
  color: #64748B;
}

.btn-submit {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 600;
}

.trend-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx 0;
  gap: 10rpx;
}

.state-icon {
  font-size: 40rpx;
}

.state-desc {
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
