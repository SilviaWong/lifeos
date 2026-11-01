<template>
  <view class="health-page">
    <!-- 非医疗免责声明横幅 -->
    <view class="disclaimer-banner">
      <text class="banner-icon">ℹ️</text>
      <text class="banner-text">所有指标评估仅供生活方式自律引导，绝不构成任何医学诊断依据</text>
    </view>

    <!-- 今日健康数据概览卡片 -->
    <view class="life-card summary-card">
      <view class="card-header-row">
        <text class="life-card-title">今日健康看板</text>
        <button class="quick-log-btn" @tap="openCreateRecordModal()">+ 快速打卡</button>
      </view>

      <view class="metric-grid">
        <!-- 今日步数 -->
        <view class="metric-card" @tap="openMetricDetail('STEPS')">
          <view class="metric-top">
            <text class="metric-name">今日步数</text>
            <text class="status-tag" :class="'tag-' + (summary.stepsStatus ? summary.stepsStatus.toLowerCase() : 'unknown')">
              {{ formatStatusText(summary.stepsStatus) }}
            </text>
          </view>
          <view class="metric-body">
            <text class="metric-num">{{ summary.todaySteps || 0 }}</text>
            <text class="metric-unit">步</text>
          </view>
          <view class="metric-footer">
            <text class="metric-ref">参考: 6000 ~ 12000 步</text>
            <text class="metric-add-link" @tap.stop="openCreateRecordModal('STEPS')">+ 记步数</text>
          </view>
        </view>

        <!-- 昨晚/今日睡眠 -->
        <view class="metric-card" @tap="openMetricDetail('SLEEP_HOURS')">
          <view class="metric-top">
            <text class="metric-name">睡眠时长</text>
            <text class="status-tag" :class="'tag-' + (summary.sleepStatus ? summary.sleepStatus.toLowerCase() : 'unknown')">
              {{ formatStatusText(summary.sleepStatus) }}
            </text>
          </view>
          <view class="metric-body">
            <text class="metric-num">{{ summary.todaySleepHours || 0 }}</text>
            <text class="metric-unit">小时</text>
          </view>
          <view class="metric-footer">
            <text class="metric-ref">参考: 7.0 ~ 9.0 小时</text>
            <text class="metric-add-link" @tap.stop="openCreateRecordModal('SLEEP_HOURS')">+ 记睡眠</text>
          </view>
        </view>

        <!-- 饮水量 -->
        <view class="metric-card" @tap="openMetricDetail('WATER_ML')">
          <view class="metric-top">
            <text class="metric-name">今日饮水</text>
            <text class="status-tag" :class="'tag-' + (summary.waterStatus ? summary.waterStatus.toLowerCase() : 'unknown')">
              {{ formatStatusText(summary.waterStatus) }}
            </text>
          </view>
          <view class="metric-body">
            <text class="metric-num">{{ summary.todayWaterMl || 0 }}</text>
            <text class="metric-unit">ml</text>
          </view>
          <view class="metric-footer">
            <text class="metric-ref">参考: 1500 ~ 2500 ml</text>
            <text class="metric-add-link" @tap.stop="openCreateRecordModal('WATER_ML')">+ 记饮水</text>
          </view>
        </view>

        <!-- 最新体重 -->
        <view class="metric-card" @tap="openMetricDetail('WEIGHT')">
          <view class="metric-top">
            <text class="metric-name">最新体重</text>
            <text class="status-tag" :class="'tag-' + (summary.weightStatus ? summary.weightStatus.toLowerCase() : 'unknown')">
              {{ formatStatusText(summary.weightStatus) }}
            </text>
          </view>
          <view class="metric-body">
            <text class="metric-num">{{ summary.latestWeight !== null ? summary.latestWeight : '--' }}</text>
            <text class="metric-unit">kg</text>
          </view>
          <view class="metric-footer">
            <text class="metric-ref">参考: 45.0 ~ 85.0 kg</text>
            <text class="metric-add-link" @tap.stop="openCreateRecordModal('WEIGHT')">+ 称重</text>
          </view>
        </view>

        <!-- 饮食摄入热量 -->
        <view class="metric-card metric-card-wide" @tap="openMetricDetail('CALORIES')">
          <view class="metric-top">
            <text class="metric-name">饮食摄入能量</text>
            <text class="status-tag" :class="'tag-' + (summary.caloriesStatus ? summary.caloriesStatus.toLowerCase() : 'unknown')">
              {{ formatStatusText(summary.caloriesStatus) }}
            </text>
          </view>
          <view class="metric-body">
            <text class="metric-num">{{ summary.todayCalories || 0 }}</text>
            <text class="metric-unit">kcal</text>
          </view>
          <view class="metric-footer">
            <text class="metric-ref">生活参考建议: 1600 ~ 2400 kcal</text>
            <text class="metric-add-link" @tap.stop="openCreateRecordModal('CALORIES')">+ 记饮食</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 指标变化趋势图表卡片 -->
    <view class="life-card trend-card">
      <view class="trend-header">
        <text class="life-card-title">指标变化趋势</text>
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
      </view>

      <!-- 指标类型切换 Tabs -->
      <scroll-view class="metric-tabs" scroll-x :show-scrollbar="false">
        <view class="tabs-track">
          <view
            v-for="tab in metricTabs"
            :key="tab.code"
            class="tab-pill"
            :class="{ active: activeMetricCode === tab.code }"
            @tap="switchMetricTab(tab.code)"
          >
            <text class="tab-icon">{{ tab.icon }}</text>
            <text class="tab-name">{{ tab.label }}</text>
          </view>
        </view>
      </scroll-view>

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
        <!-- 参考基准线提示 -->
        <view v-if="trendData.standardMinValue || trendData.standardMaxValue" class="reference-banner">
          <text class="ref-icon">🎯</text>
          <text class="ref-text">
            生活参考推荐区间:
            {{ trendData.standardMinValue !== null ? trendData.standardMinValue : '无' }} ~
            {{ trendData.standardMaxValue !== null ? trendData.standardMaxValue : '无' }}
            {{ trendData.unit }} (非医疗诊断)
          </text>
        </view>

        <!-- 柱状图展示区 -->
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
          <view class="point-row">
            <text class="point-date">📅 {{ selectedPoint.date }}</text>
            <text class="point-badge">打卡 {{ selectedPoint.recordCount }} 次</text>
          </view>
          <text class="point-val">
            聚合结果:
            <text class="val-highlight">{{ selectedPoint.value !== null ? selectedPoint.value : '无记录' }}</text>
            {{ trendData.unit }}
            <text class="method-text">({{ trendData.aggregateMethod === 'SUM' ? '当日总和' : '最新测量' }})</text>
          </text>
        </view>

        <!-- 底部聚合统计摘要 -->
        <view class="trend-summary-row">
          <view class="t-sum-item">
            <text class="t-sum-label">最高值</text>
            <text class="t-sum-val">{{ trendStats.max }} {{ trendData.unit }}</text>
          </view>
          <view class="t-sum-item">
            <text class="t-sum-label">平均值</text>
            <text class="t-sum-val">{{ trendStats.avg }} {{ trendData.unit }}</text>
          </view>
          <view class="t-sum-item">
            <text class="t-sum-label">打卡天数</text>
            <text class="t-sum-val">{{ trendStats.activeDays }} / {{ trendData.points.length }} 天</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 近期健康记录明细卡片 -->
    <view class="life-card history-card">
      <view class="card-header-row">
        <text class="life-card-title">最近打卡明细</text>
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
        <view v-for="rec in records" :key="rec.id" class="record-item">
          <view class="rec-main">
            <view class="rec-header">
              <text class="rec-name">{{ rec.metricName }}</text>
              <text class="rec-time">{{ formatTime(rec.recordTime) }}</text>
            </view>
            <view class="rec-value-row">
              <text class="rec-num">{{ rec.metricValue }}</text>
              <text class="rec-unit">{{ rec.unit }}</text>
              <text
                v-if="rec.standardStatus"
                class="status-tag"
                :class="'tag-' + rec.standardStatus.toLowerCase()"
              >
                {{ formatStatusText(rec.standardStatus) }}
              </text>
            </view>
            <text v-if="rec.note" class="rec-note">备注: {{ rec.note }}</text>
            <text v-if="rec.standardHint" class="rec-hint">{{ rec.standardHint }}</text>
          </view>
          <view class="rec-action" @tap="confirmDeleteRecord(rec.id)">
            <text class="del-btn">✕</text>
          </view>
        </view>
      </view>
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

          <!-- 非医疗说明提示 -->
          <view class="modal-disclaimer">
            <text>⚠️ 所填数据仅供个人自律与习惯记录，LifeOS 不提供医学建议与诊疗依据。</text>
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

