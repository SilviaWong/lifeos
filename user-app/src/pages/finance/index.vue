<template>
  <view class="finance-page">
    <!-- 顶部月度收支汇总看板 -->
    <view class="life-card summary-card">
      <view class="month-selector">
        <button class="nav-btn" @tap="prevMonth">‹ 上月</button>
        <view class="month-title-box">
          <text class="current-month">{{ currentYear }} 年 {{ currentMonth }} 月</text>
          <text class="count-hint">{{ summary.recordCount }} 笔收支记录</text>
        </view>
        <button class="nav-btn" @tap="nextMonth">下月 ›</button>
      </view>

      <view class="summary-balance-box">
        <text class="balance-label">本月结余</text>
        <text class="balance-val" :class="summary.balance >= 0 ? 'text-success' : 'text-danger'">
          {{ summary.balance >= 0 ? '+' : '' }}¥{{ formatMoney(summary.balance) }}
        </text>
      </view>

      <view class="summary-details-grid">
        <view class="detail-item">
          <text class="label">本月总收入</text>
          <text class="val text-success">+¥{{ formatMoney(summary.totalIncome) }}</text>
        </view>
        <view class="divider" />
        <view class="detail-item">
          <text class="label">本月总支出</text>
          <text class="val text-danger">-¥{{ formatMoney(summary.totalExpense) }}</text>
        </view>
      </view>
    </view>

    <!-- 视图切换与顶部操作卡片 -->
    <view class="life-card view-switch-card">
      <view class="view-tabs">
        <view
          class="tab-item"
          :class="{ active: currentView === 'RECORDS' }"
          @tap="currentView = 'RECORDS'"
        >
          <text>流水明细</text>
        </view>
        <view
          class="tab-item"
          :class="{ active: currentView === 'CHARTS' }"
          @tap="currentView = 'CHARTS'"
        >
          <text>统计图表</text>
        </view>
      </view>

      <button class="record-btn" @tap="openCreateModal">+ 记一笔</button>
    </view>

    <!-- 视图 1：流水明细列表 -->
    <view v-if="currentView === 'RECORDS'" class="life-card list-card">
      <!-- 类型快捷筛选 -->
      <view class="type-filter-bar">
        <view
          v-for="f in filterOptions"
          :key="f.value"
          class="filter-pill"
          :class="{ active: queryType === f.value }"
          @tap="switchTypeFilter(f.value)"
        >
          <text>{{ f.label }}</text>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loadingRecords" class="state-container">
        <text class="loading-icon">⏳</text>
        <text class="state-text">正在加载收支明细...</text>
      </view>

      <!-- 空数据状态 -->
      <view v-else-if="records.length === 0" class="state-container">
        <text class="empty-icon">💰</text>
        <text class="state-text">本月暂无收支记录，随手记账自律自强！</text>
        <button class="empty-btn" @tap="openCreateModal">立即记一笔</button>
      </view>

      <!-- 记录列表 -->
      <view v-else class="record-list">
        <view
          v-for="record in records"
          :key="record.id"
          class="record-item"
          @tap="openEditModal(record)"
        >
          <view class="record-left">
            <view class="category-icon-circle" :class="record.recordType.toLowerCase()">
              <text class="icon-emoji">{{ getCategoryEmoji(record.categoryIcon, record.recordType) }}</text>
            </view>
            <view class="record-info">
              <view class="title-line">
                <text class="record-title">{{ record.title || record.categoryName || '未命名' }}</text>
                <text class="category-tag">{{ record.categoryName || record.recordTypeDesc }}</text>
              </view>
              <view class="meta-line">
                <text class="date-text">{{ record.recordDate }}</text>
                <text v-if="record.remark" class="remark-text">{{ record.remark }}</text>
              </view>
            </view>
          </view>

          <view class="record-right">
            <text
              class="amount-text"
              :class="record.recordType === 'INCOME' ? 'text-success' : 'text-danger'"
            >
              {{ record.recordType === 'INCOME' ? '+' : '-' }}¥{{ formatMoney(record.amount) }}
            </text>
            <text class="delete-link" @tap.stop="confirmDelete(record)">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 视图 2：统计图表 -->
    <view v-else class="charts-view">
      <!-- 分类占比分析 -->
      <view class="life-card chart-card">
        <view class="chart-header">
          <text class="chart-title">支出分类构成</text>
          <text class="chart-subtitle">共支出 ¥{{ formatMoney(summary.totalExpense) }}</text>
        </view>

        <view v-if="summary.expenseCategoryStats.length === 0" class="chart-empty">
          <text>本月暂无支出分类数据</text>
        </view>

        <view v-else class="category-stat-list">
          <view
            v-for="item in summary.expenseCategoryStats"
            :key="item.categoryId || item.categoryName"
            class="stat-bar-item"
          >
            <view class="stat-bar-header">
              <view class="cat-left">
                <text class="cat-emoji">{{ getCategoryEmoji(item.categoryIcon, 'EXPENSE') }}</text>
                <text class="cat-name">{{ item.categoryName }}</text>
                <text class="cat-count">({{ item.recordCount }}笔)</text>
              </view>
              <view class="cat-right">
                <text class="cat-amount">¥{{ formatMoney(item.totalAmount) }}</text>
                <text class="cat-pct">{{ item.percentage }}%</text>
              </view>
            </view>
            <!-- 进度条 -->
            <view class="progress-track">
              <view class="progress-fill bg-danger" :style="{ width: item.percentage + '%' }" />
            </view>
          </view>
        </view>
      </view>

      <!-- 收入分类构成 -->
      <view v-if="summary.incomeCategoryStats.length > 0" class="life-card chart-card">
        <view class="chart-header">
          <text class="chart-title">收入分类构成</text>
          <text class="chart-subtitle">共收入 ¥{{ formatMoney(summary.totalIncome) }}</text>
        </view>

        <view class="category-stat-list">
          <view
            v-for="item in summary.incomeCategoryStats"
            :key="item.categoryId || item.categoryName"
            class="stat-bar-item"
          >
            <view class="stat-bar-header">
              <view class="cat-left">
                <text class="cat-emoji">{{ getCategoryEmoji(item.categoryIcon, 'INCOME') }}</text>
                <text class="cat-name">{{ item.categoryName }}</text>
                <text class="cat-count">({{ item.recordCount }}笔)</text>
              </view>
              <view class="cat-right">
                <text class="cat-amount">¥{{ formatMoney(item.totalAmount) }}</text>
                <text class="cat-pct">{{ item.percentage }}%</text>
              </view>
            </view>
            <view class="progress-track">
              <view class="progress-fill bg-success" :style="{ width: item.percentage + '%' }" />
            </view>
          </view>
        </view>
      </view>

      <!-- 近 6 个月收支趋势图 -->
      <view class="life-card chart-card">
        <view class="chart-header">
          <text class="chart-title">近 6 个月收支趋势</text>
        </view>

        <view class="trend-bars-container">
          <view
            v-for="pt in trends.points"
            :key="pt.month"
            class="trend-col"
          >
            <view class="bars-pair">
              <!-- 收入柱 -->
              <view
                class="bar-item bar-income"
                :style="{ height: getBarHeight(pt.income, maxTrendVal) + 'rpx' }"
              />
              <!-- 支出柱 -->
              <view
                class="bar-item bar-expense"
                :style="{ height: getBarHeight(pt.expense, maxTrendVal) + 'rpx' }"
              />
            </view>
            <text class="month-label">{{ pt.month.substring(5) }}月</text>
            <text class="net-label" :class="pt.balance >= 0 ? 'text-success' : 'text-danger'">
              {{ pt.balance >= 0 ? '+' : '' }}{{ Math.round(pt.balance) }}
            </text>
          </view>
        </view>

        <view class="trend-legend">
          <view class="legend-item">
            <view class="legend-color bg-success" />
            <text>收入</text>
          </view>
          <view class="legend-item">
            <view class="legend-color bg-danger" />
            <text>支出</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 记账弹窗（新增 / 编辑） -->
    <view v-if="recordModalVisible" class="modal-overlay" @tap.self="closeRecordModal">
      <view class="modal-card record-modal">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑明细' : '记一笔账' }}</text>
          <text class="close-icon" @tap="closeRecordModal">✕</text>
        </view>

        <view class="modal-body">
          <!-- 收入/支出切换 -->
          <view class="type-segment">
            <view
              class="segment-item"
              :class="{ 'active-expense': form.recordType === 'EXPENSE' }"
              @tap="switchFormType('EXPENSE')"
            >
              <text>支出</text>
            </view>
            <view
              class="segment-item"
              :class="{ 'active-income': form.recordType === 'INCOME' }"
              @tap="switchFormType('INCOME')"
            >
              <text>收入</text>
            </view>
          </view>

          <!-- 金额输入大框 -->
          <view class="amount-input-box">
            <text class="currency-symbol">¥</text>
            <input
              v-model="formAmountStr"
              type="digit"
              class="amount-input"
              placeholder="0.00"
              maxlength="10"
            />
          </view>

          <!-- 分类网格选择器 -->
          <view class="form-section">
            <text class="section-label">选择分类</text>
            <view class="category-grid">
              <view
                v-for="cat in availableCategories"
                :key="cat.id"
                class="cat-grid-item"
                :class="{ selected: form.categoryId === cat.id }"
                @tap="form.categoryId = cat.id"
              >
                <text class="cat-icon">{{ getCategoryEmoji(cat.icon, cat.categoryType) }}</text>
                <text class="cat-name-label">{{ cat.categoryName }}</text>
              </view>
            </view>
          </view>

          <!-- 日期选择器 -->
          <view class="form-row-item">
            <text class="row-label">日期</text>
            <picker mode="date" :value="form.recordDate" @change="onRecordDateChange">
              <view class="picker-display">
                <text>{{ form.recordDate }}</text>
                <text class="arrow">›</text>
              </view>
            </picker>
          </view>

          <!-- 标题输入 -->
          <view class="form-row-item">
            <text class="row-label">描述/标题</text>
            <input
              v-model="form.title"
              class="row-input"
              placeholder="例如：午餐外卖、日用品等"
              maxlength="50"
            />
          </view>

          <!-- 备注输入 -->
          <view class="form-row-item">
            <text class="row-label">备注</text>
            <input
              v-model="form.remark"
              class="row-input"
              placeholder="可选补充备注"
              maxlength="100"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="life-btn-secondary" @tap="closeRecordModal">取消</button>
          <button class="life-btn-primary" @tap="submitRecord">确认记录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import {
  createFinanceRecordApi,
  deleteFinanceRecordApi,
  getFinanceCategoriesApi,
  getFinanceRecordsApi,
  getFinanceSummaryApi,
  getFinanceTrendsApi,
  updateFinanceRecordApi
} from '../../api/finance'
import type {
  CreateFinanceRecordDTO,
  FinanceCategoryItem,
  FinanceRecordItem,
  FinanceSummaryData,
  FinanceTrendData
} from '../../types/finance'

