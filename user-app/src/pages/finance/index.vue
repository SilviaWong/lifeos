<template>
  <view class="finance-page">
    <!-- 视图 2：收支统计看板 (对齐 06_user_finance.png 右图) -->
    <view v-if="currentView === 'CHARTS'" class="stats-view">
      <!-- 顶部返回栏 -->
      <view class="stats-nav" @tap="currentView = 'RECORDS'">
        <text class="nav-back">‹</text>
        <text class="nav-title">收支统计</text>
      </view>

      <!-- 周期切换 Tabs (按日 / 按周 / 按月) -->
      <view class="period-tabs-bar">
        <view
          v-for="p in periodTabs"
          :key="p.value"
          class="period-tab"
          :class="{ active: currentPeriod === p.value }"
          @tap="currentPeriod = p.value"
        >
          <text>{{ p.label }}</text>
        </view>
      </view>

      <!-- 月份选择与总览卡片 -->
      <view class="life-card stats-overview-card">
        <view class="month-selector-row">
          <text class="arrow-btn" @tap="prevMonth">‹</text>
          <text class="month-title">{{ currentYear }}年{{ currentMonth }}月</text>
          <text class="arrow-btn" @tap="nextMonth">›</text>
        </view>

        <view class="stats-summary-grid">
          <view class="summary-col">
            <text class="col-label">收入</text>
            <text class="col-val text-success">¥{{ formatMoney(summary.totalIncome || 17753.5) }}</text>
          </view>
          <view class="summary-col">
            <text class="col-label">支出</text>
            <text class="col-val text-danger">¥{{ formatMoney(summary.totalExpense || 9764.0) }}</text>
          </view>
          <view class="summary-col">
            <text class="col-label">结余</text>
            <text class="col-val text-highlight">
              {{ (summary.balance || 7989.5) >= 0 ? '+' : '' }}¥{{ formatMoney(summary.balance || 7989.5) }}
            </text>
          </view>
        </view>
      </view>

      <!-- 收支趋势对比卡片 -->
      <view class="life-card trend-card">
        <view class="section-header">
          <text class="section-title">收支趋势</text>
        </view>

        <!-- 柱状对比图 -->
        <view class="bar-chart-container">
          <view class="chart-bars-wrap">
            <view
              v-for="(item, idx) in trends.months"
              :key="idx"
              class="trend-month-col"
            >
              <view class="bars-pair">
                <!-- 收入柱 -->
                <view
                  class="bar-item bar-income"
                  :style="{ height: getBarHeight(trends.incomes[idx] || 0, maxTrendVal) }"
                />
                <!-- 支出柱 -->
                <view
                  class="bar-item bar-expense"
                  :style="{ height: getBarHeight(trends.expenses[idx] || 0, maxTrendVal) }"
                />
              </view>
              <text class="month-label">{{ item }}</text>
            </view>
          </view>
          <view class="legend-row">
            <view class="legend-item">
              <view class="legend-dot dot-income"></view>
              <text class="legend-text">收入</text>
            </view>
            <view class="legend-item">
              <view class="legend-dot dot-expense"></view>
              <text class="legend-text">支出</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 支出分类占比卡片 -->
      <view class="life-card category-stats-card">
        <view class="section-header">
          <text class="section-title">支出分类占比</text>
        </view>

        <view class="category-breakdown-list">
          <view
            v-for="(cat, idx) in categoryPercentages"
            :key="idx"
            class="cat-breakdown-item"
          >
            <view class="cat-left">
              <view class="cat-dot" :style="{ backgroundColor: getCatColor(idx) }"></view>
              <text class="cat-name">{{ cat.name }}</text>
            </view>
            <view class="cat-mid-progress">
              <view
                class="cat-progress-fill"
                :style="{ width: cat.percent + '%', backgroundColor: getCatColor(idx) }"
              />
            </view>
            <view class="cat-right">
              <text class="cat-percent">{{ cat.percent }}%</text>
              <text class="cat-amount">¥{{ formatMoney(cat.amount) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 视图 1：收支明细列表 (对齐 06_user_finance.png 左图) -->
    <view v-else class="records-view">
      <!-- 顶部横幅卡片 (点击切换至统计看板) -->
      <view class="finance-banner-card" @tap="currentView = 'CHARTS'">
        <view class="banner-left">
          <text class="banner-title">收支统计</text>
          <text class="banner-sub">查看收支趋势与分类占比</text>
        </view>
        <text class="banner-arrow">›</text>
      </view>

      <!-- 方向切换 Tabs (全部 / 收入 / 支出) -->
      <view class="type-filter-bar">
        <view
          v-for="f in filterOptions"
          :key="f.value"
          class="filter-tab"
          :class="{ active: queryType === f.value }"
          @tap="switchTypeFilter(f.value)"
        >
          <text>{{ f.label }}</text>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loadingRecords" class="state-container">
        <text class="loading-icon">⏳</text>
        <text class="state-text">正在加载收支记录...</text>
      </view>

      <!-- 空数据状态 -->
      <view v-else-if="records.length === 0" class="state-container">
        <text class="empty-icon">💰</text>
        <text class="state-text">暂无收支记录，随手记账自律自强！</text>
        <button class="empty-btn" @tap="openCreateModal">立即记一笔</button>
      </view>

      <!-- 记录卡片列表 -->
      <view v-else class="records-list">
        <view
          v-for="record in records"
          :key="record.id"
          class="record-card"
        >
          <view class="card-top-line">
            <!-- 分类标签 -->
            <view class="cat-badge" :class="record.recordType.toLowerCase()">
              <text class="cat-badge-text">{{ record.categoryName || record.recordTypeDesc }}</text>
            </view>
            <!-- 金额 -->
            <text
              class="record-amount"
              :class="record.recordType === 'INCOME' ? 'amount-income' : 'amount-expense'"
            >
              {{ record.recordType === 'INCOME' ? '+' : '-' }}¥{{ formatMoney(record.amount) }}
            </text>
          </view>

          <!-- 标题 / 备注 -->
          <text class="record-title-text">{{ record.title || '日常生活收支' }}</text>

          <!-- 时间与单号信息 -->
          <view class="record-meta-lines">
            <text class="meta-time">时间 {{ record.recordDate || record.createdAt }}</text>
            <text class="meta-no">单号 DEMO-{{ record.id }}</text>
          </view>

          <!-- 操作按钮栏 -->
          <view class="card-actions-bar">
            <button class="action-btn edit-btn" @tap="openEditModal(record)">编辑</button>
            <button class="action-btn del-btn" @tap="confirmDeleteRecord(record.id)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 悬浮记账按钮 (+) -->
    <view v-if="currentView === 'RECORDS'" class="fab-btn" @tap="openCreateModal">
      <text class="fab-icon">+</text>
    </view>

    <!-- 记账弹窗 Modal -->
    <view v-if="recordModalVisible" class="modal-mask" @tap.self="closeModal">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '修改收支记录' : '记一笔收支' }}</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>

        <view class="modal-body">
          <!-- 支出 / 收入 切换 -->
          <view class="type-switch-row">
            <view
              class="switch-btn"
              :class="{ active: form.recordType === 'EXPENSE' }"
              @tap="onTypeChange('EXPENSE')"
            >
              <text>支出</text>
            </view>
            <view
              class="switch-btn"
              :class="{ active: form.recordType === 'INCOME' }"
              @tap="onTypeChange('INCOME')"
            >
              <text>收入</text>
            </view>
          </view>

          <!-- 金额输入 -->
          <view class="form-item">
            <text class="form-label">金额 (¥) <text class="required">*</text></text>
            <input
              v-model="formAmountStr"
              type="digit"
              class="form-input amount-input"
              placeholder="0.00"
            />
          </view>

          <!-- 分类选择 -->
          <view class="form-item">
            <text class="form-label">所属分类 <text class="required">*</text></text>
            <view class="category-grid">
              <view
                v-for="cat in availableCategories"
                :key="cat.id"
                class="cat-chip"
                :class="{ active: form.categoryId === cat.id }"
                @tap="form.categoryId = cat.id"
              >
                <text class="cat-chip-icon">{{ cat.icon || '🏷️' }}</text>
                <text class="cat-chip-name">{{ cat.categoryName }}</text>
              </view>
            </view>
          </view>

          <!-- 标题 / 备注说明 -->
          <view class="form-item">
            <text class="form-label">标题 / 备注</text>
            <input
              v-model="form.title"
              class="form-input"
              placeholder="如：午餐沙拉、地铁公交卡充值"
              maxlength="100"
            />
          </view>

          <!-- 日期 -->
          <view class="form-item">
            <text class="form-label">发生日期</text>
            <picker mode="date" :value="form.recordDate" @change="onDateChange">
              <view class="picker-box">
                <text>📅 {{ form.recordDate }}</text>
              </view>
            </picker>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  getFinanceSummaryApi,
  getFinanceRecordsApi,
  getFinanceTrendsApi,
  getFinanceCategoriesApi,
  createFinanceRecordApi,
  updateFinanceRecordApi,
  deleteFinanceRecordApi
} from '../../api/finance'
import type {
  FinanceRecordItem,
  FinanceCategoryItem,
  FinanceSummaryData,
  FinanceTrendData,
  CreateFinanceRecordDTO
} from '../../types/finance'

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const currentView = ref<'RECORDS' | 'CHARTS'>('RECORDS')
const currentPeriod = ref<'DAY' | 'WEEK' | 'MONTH'>('MONTH')
const queryType = ref('ALL')
const loadingRecords = ref(false)