// --- 看板汇总数据 ---
const summary = ref<HealthDashboardSummary>({
  todaySteps: 0,
  stepsStatus: 'NOT_RECORDED',
  todaySleepHours: 0,
  sleepStatus: 'NOT_RECORDED',
  todayWaterMl: 0,
  waterStatus: 'NOT_RECORDED',
  latestWeight: null,
  weightStatus: 'NOT_RECORDED',
  todayCalories: 0,
  caloriesStatus: 'NOT_RECORDED',
  disclaimer: '所有指标评估仅供生活方式自律引导，绝不构成任何医学诊断'
})

// --- 趋势图表状态 ---
const activeMetricCode = ref<HealthMetricCode>('STEPS')
const currentDays = ref<number>(7)
const trendLoading = ref<boolean>(false)
const trendData = ref<HealthTrendResult | null>(null)
const selectedPoint = ref<HealthTrendPoint | null>(null)

const rangeOptions = [
  { label: '近 7 天', days: 7 },
  { label: '近 14 天', days: 14 },
  { label: '近 30 天', days: 30 }
]

const metricTabs: { code: HealthMetricCode; label: string; icon: string; unit: string }[] = [
  { code: 'STEPS', label: '步数', icon: '🚶‍♂️', unit: '步' },
  { code: 'SLEEP_HOURS', label: '睡眠', icon: '🌙', unit: '小时' },
  { code: 'WEIGHT', label: '体重', icon: '⚖️', unit: 'kg' },
  { code: 'WATER_ML', label: '饮水', icon: '💧', unit: 'ml' },
  { code: 'CALORIES', label: '饮食热量', icon: '🥗', unit: 'kcal' }
]