// 当前年份和月份
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const currentView = ref<'RECORDS' | 'CHARTS'>('RECORDS')
const queryType = ref('ALL')
const loadingRecords = ref(false)

const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]

// 汇总数据
const summary = reactive<FinanceSummaryData>({
  year: currentYear.value,
  month: currentMonth.value,
  totalIncome: 0,
  totalExpense: 0,
  balance: 0,
  recordCount: 0,
  expenseCategoryStats: [],
  incomeCategoryStats: []
})

// 明细列表
const records = ref<FinanceRecordItem[]>([])

// 趋势数据
const trends = reactive<FinanceTrendData>({
  points: [],
  totalPeriodIncome: 0,
  totalPeriodExpense: 0,
  totalPeriodBalance: 0
})

// 分类列表
const allCategories = ref<FinanceCategoryItem[]>([])

// 弹窗状态
const recordModalVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
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
  amount: 0,
  categoryId: undefined,
  recordDate: getTodayDateStr(),
  title: '',
  remark: ''
})

const availableCategories = computed(() => {
  return allCategories.value.filter((c) => c.categoryType === form.recordType)
})

const maxTrendVal = computed(() => {
  let max = 1
  trends.points.forEach((p) => {
    if (p.income > max) max = p.income
    if (p.expense > max) max = p.expense
  })
  return max
})