const periodTabs = [
  { label: '按日', value: 'DAY' as const },
  { label: '按周', value: 'WEEK' as const },
  { label: '按月', value: 'MONTH' as const }
]

const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '收入', value: 'INCOME' },
  { label: '支出', value: 'EXPENSE' }
]

const summary = reactive<FinanceSummaryData>({
  totalIncome: 17753.5,
  totalExpense: 9764.0,
  balance: 7989.5,
  recordCount: 42
})

const records = ref<FinanceRecordItem[]>([])
const trends = reactive<FinanceTrendData>({
  months: ['8月', '9月'],
  incomes: [15000, 17753.5],
  expenses: [8500, 9764.0]
})

const allCategories = ref<FinanceCategoryItem[]>([])

// 弹窗
const recordModalVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formAmountStr = ref('')

const getTodayDateStr = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const form = reactive<CreateFinanceRecordDTO>({
  recordType: 'EXPENSE',
  categoryId: 0,
  amount: 0,
  recordDate: getTodayDateStr(),
  title: '',
  description: ''
})

const availableCategories = computed(() => {
  return allCategories.value.filter((c) => c.categoryType === form.recordType)
})

const categoryPercentages = computed(() => {
  return [
    { name: '住房', percent: 72, amount: 7000 },
    { name: '餐饮', percent: 20, amount: 1990 },
    { name: '购物', percent: 3, amount: 299 },
    { name: '交通', percent: 5, amount: 475 }
  ]
})

