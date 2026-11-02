<template>
  <view class="home-page">
    <!-- 1. 顶部用户问候与快捷动作 -->
    <view class="top-header">
      <view class="greeting-left">
        <text class="greeting-hi">Hi, {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '你好' }}</text>
        <text class="greeting-sub">今日概览</text>
      </view>
      <view class="action-right">
        <view class="quick-btn" @click="goToPomodoro">
          <text class="quick-icon">⚡</text>
        </view>
        <view class="quick-btn msg-btn" @click="goToNotifications">
          <text class="quick-icon">💬</text>
          <text v-if="unreadCount > 0" class="badge-dot">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
        </view>
        <view class="avatar-wrap" @click="switchTab('/pages/my/index')">
          <text class="avatar-emoji">👤</text>
        </view>
      </view>
    </view>

    <!-- 2. 今日概览双色大卡片 (步数 & 待办) -->
    <view class="hero-cards">
      <!-- 步数卡片 (翡翠绿渐变) -->
      <view class="hero-card bg-green-gradient" @click="switchTab('/pages/health/index')">
        <view class="hero-top">
          <text class="hero-tag">步数</text>
        </view>
        <view class="hero-body">
          <text class="hero-value">{{ todaySteps }}</text>
          <text class="hero-sub">{{ Math.min(100, Math.round((todaySteps / 8000) * 100)) }}% 日目标</text>
        </view>
      </view>

      <!-- 待办卡片 (科技蓝渐变) -->
      <view class="hero-card bg-blue-gradient" @click="goToTodos">
        <view class="hero-top">
          <text class="hero-icon-small">🕒</text>
          <text class="hero-tag">待办</text>
        </view>
        <view class="hero-body">
          <text class="hero-value">{{ homeSummary.todayTodoCount || todayTodos.length }}</text>
          <text class="hero-sub">{{ homeSummary.todayCompletedTodoCount }} 已完成</text>
        </view>
      </view>
    </view>

    <!-- 3. 今日待办 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">今日待办</text>
        <text class="section-link" @click="goToTodos">查看全部 ></text>
      </view>

      <view v-if="loadingTodos" class="state-hint">
        <text>待办加载中...</text>
      </view>
      <view v-else-if="todayTodos.length === 0" class="empty-hint">
        <text>🎉 今日暂无待办事项，点击右上角添加</text>
      </view>
      <view v-else class="todo-list">
        <view
          v-for="item in todayTodos.slice(0, 4)"
          :key="item.id"
          class="todo-item"
          @click="toggleTodo(item)"
        >
          <view class="todo-checkbox" :class="{ 'is-checked': item.status === 'DONE' }">
            <text v-if="item.status === 'DONE'" class="check-mark">✓</text>
          </view>
          <view class="todo-info">
            <text class="todo-text" :class="{ 'text-done': item.status === 'DONE' }">
              {{ item.title }}
            </text>
            <text class="priority-pill" :class="'p-' + item.priority">{{ item.priorityDesc || '普通' }}</text>
          </view>
          <text class="item-arrow">></text>
        </view>
      </view>
    </view>

    <!-- 4. 目标进度 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">目标进度</text>
        <text class="section-link" @click="switchTab('/pages/goals/index')">查看全部 ></text>
      </view>

      <view v-if="activeGoals.length === 0" class="empty-hint">
        <text>暂无进行中的目标，前去制定你的宏伟计划</text>
      </view>
      <view v-else class="goal-progress-list">
        <view
          v-for="goal in activeGoals.slice(0, 3)"
          :key="goal.id"
          class="goal-prog-item"
          @click="switchTab('/pages/goals/index')"
        >
          <view class="prog-title-row">
            <text class="prog-title">{{ goal.title }}</text>
            <text class="prog-num">{{ Math.round(goal.progress || 0) }}%</text>
          </view>
          <view class="prog-track">
            <view class="prog-bar" :style="{ width: Math.min(100, Math.max(5, goal.progress || 0)) + '%' }" />
          </view>
        </view>
      </view>
    </view>

    <!-- 5. 本月收支 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">本月收支</text>
        <text class="section-link" @click="goToFinance">查看详情 ></text>
      </view>

      <view class="finance-overview-grid" @click="goToFinance">
        <view class="finance-col">
          <text class="finance-label">收入</text>
          <text class="finance-val income-val">+¥{{ formatMoney(monthlyIncome) }}</text>
        </view>
        <view class="finance-divider" />
        <view class="finance-col">
          <text class="finance-label">支出</text>
          <text class="finance-val expense-val">-¥{{ formatMoney(monthlyExpense) }}</text>
        </view>
      </view>
    </view>

    <!-- 6. 目标完成率 (3 圆环进度) -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">目标完成率</text>
        <text class="section-link" @click="switchTab('/pages/goals/index')">查看全部 ></text>
      </view>

      <view class="rings-row">
        <view
          v-for="(goal, idx) in goalRings"
          :key="idx"
          class="ring-col"
          @click="switchTab('/pages/goals/index')"
        >
          <view class="ring-circle" :style="getRingStyle(goal.pct)">
            <view class="ring-inner">
              <text class="ring-pct">{{ goal.pct }}%</text>
            </view>
          </view>
          <text class="ring-title">{{ goal.title }}</text>
          <text class="ring-status">进行中</text>
        </view>
      </view>
    </view>

    <!-- 7. 时间分布 (环形饼图 + 耗时进度明细) -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">时间分布</text>
        <text class="section-link" @click="goToPomodoro">查看详情 ></text>
      </view>

      <view class="time-dist-layout">
        <!-- 环形概览 -->
        <view class="donut-chart-wrap">
          <view class="donut-chart">
            <view class="donut-center">
              <text class="donut-num">{{ totalFocusMinutes }}</text>
              <text class="donut-unit">分钟</text>
            </view>
          </view>
        </view>

        <!-- 图例耗时 -->
        <view class="time-legend-list">
          <view v-for="cat in timeCategories" :key="cat.name" class="legend-row">
            <view class="legend-dot" :style="{ backgroundColor: cat.color }" />
            <text class="legend-name">{{ cat.name }}</text>
            <text class="legend-val">{{ cat.minutes }}分钟</text>
          </view>
        </view>
      </view>

      <!-- 分类占比条 -->
      <view class="cat-bar-list">
        <view v-for="cat in timeCategories" :key="'bar-' + cat.name" class="cat-bar-row">
          <view class="bar-header">
            <view class="dot-name">
              <view class="legend-dot" :style="{ backgroundColor: cat.color }" />
              <text class="bar-cat-name">{{ cat.name }}</text>
            </view>
            <text class="bar-pct">{{ cat.percent }}%</text>
          </view>
          <view class="bar-track">
            <view class="bar-fill" :style="{ width: cat.percent + '%', backgroundColor: cat.color }" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { getHomeSummaryApi } from '../../api/home'