const formatMoney = (val: number | undefined) => {
  if (val === undefined || val === null || isNaN(val)) return '0.00'
  return val.toFixed(2)
}

const getCategoryEmoji = (icon: string | undefined, type: string) => {
  if (!icon) return type === 'INCOME' ? '💵' : '💸'
  const map: Record<string, string> = {
    food: '🍜',
    transport: '🚌',
    shopping: '🛍️',
    entertainment: '🎬',
    housing: '🏠',
    medical: '💊',
    salary: '💰',
    bonus: '🧧',
    investment: '📈',
    part_time: '💼',
    digital: '💻',
    other: '🧾'
  }
  return map[icon] || (type === 'INCOME' ? '💵' : '💸')
}

const getBarHeight = (val: number, max: number) => {
  if (max <= 0 || !val) return 4
  const pct = Math.min(1, val / max)
  return Math.max(4, Math.round(pct * 160))
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentYear.value -= 1
    currentMonth.value = 12
  } else {
    currentMonth.value -= 1
  }
  loadMonthData()
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentYear.value += 1
    currentMonth.value = 1
  } else {
    currentMonth.value += 1
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
    if (res.code === 0 && res.data) {
      Object.assign(summary, res.data)
    }
  } catch (e) {
    uni.showToast({ title: '加载汇总失败', icon: 'none' })
  }
}