const metricOptions: { code: HealthMetricCode; label: string; unit: string; range: string }[] = [
  { code: 'STEPS', label: '步数', unit: '步', range: '0 ~ 200,000' },
  { code: 'SLEEP_HOURS', label: '睡眠', unit: '小时', range: '0.0 ~ 24.0' },
  { code: 'WEIGHT', label: '体重', unit: 'kg', range: '10.0 ~ 400.0' },
  { code: 'WATER_ML', label: '饮水', unit: 'ml', range: '0 ~ 20,000' },
  { code: 'CALORIES', label: '饮食摄入', unit: 'kcal', range: '0 ~ 20,000' }
]

// --- 历史记录状态 ---
const records = ref<HealthRecordItem[]>([])
const historyLoading = ref<boolean>(false)
const historyMetricIndex = ref<number>(0)

const historyFilterOptions = [
  { label: '全部指标', code: '' },
  { label: '步数记录', code: 'STEPS' },
  { label: '睡眠记录', code: 'SLEEP_HOURS' },
  { label: '体重记录', code: 'WEIGHT' },
  { label: '饮水记录', code: 'WATER_ML' },
  { label: '饮食记录', code: 'CALORIES' }
]

// --- 录入弹窗表单状态 ---
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
  const m = metricOptions.find(o => o.code === form.value.metricCode)
  return m ? m.unit : ''
})

const currentFormRange = computed(() => {
  const m = metricOptions.find(o => o.code === form.value.metricCode)
  return m ? m.range : ''
})

// --- 趋势统计计算 ---
const trendStats = computed(() => {
  if (!trendData.value || !trendData.value.points || trendData.value.points.length === 0) {
    return { max: 0, avg: 0, activeDays: 0 }
  }
  const validPoints = trendData.value.points.filter(p => p.value !== null && Number(p.value) > 0)
  if (validPoints.length === 0) {
    return { max: 0, avg: 0, activeDays: 0 }
  }
  const values = validPoints.map(p => Number(p.value))
  const max = Math.max(...values)
  const sum = values.reduce((a, b) => a + b, 0)
  const avg = Math.round((sum / validPoints.length) * 10) / 10
  return {
    max,
    avg,
    activeDays: validPoints.length
  }
})