import { getTodoListApi, updateTodoStatusApi } from '../../api/todo'
import { getGoalListApi } from '../../api/goal'
import { getFinancePageApi } from '../../api/finance'
import { getHealthRecordPageApi } from '../../api/health'
import { getUnreadCountApi } from '../../api/reminder'
import type { HomeSummary } from '../../types/home'
import type { TodoItem } from '../../types/todo'
import type { GoalItem } from '../../types/goal'

const userStore = useUserStore()

const homeSummary = reactive<HomeSummary>({
  greeting: '',
  currentDate: '',
  dayOfWeekDesc: '',
  todayTodoCount: 0,
  todayCompletedTodoCount: 0,
  pendingTodoCount: 0,
  totalTodoCount: 0,
  overdueTodoCount: 0,
  todayHabitCheckinCount: 0,
  activeGoalCount: 0,
  todayFocusMinutes: 0
})

const todayTodos = ref<TodoItem[]>([])
const loadingTodos = ref(false)
const activeGoals = ref<GoalItem[]>([])
const todaySteps = ref(5500)
const unreadCount = ref(0)
const monthlyIncome = ref(34630.50)
const monthlyExpense = ref(11989.00)
const totalFocusMinutes = ref(12)

const timeCategories = ref([
  { name: '工作', minutes: 4, percent: 33, color: '#3B82F6' },
  { name: '学习', minutes: 3, percent: 25, color: '#F59E0B' },
  { name: '生活', minutes: 3, percent: 25, color: '#10B981' },
  { name: '运动', minutes: 2, percent: 17, color: '#EF4444' }
])

const goalRings = computed(() => {
  if (activeGoals.value.length >= 3) {
    return activeGoals.value.slice(0, 3).map(g => ({
      title: g.title,
      pct: Math.round(g.progress || 0)
    }))
  }
  return [
    { title: activeGoals.value[0]?.title || '通过PMP认证', pct: Math.round(activeGoals.value[0]?.progress || 45) },
    { title: activeGoals.value[1]?.title || '体重减到70kg', pct: Math.round(activeGoals.value[1]?.progress || 30) },
    { title: activeGoals.value[2]?.title || '存款达到10万', pct: Math.round(activeGoals.value[2]?.progress || 35) }
  ]
})

const getRingStyle = (pct: number) => {
  const deg = (pct / 100) * 360
  return {
    background: `conic-gradient(#00C28A 0deg ${deg}deg, #E2E8F0 ${deg}deg 360deg)`
  }
}