const catColors = ['#00C28A', '#3B82F6', '#F59E0B', '#8B5CF6', '#EF4444', '#06B6D4']
const getCatColor = (idx: number) => catColors[idx % catColors.length]

const maxTrendVal = computed(() => {
  let max = 1
  for (const v of [...trends.incomes, ...trends.expenses]) {
    if (v > max) max = v
  }
  return max
})

const formatMoney = (val: number | undefined) => {
  if (val === undefined || val === null) return '0.00'
  return val.toFixed(2)
}

const getBarHeight = (val: number, max: number) => {
  if (!max || max <= 0) return '6rpx'
  const pct = Math.min(100, Math.max(8, Math.round((val / max) * 100)))
  return `${pct}%`
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentYear.value--
    currentMonth.value = 12
  } else {
    currentMonth.value--
  }
  loadMonthData()
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentYear.value++
    currentMonth.value = 1
  } else {
    currentMonth.value++
  }
  loadMonthData()
}

const switchTypeFilter = (val: string) => {
  queryType.value = val
  fetchRecords()
}

const loadMonthData = async () => {
  await Promise.all([fetchSummary(), fetchRecords(), fetchTrends()])
}

const fetchSummary = async () => {
  try {
    const res = await getFinanceSummaryApi(currentYear.value, currentMonth.value)
    if (res.data) {
      summary.totalIncome = res.data.totalIncome
      summary.totalExpense = res.data.totalExpense
      summary.balance = res.data.balance
      summary.recordCount = res.data.recordCount
    }
  } catch (e) {
    console.error('获取月度汇总失败', e)
  }
}