// --- 柱状图高度自适应计算 ---
const calculateBarHeight = (val: number | null): string => {
  if (val === null || val <= 0) {
    return '4rpx'
  }
  const maxVal = Math.max(trendStats.value.max, 1)
  const percent = Math.min(100, Math.max(8, Math.round((Number(val) / maxVal) * 100)))
  return `${percent}%`
}

// --- 格式化辅助方法 ---
const formatStatusText = (status?: string): string => {
  if (!status) return '未设定'
  switch (status.toUpperCase()) {
    case 'NORMAL':
      return '达标'
    case 'BELOW':
      return '偏低'
    case 'ABOVE':
      return '偏高'
    case 'NOT_RECORDED':
      return '未记录'
    default:
      return '正常'
  }
}

const formatShortDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  if (parts.length >= 3) {
    return `${parts[1]}.${parts[2]}`
  }
  return dateStr
}

const formatTime = (timeStr: string): string => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// --- 业务交互逻辑 ---
const loadDashboardSummary = async () => {
  try {
    const res = await getHealthSummaryApi()
    if (res.code === 0 && res.data) {
      summary.value = res.data
    }
  } catch (e) {
    console.error('加载健康看板失败', e)
  }
}

const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - (currentDays.value - 1))

    const pad = (n: number) => (n < 10 ? '0' + n : String(n))
    const formatDate = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

    const res = await getHealthTrendsApi({
      metricCode: activeMetricCode.value,
      startDate: formatDate(startDate),
      endDate: formatDate(endDate),
      period: 'DAY'
    })

    if (res.code === 0 && res.data) {
      trendData.value = res.data
      if (res.data.points && res.data.points.length > 0) {
        selectedPoint.value = res.data.points[res.data.points.length - 1]
      }
    }
  } catch (e) {
    console.error('加载趋势失败', e)
  } finally {
    trendLoading.value = false
  }
}