const loadAllData = async () => {
  if (!userStore.isLoggedIn) return

  userStore.fetchUserInfo().catch(() => {})

  // 1. 摘要
  getHomeSummaryApi().then(res => {
    if (res.code === 0 && res.data) {
      Object.assign(homeSummary, res.data)
      if (res.data.todayFocusMinutes) {
        totalFocusMinutes.value = res.data.todayFocusMinutes
      }
    }
  }).catch(() => {})

  // 2. 待办
  loadingTodos.value = true
  getTodoListApi({ filterType: 'TODAY', pageSize: 5 }).then(res => {
    if (res.code === 0 && res.data) {
      todayTodos.value = res.data.records || []
    }
  }).catch(() => {}).finally(() => {
    loadingTodos.value = false
  })

  // 3. 目标
  getGoalListApi({ status: 'IN_PROGRESS', pageSize: 5 }).then(res => {
    if (res.code === 0 && res.data) {
      activeGoals.value = res.data.records || []
    }
  }).catch(() => {})

  // 4. 收支当月数据
  getFinancePageApi({ pageNo: 1, pageSize: 20 }).then(res => {
    if (res.code === 0 && res.data && res.data.records) {
      let inc = 0
      let exp = 0
      res.data.records.forEach(r => {
        if (r.recordType === 'INCOME') inc += Number(r.amount) || 0
        else exp += Number(r.amount) || 0
      })
      if (inc > 0 || exp > 0) {
        monthlyIncome.value = inc
        monthlyExpense.value = exp
      }
    }
  }).catch(() => {})

  // 5. 步数健康记录
  getHealthRecordPageApi({ metricCode: 'STEPS', pageNo: 1, pageSize: 1 }).then(res => {
    if (res.code === 0 && res.data && res.data.records && res.data.records.length > 0) {
      todaySteps.value = Math.round(Number(res.data.records[0].metricValue) || 5500)
    }
  }).catch(() => {})

  // 6. 未读消息
  getUnreadCountApi().then(res => {
    if (typeof res.data === 'number') {
      unreadCount.value = res.data
    }
  }).catch(() => {})
}

const toggleTodo = async (item: TodoItem) => {
  const targetStatus = item.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    const res = await updateTodoStatusApi(item.id, targetStatus)
    if (res.code === 0) {
      item.status = targetStatus
      uni.showToast({
        title: targetStatus === 'DONE' ? '已完成待办 🎉' : '重新标记待办',
        icon: 'none'
      })
    }
  } catch {
    // 忽略异常
  }
}

onMounted(() => {
  loadAllData()
})

onShow(() => {
  loadAllData()
})

onPullDownRefresh(() => {
  loadAllData().finally(() => {
    uni.stopPullDownRefresh()
  })
})

const goToTodos = () => {
  uni.navigateTo({ url: '/pages/todos/index' })
}

const goToPomodoro = () => {
  uni.navigateTo({ url: '/pages/pomodoro/index' })
}

const goToFinance = () => {
  uni.navigateTo({ url: '/pages/finance/index' })
}

const goToNotifications = () => {
  uni.navigateTo({ url: '/pages/notifications/index' })
}

const switchTab = (url: string) => {
  uni.switchTab({ url })
}

const formatMoney = (val: number) => {
  return val.toFixed(2)
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #F6F8FA;
  padding: 30rpx 28rpx 140rpx;
  box-sizing: border-box;
}

/* 1. 顶部 Header */
.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}
.greeting-hi {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #1E293B;
  line-height: 1.2;
}
.greeting-sub {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
  margin-top: 6rpx;
}
.action-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.quick-btn {
  width: 72rpx;
  height: 72rpx;
  background-color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.04);
  position: relative;
}
.quick-icon {
  font-size: 32rpx;
}
.badge-dot {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  background-color: #EF4444;
  color: #FFFFFF;
  font-size: 18rpx;
  font-weight: 700;
  padding: 2rpx 8rpx;
  border-radius: 20rpx;
  line-height: 1;
}
.avatar-wrap {
  width: 76rpx;
  height: 76rpx;
  background-color: #E2E8F0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.05);
}
.avatar-emoji {
  font-size: 36rpx;
}