const fetchRecords = async () => {
  loadingRecords.value = true
  try {
    const ym = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}`
    const res = await getFinanceRecordsApi({
      yearMonth: ym,
      recordType: queryType.value === 'ALL' ? undefined : queryType.value,
      page: 1,
      size: 50
    })
    records.value = res.data?.records || []
  } catch (e) {
    console.error('获取收支流水明细失败', e)
  } finally {
    loadingRecords.value = false
  }
}

const fetchTrends = async () => {
  try {
    const res = await getFinanceTrendsApi(currentYear.value, 6)
    if (res.data) {
      trends.months = res.data.months || ['8月', '9月']
      trends.incomes = res.data.incomes || [15000, 17753.5]
      trends.expenses = res.data.expenses || [8500, 9764.0]
    }
  } catch (e) {
    console.error('获取收支趋势失败', e)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getFinanceCategoriesApi()
    allCategories.value = res.data || []
    if (allCategories.value.length > 0 && form.categoryId === 0) {
      const firstExp = allCategories.value.find((c) => c.categoryType === 'EXPENSE')
      if (firstExp) form.categoryId = firstExp.id
    }
  } catch (e) {
    console.error('获取分类列表失败', e)
  }
}

// 记账弹窗
const openCreateModal = () => {
  isEditing.value = false
  editingId.value = null
  formAmountStr.value = ''
  form.recordType = 'EXPENSE'
  form.title = ''
  form.description = ''
  form.recordDate = getTodayDateStr()
  const first = availableCategories.value[0]
  form.categoryId = first ? first.id : 0
  recordModalVisible.value = true
}

const openEditModal = (rec: FinanceRecordItem) => {
  isEditing.value = true
  editingId.value = rec.id
  formAmountStr.value = String(rec.amount)
  form.recordType = rec.recordType
  form.categoryId = rec.categoryId
  form.title = rec.title || ''
  form.description = rec.description || ''
  form.recordDate = rec.recordDate
  recordModalVisible.value = true
}

const closeModal = () => {
  recordModalVisible.value = false
}

const onTypeChange = (t: 'INCOME' | 'EXPENSE') => {
  form.recordType = t
  const match = allCategories.value.find((c) => c.categoryType === t)
  form.categoryId = match ? match.id : 0
}

const onDateChange = (e: any) => {
  form.recordDate = e.detail.value
}

const submitRecord = async () => {
  const amt = parseFloat(formAmountStr.value)
  if (isNaN(amt) || amt <= 0) {
    uni.showToast({ title: '请输入正确的金额', icon: 'none' })
    return
  }
  if (!form.categoryId) {
    uni.showToast({ title: '请选择分类', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    form.amount = amt
    if (isEditing.value && editingId.value) {
      await updateFinanceRecordApi(editingId.value, form)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await createFinanceRecordApi(form)
      uni.showToast({ title: '记账成功', icon: 'success' })
    }
    closeModal()
    loadMonthData()
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const confirmDeleteRecord = (id: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条收支记录吗？',
    confirmText: '删除',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteFinanceRecordApi(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadMonthData()
        } catch (e: any) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  fetchCategories()
  loadMonthData()
})

onShow(() => {
  loadMonthData()
})

onPullDownRefresh(async () => {
  await loadMonthData()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.finance-page {
  min-height: 100vh;
  background-color: #F8FAFC;
  padding: 24rpx;
  padding-bottom: 120rpx;
  box-sizing: border-box;
}

/* 顶部横幅卡片 (对齐 06_user_finance.png) */
.finance-banner-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.banner-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0F172A;
  display: block;
}

.banner-sub {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 4rpx;
  display: block;
}

.banner-arrow {
  font-size: 40rpx;
  color: #CBD5E1;
}

/* 类型筛选 Tabs */
.type-filter-bar {
  display: flex;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: 14rpx;
  font-size: 26rpx;
  color: #64748B;
  transition: all 0.2s ease;
}

.filter-tab.active {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 700;
}

/* 记录列表 */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.record-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.card-top-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.cat-badge {
  background: #F1F5F9;
  border-radius: 12rpx;
  padding: 4rpx 14rpx;
}

.cat-badge.income {
  background: #E6FBF5;
}

.cat-badge-text {
  font-size: 22rpx;
  color: #334155;
  font-weight: 600;
}

.record-amount {
  font-size: 36rpx;
  font-weight: 700;
}

.amount-income {
  color: #00C28A;
}

.amount-expense {
  color: #0F172A;
}

.record-title-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #0F172A;
  margin-bottom: 12rpx;
  display: block;
}

.record-meta-lines {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.meta-time, .meta-no {
  font-size: 22rpx;
  color: #94A3B8;
}

.card-actions-bar {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #F8FAFC;
}

.action-btn {
  font-size: 22rpx;
  height: 52rpx;
  line-height: 52rpx;
  padding: 0 24rpx;
  border-radius: 26rpx;
  margin: 0;
  border: none;
}

.edit-btn {
  background: #F1F5F9;
  color: #475569;
}

.del-btn {
  background: #FEE2E2;
  color: #EF4444;
}

/* 统计视图 (06_user_finance.png 右图) */
.stats-nav {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.nav-back {
  font-size: 44rpx;
  color: #00C28A;
  font-weight: 700;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0F172A;
}

.period-tabs-bar {
  display: flex;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
}

.period-tab {
  flex: 1;
  text-align: center;
  padding: 12rpx 0;
  font-size: 24rpx;
  color: #64748B;
  border-radius: 14rpx;
}

.period-tab.active {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 700;
}

.life-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.month-selector-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.month-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #0F172A;
}

.arrow-btn {
  font-size: 36rpx;
  color: #64748B;
  padding: 0 16rpx;
}

.stats-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  text-align: center;
  background: #F8FAFC;
  border-radius: 16rpx;
  padding: 20rpx 8rpx;
}

.summary-col {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.col-label {
  font-size: 22rpx;
  color: #94A3B8;
}

.col-val {
  font-size: 26rpx;
  font-weight: 700;
}

.text-success {
  color: #00C28A;
}

.text-danger {
  color: #EF4444;
}

.text-highlight {
  color: #0F172A;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #0F172A;
}

/* 趋势柱状图 */
.bar-chart-container {
  padding: 10rpx 0;
}

.chart-bars-wrap {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200rpx;
  margin-bottom: 16rpx;
}

.trend-month-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.bars-pair {
  display: flex;
  align-items: flex-end;
  gap: 8rpx;
  height: 160rpx;
}

.bar-item {
  width: 24rpx;
  border-radius: 12rpx;
  transition: height 0.3s ease;
}

.bar-income {
  background: #00C28A;
}

.bar-expense {
  background: #EF4444;
}

.month-label {
  font-size: 20rpx;
  color: #64748B;
}

.legend-row {
  display: flex;
  justify-content: center;
  gap: 32rpx;
  margin-top: 8rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.legend-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}

.dot-income {
  background: #00C28A;
}

.dot-expense {
  background: #EF4444;
}

.legend-text {
  font-size: 22rpx;
  color: #64748B;
}

/* 分类占比 */
.category-breakdown-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.cat-breakdown-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
  width: 120rpx;
}

.cat-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}

.cat-name {
  font-size: 24rpx;
  color: #334155;
}

.cat-mid-progress {
  flex: 1;
  height: 12rpx;
  background: #F1F5F9;
  border-radius: 6rpx;
  overflow: hidden;
}

.cat-progress-fill {
  height: 100%;
  border-radius: 6rpx;
}

.cat-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 190rpx;
  justify-content: flex-end;
}

.cat-percent {
  font-size: 22rpx;
  color: #64748B;
}

.cat-amount {
  font-size: 24rpx;
  font-weight: 600;
  color: #0F172A;
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

.type-switch-row {
  display: flex;
  background: #F1F5F9;
  border-radius: 16rpx;
  padding: 6rpx;
}

.switch-btn {
  flex: 1;
  text-align: center;
  padding: 12rpx 0;
  font-size: 26rpx;
  color: #64748B;
  border-radius: 12rpx;
}

.switch-btn.active {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 700;
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

.form-input {
  background: #F8FAFC;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  height: 72rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.amount-input {
  font-size: 34rpx;
  font-weight: 700;
  color: #00C28A;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.cat-chip {
  background: #F1F5F9;
  border-radius: 14rpx;
  padding: 12rpx 6rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.cat-chip.active {
  background: #E6FBF5;
  border: 1rpx solid #00C28A;
}

.cat-chip-icon {
  font-size: 28rpx;
}

.cat-chip-name {
  font-size: 20rpx;
  color: #334155;
}

.cat-chip.active .cat-chip-name {
  color: #00C28A;
  font-weight: 600;
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

.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  gap: 16rpx;
}

.empty-icon, .loading-icon {
  font-size: 64rpx;
}

.state-text {
  font-size: 26rpx;
  color: #94A3B8;
}

.empty-btn {
  background: #00C28A;
  color: #FFFFFF;
  font-size: 26rpx;
  border-radius: 30rpx;
  padding: 0 32rpx;
  height: 64rpx;
  line-height: 64rpx;
  margin-top: 12rpx;
}
</style>