const fetchRecords = async () => {
  loadingRecords.value = true
  try {
    const ym = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}`
    const res = await getFinanceRecordsApi({
      yearMonth: ym,
      recordType: queryType.value === 'ALL' ? undefined : queryType.value,
      pageNo: 1,
      pageSize: 50
    })
    if (res.code === 0 && res.data) {
      records.value = res.data.records
    }
  } catch (e) {
    uni.showToast({ title: '加载明细失败', icon: 'none' })
  } finally {
    loadingRecords.value = false
    uni.stopPullDownRefresh()
  }
}

const fetchTrends = async () => {
  try {
    const res = await getFinanceTrendsApi(currentYear.value, 6)
    if (res.code === 0 && res.data) {
      Object.assign(trends, res.data)
    }
  } catch (e) {
    console.warn('Failed to load trends', e)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getFinanceCategoriesApi()
    if (res.code === 0 && res.data) {
      allCategories.value = res.data
    }
  } catch (e) {
    console.warn('Failed to load categories', e)
  }
}

// 记账弹窗操作
const openCreateModal = () => {
  isEditing.value = false
  editingId.value = null
  form.recordType = 'EXPENSE'
  formAmountStr.value = ''
  form.categoryId = availableCategories.value.length > 0 ? availableCategories.value[0].id : undefined
  form.recordDate = getTodayDateStr()
  form.title = ''
  form.remark = ''
  recordModalVisible.value = true
}

const openEditModal = (rec: FinanceRecordItem) => {
  isEditing.value = true
  editingId.value = rec.id
  form.recordType = rec.recordType
  formAmountStr.value = String(rec.amount)
  form.categoryId = rec.categoryId
  form.recordDate = rec.recordDate
  form.title = rec.title || ''
  form.remark = rec.remark || ''
  recordModalVisible.value = true
}

const closeRecordModal = () => {
  recordModalVisible.value = false
  editingId.value = null
}

const switchFormType = (type: 'INCOME' | 'EXPENSE') => {
  form.recordType = type
  const cats = allCategories.value.filter((c) => c.categoryType === type)
  form.categoryId = cats.length > 0 ? cats[0].id : undefined
}

const onRecordDateChange = (e: any) => {
  form.recordDate = e.detail.value
}

const submitRecord = async () => {
  const amt = parseFloat(formAmountStr.value)
  if (isNaN(amt) || amt <= 0) {
    uni.showToast({ title: '请输入大于0的有效金额', icon: 'none' })
    return
  }
  form.amount = Math.round(amt * 100) / 100

  try {
    if (isEditing.value && editingId.value) {
      const res = await updateFinanceRecordApi(editingId.value, form)
      if (res.code === 0) {
        uni.showToast({ title: '保存成功', icon: 'success' })
        closeRecordModal()
        loadMonthData()
      }
    } else {
      const res = await createFinanceRecordApi(form)
      if (res.code === 0) {
        uni.showToast({ title: '记账成功', icon: 'success' })
        closeRecordModal()
        loadMonthData()
      }
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

const confirmDelete = (record: FinanceRecordItem) => {
  uni.showModal({
    title: '删除明细',
    content: `确定删除这笔 ${record.recordTypeDesc} ¥${formatMoney(record.amount)} 吗？`,
    confirmColor: '#ff4d4f',
    success: async (r) => {
      if (r.confirm) {
        try {
          const res = await deleteFinanceRecordApi(record.id)
          if (res.code === 0) {
            uni.showToast({ title: '已删除明细', icon: 'success' })
            loadMonthData()
          }
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

onPullDownRefresh(() => {
  loadMonthData()
})
</script>

<style lang="scss" scoped>
.finance-page {
  padding: 24rpx;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.life-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
}

.summary-card {
  .month-selector {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .nav-btn {
      margin: 0;
      height: 52rpx;
      line-height: 52rpx;
      font-size: 24rpx;
      padding: 0 20rpx;
      background: #f5f5f5;
      border-radius: 26rpx;
      color: #595959;
    }

    .month-title-box {
      text-align: center;

      .current-month {
        font-size: 32rpx;
        font-weight: 700;
        color: #262626;
        display: block;
      }

      .count-hint {
        font-size: 22rpx;
        color: #8c8c8c;
      }
    }
  }

  .summary-balance-box {
    text-align: center;
    padding: 16rpx 0 24rpx;

    .balance-label {
      font-size: 24rpx;
      color: #8c8c8c;
      display: block;
      margin-bottom: 6rpx;
    }

    .balance-val {
      font-size: 52rpx;
      font-weight: 800;
      letter-spacing: 1rpx;
    }
  }

  .summary-details-grid {
    display: flex;
    justify-content: space-around;
    align-items: center;
    background: #f7f8fa;
    border-radius: 12rpx;
    padding: 20rpx;

    .detail-item {
      text-align: center;
      flex: 1;

      .label {
        font-size: 22rpx;
        color: #8c8c8c;
        display: block;
        margin-bottom: 4rpx;
      }

      .val {
        font-size: 30rpx;
        font-weight: 700;
      }
    }

    .divider {
      width: 2rpx;
      height: 40rpx;
      background: #e8e8e8;
    }
  }
}

.text-success { color: #52c41a; }
.text-danger { color: #ff4d4f; }
.bg-success { background-color: #52c41a; }
.bg-danger { background-color: #ff4d4f; }

.view-switch-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;

  .view-tabs {
    display: flex;
    gap: 16rpx;

    .tab-item {
      padding: 12rpx 28rpx;
      font-size: 28rpx;
      color: #595959;
      border-radius: 30rpx;

      &.active {
        background: #e6f4ff;
        color: #1677ff;
        font-weight: 600;
      }
    }
  }

  .record-btn {
    margin: 0;
    height: 64rpx;
    line-height: 64rpx;
    padding: 0 28rpx;
    background: #1677ff;
    color: #ffffff;
    font-size: 26rpx;
    border-radius: 32rpx;
  }
}

.list-card {
  .type-filter-bar {
    display: flex;
    gap: 16rpx;
    margin-bottom: 20rpx;

    .filter-pill {
      font-size: 24rpx;
      color: #8c8c8c;
      padding: 6rpx 20rpx;
      border-radius: 20rpx;
      background: #f5f5f5;

      &.active {
        background: #262626;
        color: #ffffff;
        font-weight: 600;
      }
    }
  }

  .record-list {
    display: flex;
    flex-direction: column;

    .record-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .record-left {
        display: flex;
        align-items: center;
        flex: 1;

        .category-icon-circle {
          width: 76rpx;
          height: 76rpx;
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-right: 20rpx;
          font-size: 34rpx;

          &.expense {
            background: #fff1f0;
          }

          &.income {
            background: #f6ffed;
          }
        }

        .record-info {
          flex: 1;

          .title-line {
            display: flex;
            align-items: center;
            gap: 12rpx;
            margin-bottom: 6rpx;

            .record-title {
              font-size: 28rpx;
              font-weight: 600;
              color: #262626;
            }

            .category-tag {
              font-size: 20rpx;
              color: #8c8c8c;
              background: #f5f5f5;
              padding: 2rpx 10rpx;
              border-radius: 8rpx;
            }
          }

          .meta-line {
            display: flex;
            gap: 16rpx;
            font-size: 22rpx;
            color: #8c8c8c;

            .remark-text {
              color: #595959;
            }
          }
        }
      }

      .record-right {
        text-align: right;

        .amount-text {
          font-size: 30rpx;
          font-weight: 700;
          display: block;
        }

        .delete-link {
          font-size: 22rpx;
          color: #ff4d4f;
          margin-top: 4rpx;
          display: inline-block;
        }
      }
    }
  }
}

/* 图表视图 */
.charts-view {
  .chart-card {
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;

      .chart-title {
        font-size: 30rpx;
        font-weight: 700;
        color: #262626;
      }

      .chart-subtitle {
        font-size: 24rpx;
        color: #8c8c8c;
      }
    }

    .chart-empty {
      text-align: center;
      padding: 40rpx 0;
      color: #8c8c8c;
      font-size: 26rpx;
    }

    .category-stat-list {
      display: flex;
      flex-direction: column;
      gap: 20rpx;

      .stat-bar-item {
        .stat-bar-header {
          display: flex;
          justify-content: space-between;
          font-size: 26rpx;
          margin-bottom: 8rpx;

          .cat-left {
            display: flex;
            align-items: center;
            gap: 8rpx;

            .cat-name {
              font-weight: 600;
              color: #262626;
            }

            .cat-count {
              color: #8c8c8c;
              font-size: 22rpx;
            }
          }

          .cat-right {
            display: flex;
            gap: 12rpx;

            .cat-amount {
              font-weight: 700;
              color: #262626;
            }

            .cat-pct {
              color: #8c8c8c;
            }
          }
        }

        .progress-track {
          height: 12rpx;
          background: #f0f0f0;
          border-radius: 6rpx;
          overflow: hidden;

          .progress-fill {
            height: 100%;
            border-radius: 6rpx;
          }
        }
      }
    }

    .trend-bars-container {
      display: flex;
      justify-content: space-around;
      align-items: flex-end;
      height: 240rpx;
      padding-top: 20rpx;
      border-bottom: 2rpx solid #f0f0f0;

      .trend-col {
        display: flex;
        flex-direction: column;
        align-items: center;

        .bars-pair {
          display: flex;
          align-items: flex-end;
          gap: 6rpx;
          height: 170rpx;

          .bar-item {
            width: 18rpx;
            border-radius: 6rpx 6rpx 0 0;
            min-height: 4rpx;

            &.bar-income {
              background: #52c41a;
            }

            &.bar-expense {
              background: #ff4d4f;
            }
          }
        }

        .month-label {
          font-size: 20rpx;
          color: #8c8c8c;
          margin-top: 8rpx;
        }

        .net-label {
          font-size: 18rpx;
          font-weight: 700;
        }
      }
    }

    .trend-legend {
      display: flex;
      justify-content: center;
      gap: 32rpx;
      margin-top: 16rpx;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 22rpx;
        color: #8c8c8c;

        .legend-color {
          width: 16rpx;
          height: 16rpx;
          border-radius: 4rpx;
        }
      }
    }
  }
}

/* 弹窗通用样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-card {
  width: 650rpx;
  background: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 28rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .modal-title {
      font-size: 32rpx;
      font-weight: 700;
      color: #262626;
    }

    .close-icon {
      font-size: 36rpx;
      color: #8c8c8c;
    }
  }

  .modal-body {
    padding: 24rpx 28rpx;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 20rpx;
    padding: 20rpx 28rpx;
    border-top: 2rpx solid #f0f0f0;

    button {
      margin: 0;
      font-size: 28rpx;
      height: 68rpx;
      line-height: 68rpx;
      border-radius: 34rpx;
      padding: 0 36rpx;
    }
  }
}

.record-modal {
  .type-segment {
    display: flex;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 6rpx;
    margin-bottom: 24rpx;

    .segment-item {
      flex: 1;
      text-align: center;
      padding: 12rpx 0;
      font-size: 28rpx;
      color: #595959;
      border-radius: 8rpx;

      &.active-expense {
        background: #ff4d4f;
        color: #ffffff;
        font-weight: 700;
      }

      &.active-income {
        background: #52c41a;
        color: #ffffff;
        font-weight: 700;
      }
    }
  }

  .amount-input-box {
    display: flex;
    align-items: center;
    border-bottom: 4rpx solid #262626;
    padding-bottom: 12rpx;
    margin-bottom: 24rpx;

    .currency-symbol {
      font-size: 48rpx;
      font-weight: 700;
      color: #262626;
      margin-right: 12rpx;
    }

    .amount-input {
      font-size: 52rpx;
      font-weight: 800;
      height: 72rpx;
      flex: 1;
    }
  }

  .form-section {
    margin-bottom: 20rpx;

    .section-label {
      font-size: 24rpx;
      color: #8c8c8c;
      display: block;
      margin-bottom: 12rpx;
    }

    .category-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16rpx;
      max-height: 280rpx;
      overflow-y: auto;

      .cat-grid-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 14rpx 8rpx;
        background: #fafafa;
        border: 2rpx solid #f0f0f0;
        border-radius: 12rpx;

        .cat-icon {
          font-size: 32rpx;
          margin-bottom: 6rpx;
        }

        .cat-name-label {
          font-size: 22rpx;
          color: #595959;
        }

        &.selected {
          border-color: #1677ff;
          background: #e6f4ff;

          .cat-name-label {
            color: #1677ff;
            font-weight: 700;
          }
        }
      }
    }
  }

  .form-row-item {
    display: flex;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 2rpx solid #f5f5f5;

    .row-label {
      width: 140rpx;
      font-size: 26rpx;
      color: #8c8c8c;
    }

    .row-input {
      flex: 1;
      font-size: 26rpx;
    }

    .picker-display {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 26rpx;
      color: #262626;

      .arrow {
        color: #8c8c8c;
      }
    }
  }
}

.life-btn-primary {
  background: #1677ff;
  color: #ffffff;
}

.life-btn-secondary {
  background: #f5f5f5;
  color: #595959;
}

.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;

  .loading-icon, .empty-icon {
    font-size: 64rpx;
    margin-bottom: 16rpx;
  }

  .state-text {
    font-size: 28rpx;
    color: #8c8c8c;
    margin-bottom: 24rpx;
  }

  .empty-btn {
    background: #1677ff;
    color: #ffffff;
    font-size: 28rpx;
    padding: 0 40rpx;
    height: 68rpx;
    line-height: 68rpx;
    border-radius: 34rpx;
  }
}
</style>