const loadRecentRecords = async () => {
  historyLoading.value = true
  try {
    const selectedMetric = historyFilterOptions[historyMetricIndex.value].code
    const res = await getHealthRecordsApi({
      pageNum: 1,
      pageSize: 15,
      metricCode: selectedMetric || undefined
    })
    if (res.code === 0 && res.data) {
      records.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载历史记录失败', e)
  } finally {
    historyLoading.value = false
  }
}

const switchMetricTab = (code: HealthMetricCode) => {
  activeMetricCode.value = code
  loadTrendData()
}

const switchRange = (days: number) => {
  currentDays.value = days
  loadTrendData()
}

const selectTrendPoint = (point: HealthTrendPoint) => {
  selectedPoint.value = point
}

const openMetricDetail = (code: HealthMetricCode) => {
  activeMetricCode.value = code
  loadTrendData()
}

const onHistoryMetricChange = (e: any) => {
  historyMetricIndex.value = e.detail.value
  loadRecentRecords()
}

// --- 录入弹窗 ---
const openCreateRecordModal = (defaultMetric?: HealthMetricCode) => {
  const now = new Date()
  const pad = (n: number) => (n < 10 ? '0' + n : String(n))
  formDate.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  formTime.value = `${pad(now.getHours())}:${pad(now.getMinutes())}`

  form.value = {
    metricCode: defaultMetric || activeMetricCode.value || 'STEPS',
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
  if (!form.value.metricValue || isNaN(Number(form.value.metricValue))) {
    uni.showToast({ title: '请输入有效的数值', icon: 'none' })
    return
  }

  const val = Number(form.value.metricValue)
  if (val < 0) {
    uni.showToast({ title: '数值不能为负数', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const recordTime = `${formDate.value} ${formTime.value}:00`
    const res = await createHealthRecordApi({
      metricCode: form.value.metricCode,
      metricValue: val,
      recordTime,
      note: form.value.note ? form.value.note.trim() : undefined
    })

    if (res.code === 0) {
      uni.showToast({ title: '记录已保存', icon: 'success' })
      closeModal()
      // 联动刷新看板、趋势和明细
      loadDashboardSummary()
      if (activeMetricCode.value === form.value.metricCode) {
        loadTrendData()
      }
      loadRecentRecords()
    } else {
      uni.showToast({ title: res.message || '保存失败', icon: 'none' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存异常', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const confirmDeleteRecord = (id: number) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条健康记录吗？删除后不可恢复。',
    confirmText: '删除',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          const delRes = await deleteHealthRecordApi(id)
          if (delRes.code === 0) {
            uni.showToast({ title: '已删除', icon: 'success' })
            loadDashboardSummary()
            loadTrendData()
            loadRecentRecords()
          }
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 刷新全量数据
const refreshAll = async () => {
  await Promise.all([loadDashboardSummary(), loadTrendData(), loadRecentRecords()])
}

onMounted(() => {
  refreshAll()
})

onShow(() => {
  loadDashboardSummary()
})

onPullDownRefresh(async () => {
  await refreshAll()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.health-page {
  padding-bottom: 40rpx;
}

/* 免责声明提示横幅 */
.disclaimer-banner {
  margin: 16rpx 24rpx 0;
  background-color: #fffbe6;
  border: 1rpx solid #ffe58f;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  display: flex;
  align-items: center;
}

.banner-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.banner-text {
  font-size: 22rpx;
  color: #d46b08;
  line-height: 1.4;
}

/* 顶部与卡片公共 */
.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.quick-log-btn {
  background-color: #1677ff;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 500;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  line-height: 1.6;
}

/* 今日看板 Grid */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.metric-card {
  background-color: #f7f8fa;
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  transition: transform 0.1s ease;
}

.metric-card:active {
  transform: scale(0.98);
}

.metric-card-wide {
  grid-column: span 2;
}

.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-name {
  font-size: 26rpx;
  color: #595959;
  font-weight: 500;
}

/* 达标状态 Tag */
.status-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.tag-normal {
  background-color: #e6f7ff;
  color: #1677ff;
}

.tag-below {
  background-color: #fff7e6;
  color: #fa8c16;
}

.tag-above {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.tag-not_recorded,
.tag-unknown {
  background-color: #f0f0f0;
  color: #8c8c8c;
}

.metric-body {
  margin: 16rpx 0 10rpx;
  display: flex;
  align-items: baseline;
}

.metric-num {
  font-size: 42rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.metric-unit {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-left: 8rpx;
}

.metric-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.metric-ref {
  font-size: 20rpx;
  color: #8c8c8c;
}

.metric-add-link {
  font-size: 22rpx;
  color: #1677ff;
  font-weight: 500;
}

/* 趋势卡片 */
.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.range-selector {
  display: flex;
  background-color: #f0f2f5;
  border-radius: 24rpx;
  padding: 4rpx;
}

.range-pill {
  font-size: 22rpx;
  color: #595959;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.range-pill.active {
  background-color: #ffffff;
  color: #1677ff;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.metric-tabs {
  white-space: nowrap;
  margin-bottom: 24rpx;
}

.tabs-track {
  display: inline-flex;
  gap: 16rpx;
  padding: 4rpx 0;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  padding: 10rpx 24rpx;
  background-color: #f7f8fa;
  border: 1rpx solid #ebeef5;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #595959;
}

.tab-pill.active {
  background-color: #e6f4ff;
  border-color: #1677ff;
  color: #1677ff;
  font-weight: 600;
}

.tab-icon {
  margin-right: 8rpx;
  font-size: 24rpx;
}

.trend-state {
  padding: 60rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c8c8c;
}

.state-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.state-desc {
  font-size: 26rpx;
}

/* 柱状图展示 */
.reference-banner {
  background-color: #f6ffed;
  border: 1rpx solid #b7eb8f;
  border-radius: 10rpx;
  padding: 10rpx 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.ref-icon {
  font-size: 24rpx;
  margin-right: 10rpx;
}

.ref-text {
  font-size: 22rpx;
  color: #389e0d;
}

.bar-chart {
  padding: 20rpx 0 10rpx;
  overflow-x: auto;
}

.chart-columns {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  min-height: 280rpx;
  gap: 12rpx;
  padding-bottom: 10rpx;
}

.column-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 50rpx;
}

.col-value {
  font-size: 18rpx;
  color: #8c8c8c;
  margin-bottom: 8rpx;
  white-space: nowrap;
}

.bar-track {
  width: 24rpx;
  height: 200rpx;
  background-color: #f0f2f5;
  border-radius: 12rpx;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #69b1ff 0%, #1677ff 100%);
  border-radius: 12rpx;
  transition: height 0.3s ease;
}

.bar-fill.is-selected {
  background: linear-gradient(180deg, #ff7a45 0%, #fa541c 100%);
}

.col-date {
  font-size: 20rpx;
  color: #8c8c8c;
  margin-top: 12rpx;
  white-space: nowrap;
}

/* 单点高亮卡片 */
.point-detail-card {
  margin-top: 20rpx;
  background-color: #f7f8fa;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
}

.point-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.point-date {
  font-size: 24rpx;
  color: #595959;
  font-weight: 500;
}

.point-badge {
  font-size: 20rpx;
  background-color: #e6f4ff;
  color: #1677ff;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.point-val {
  font-size: 26rpx;
  color: #1f2d3d;
}

.val-highlight {
  font-weight: 700;
  color: #1677ff;
  margin: 0 6rpx;
}

.method-text {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-left: 8rpx;
}

/* 底部趋势统计 */
.trend-summary-row {
  display: flex;
  justify-content: space-around;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.t-sum-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.t-sum-label {
  font-size: 22rpx;
  color: #8c8c8c;
}

.t-sum-val {
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2d3d;
  margin-top: 6rpx;
}

/* 历史明细卡片 */
.picker-pill {
  display: flex;
  align-items: center;
  background-color: #f7f8fa;
  padding: 8rpx 18rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #595959;
}

.arrow {
  font-size: 18rpx;
  margin-left: 8rpx;
  color: #8c8c8c;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f7f8fa;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
}

.rec-main {
  flex: 1;
}

.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.rec-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.rec-time {
  font-size: 22rpx;
  color: #8c8c8c;
}

.rec-value-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.rec-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #1677ff;
}

.rec-unit {
  font-size: 22rpx;
  color: #8c8c8c;
}

.rec-note {
  font-size: 22rpx;
  color: #595959;
  margin-top: 6rpx;
  display: block;
}

.rec-hint {
  font-size: 20rpx;
  color: #52c41a;
  margin-top: 4rpx;
  display: block;
}

.del-btn {
  font-size: 30rpx;
  color: #bfbfbf;
  padding: 10rpx 16rpx;
}

.del-btn:active {
  color: #ff4d4f;
}

/* 录入弹窗 Modal */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 999;
}

.modal-panel {
  background-color: #ffffff;
  width: 100%;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx 30rpx 50rpx;
  max-height: 85vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.modal-close {
  font-size: 36rpx;
  color: #8c8c8c;
  padding: 6rpx 16rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  font-size: 26rpx;
  font-weight: 500;
  color: #1f2d3d;
  margin-bottom: 12rpx;
  display: block;
}

.required {
  color: #ff4d4f;
}

.label-with-unit {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unit-badge {
  font-size: 22rpx;
  color: #1677ff;
  background-color: #e6f4ff;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.form-input {
  width: 100%;
  height: 76rpx;
  background-color: #f7f8fa;
  border: 1rpx solid #ebeef5;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #1f2d3d;
}

.metric-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.metric-chip {
  padding: 10rpx 20rpx;
  background-color: #f7f8fa;
  border: 1rpx solid #ebeef5;
  border-radius: 24rpx;
  font-size: 24rpx;
  color: #595959;
}

.metric-chip.active {
  background-color: #e6f4ff;
  border-color: #1677ff;
  color: #1677ff;
  font-weight: 600;
}

.date-time-row {
  display: flex;
  gap: 16rpx;
}

.picker-box {
  flex: 1;
  background-color: #f7f8fa;
  border: 1rpx solid #ebeef5;
  border-radius: 12rpx;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #1f2d3d;
}

.modal-disclaimer {
  background-color: #fafafa;
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  font-size: 20rpx;
  color: #8c8c8c;
  line-height: 1.4;
  margin-top: 10rpx;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 36rpx;
}

.btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-cancel {
  background-color: #f0f2f5;
  color: #595959;
}

.btn-submit {
  background-color: #1677ff;
  color: #ffffff;
  font-weight: 600;
}

.btn-submit[disabled] {
  opacity: 0.6;
}
</style>