/* 2. 今日概览双色大卡片 */
.hero-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 28rpx;
}
.hero-card {
  border-radius: 24rpx;
  padding: 28rpx;
  color: #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}
.bg-green-gradient {
  background: linear-gradient(135deg, #00C28A 0%, #059669 100%);
}
.bg-blue-gradient {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
}
.hero-top {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8rpx;
}
.hero-icon-small {
  font-size: 24rpx;
}
.hero-tag {
  font-size: 24rpx;
  opacity: 0.9;
}
.hero-body {
  margin-top: 14rpx;
}
.hero-value {
  display: block;
  font-size: 54rpx;
  font-weight: 800;
  line-height: 1.1;
}
.hero-sub {
  display: block;
  font-size: 22rpx;
  opacity: 0.85;
  margin-top: 8rpx;
}

/* 通用 section-card */
.section-card {
  background-color: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
}
.section-link {
  font-size: 24rpx;
  color: #00C28A;
  font-weight: 600;
}

.state-hint, .empty-hint {
  text-align: center;
  padding: 30rpx 0;
  color: #94A3B8;
  font-size: 26rpx;
}

/* 3. 待办列表 */
.todo-list {
  display: flex;
  flex-direction: column;
}
.todo-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F1F5F9;
}
.todo-item:last-child {
  border-bottom: none;
}
.todo-checkbox {
  width: 40rpx;
  height: 40rpx;
  border-radius: 10rpx;
  border: 2rpx solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}
.todo-checkbox.is-checked {
  background-color: #00C28A;
  border-color: #00C28A;
}
.check-mark {
  color: #FFFFFF;
  font-size: 26rpx;
  font-weight: 700;
}
.todo-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  overflow: hidden;
}
.todo-text {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.todo-text.text-done {
  color: #94A3B8;
  text-decoration: line-through;
}
.priority-pill {
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  background-color: #F1F5F9;
  color: #64748B;
  flex-shrink: 0;
}
.priority-pill.p-3 {
  background-color: #FEF2F2;
  color: #EF4444;
}
.item-arrow {
  color: #CBD5E1;
  font-size: 28rpx;
  margin-left: 12rpx;
}

/* 4. 目标进度 */
.goal-progress-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.goal-prog-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}
.prog-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.prog-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}
.prog-num {
  font-size: 26rpx;
  font-weight: 700;
  color: #00C28A;
}
.prog-track {
  width: 100%;
  height: 12rpx;
  background-color: #F1F5F9;
  border-radius: 8rpx;
  overflow: hidden;
}
.prog-bar {
  height: 100%;
  background-color: #00C28A;
  border-radius: 8rpx;
  transition: width 0.3s ease;
}

/* 5. 本月收支 */
.finance-overview-grid {
  display: flex;
  align-items: center;
  padding: 10rpx 0;
}
.finance-col {
  flex: 1;
  text-align: center;
}
.finance-label {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
  margin-bottom: 6rpx;
}
.finance-val {
  display: block;
  font-size: 38rpx;
  font-weight: 800;
}
.income-val {
  color: #00C28A;
}
.expense-val {
  color: #EF4444;
}
.finance-divider {
  width: 1rpx;
  height: 50rpx;
  background-color: #E2E8F0;
}

/* 6. 目标完成率 3 圆环 */
.rings-row {
  display: flex;
  justify-content: space-around;
  padding: 16rpx 0;
}
.ring-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 30%;
}
.ring-circle {
  width: 130rpx;
  height: 130rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}
.ring-inner {
  width: 100rpx;
  height: 100rpx;
  background-color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ring-pct {
  font-size: 28rpx;
  font-weight: 800;
  color: #00C28A;
}
.ring-title {
  font-size: 22rpx;
  color: #1E293B;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}
.ring-status {
  font-size: 20rpx;
  color: #94A3B8;
  margin-top: 2rpx;
}

/* 7. 时间分布 */
.time-dist-layout {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}
.donut-chart-wrap {
  width: 200rpx;
  display: flex;
  justify-content: center;
}
.donut-chart {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: conic-gradient(#3B82F6 0deg 120deg, #F59E0B 120deg 210deg, #10B981 210deg 300deg, #EF4444 300deg 360deg);
  display: flex;
  align-items: center;
  justify-content: center;
}
.donut-center {
  width: 104rpx;
  height: 104rpx;
  background-color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.donut-num {
  font-size: 32rpx;
  font-weight: 800;
  color: #1E293B;
  line-height: 1;
}
.donut-unit {
  font-size: 18rpx;
  color: #94A3B8;
  margin-top: 4rpx;
}
.time-legend-list {
  flex: 1;
  padding-left: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.legend-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}
.legend-name {
  font-size: 26rpx;
  color: #475569;
  flex: 1;
}
.legend-val {
  font-size: 24rpx;
  color: #94A3B8;
}

/* 分类进度条 */
.cat-bar-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  border-top: 1rpx solid #F1F5F9;
  padding-top: 20rpx;
}
.cat-bar-row {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dot-name {
  display: flex;
  align-items: center;
}
.bar-cat-name {
  font-size: 24rpx;
  color: #334155;
  font-weight: 500;
}
.bar-pct {
  font-size: 24rpx;
  font-weight: 700;
  color: #1E293B;
}
.bar-track {
  width: 100%;
  height: 10rpx;
  background-color: #F1F5F9;
  border-radius: 6rpx;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}
</style>
