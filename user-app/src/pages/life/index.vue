<template>
  <view class="life-page">
    <!-- 顶部标题栏 -->
    <view class="life-header">
      <text class="title">生活管理</text>
      <text class="subtitle">管理日常生活资源</text>
    </view>

    <!-- 10 大功能模块网格导航 (2 列卡片) -->
    <view class="module-grid">
      <view
        v-for="item in moduleList"
        :key="item.id"
        class="module-card"
        @click="navigateTo(item.url)"
      >
        <view class="icon-wrap" :style="{ backgroundColor: item.bg }">
          <text class="module-icon">{{ item.icon }}</text>
        </view>
        <view class="module-info">
          <text class="module-name">{{ item.name }}</text>
          <text class="module-desc">{{ item.desc }}</text>
        </view>
      </view>
    </view>

    <!-- 最近收支汇总卡片与列表 -->
    <view class="recent-finance-card">
      <view class="card-header">
        <text class="card-title">最近收支</text>
        <text class="view-all" @click="navigateTo('/pages/finance/index')">查看全部 ></text>
      </view>

      <view v-if="loading" class="state-hint">
        <text>数据加载中...</text>
      </view>

      <view v-else-if="recentRecords.length === 0" class="empty-hint">
        <text>暂无最近收支记录，点击右下角添加记账</text>
      </view>

      <view v-else class="record-list">
        <view
          v-for="rec in recentRecords"
          :key="rec.id"
          class="record-item"
          @click="navigateTo('/pages/finance/index')"
        >
          <view class="record-left">
            <view class="cat-icon-badge" :class="rec.recordType === 'INCOME' ? 'bg-income' : 'bg-expense'">
              <text>{{ rec.recordType === 'INCOME' ? '💰' : '👛' }}</text>
            </view>
            <view class="record-texts">
              <text class="record-title">{{ rec.title || (rec.recordType === 'INCOME' ? '生活收入' : '日常支出') }}</text>
              <view class="record-meta">
                <text class="cat-tag">{{ rec.categoryName || (rec.recordType === 'INCOME' ? '收入' : '支出') }}</text>
                <text class="date-tag">{{ formatDate(rec.recordDate) }}</text>
              </view>
            </view>
          </view>
          <text class="record-amount" :class="rec.recordType === 'INCOME' ? 'income' : 'expense'">
            {{ rec.recordType === 'INCOME' ? '+' : '-' }}¥{{ formatAmount(rec.amount) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 右下角悬浮快捷记账按钮 -->
    <view class="fab-btn" @click="navigateTo('/pages/finance/index')">
      <text class="fab-plus">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { getFinancePageApi } from '../../api/finance'
import type { FinanceRecordItem } from '../../types/finance'

const userStore = useUserStore()
const loading = ref(false)
const recentRecords = ref<FinanceRecordItem[]>([])

const moduleList = [
  { id: 'finance', name: '收支记录', desc: '记录收入支出', icon: '👛', bg: '#E6F9F3', url: '/pages/finance/index' },
  { id: 'todos', name: '待办清单', desc: '管理待办事项', icon: '✓', bg: '#EBF5FF', url: '/pages/todos/index' },
  { id: 'schedule', name: '日程安排', desc: '管理日程计划', icon: '📅', bg: '#FEF9C3', url: '/pages/todos/index' },
  { id: 'diaries', name: '日记', desc: '记录生活点滴', icon: '📔', bg: '#F3E8FF', url: '/pages/diaries/index' },
  { id: 'memos', name: '备忘录', desc: '随手记录备忘', icon: '📌', bg: '#E0F2FE', url: '/pages/memos/index' },
  { id: 'items', name: '物品清单', desc: '管理个人物品', icon: '📦', bg: '#FCE7F3', url: '/pages/items/index' },
  { id: 'lending', name: '借还记录', desc: '跟踪借还状态', icon: '🔄', bg: '#FEF3C7', url: '/pages/items/index' },
  { id: 'habits', name: '习惯打卡', desc: '养成好习惯', icon: '⭐', bg: '#ECFDF5', url: '/pages/habits/index' },
  { id: 'pomodoro', name: '番茄钟', desc: '专注计时工具', icon: '⏱️', bg: '#FEE2E2', url: '/pages/pomodoro/index' },
  { id: 'stats', name: '时间统计', desc: '时间投入分析', icon: '📈', bg: '#E0E7FF', url: '/pages/pomodoro/index' }
]

const loadRecentFinance = async () => {
  if (!userStore.isLoggedIn) return
  loading.value = true
  try {
    const res = await getFinancePageApi({ pageNo: 1, pageSize: 6 })
    if (res.data && res.data.records) {
      recentRecords.value = res.data.records
    }
  } catch {
    // 忽略异常
  } finally {
    loading.value = false
  }
}

onShow(() => {
  loadRecentFinance()
})

const navigateTo = (url: string) => {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  uni.navigateTo({ url })
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.length >= 10 ? dateStr.substring(5, 10) : dateStr
}

const formatAmount = (val: number | string) => {
  const num = Number(val) || 0
  return num.toFixed(2)
}
</script>

<style scoped>
.life-page {
  min-height: 100vh;
  background-color: #F6F8FA;
  padding: 30rpx 28rpx 140rpx;
  box-sizing: border-box;
}

/* 顶部标题栏 */
.life-header {
  margin-bottom: 28rpx;
}
.life-header .title {
  display: block;
  font-size: 42rpx;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.2;
}
.life-header .subtitle {
  display: block;
  font-size: 26rpx;
  color: #94A3B8;
  margin-top: 8rpx;
}

/* 10 大功能模块 2 列宫格 */
.module-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 32rpx;
}
.module-card {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.module-card:active {
  transform: scale(0.98);
}
.icon-wrap {
  width: 76rpx;
  height: 76rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}
.module-icon {
  font-size: 36rpx;
}
.module-info {
  flex: 1;
  overflow: hidden;
}
.module-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  line-height: 1.3;
}
.module-desc {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 4rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 最近收支卡片 */
.recent-finance-card {
  background-color: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
}
.view-all {
  font-size: 26rpx;
  color: #00C28A;
  font-weight: 500;
}

.state-hint, .empty-hint {
  text-align: center;
  padding: 40rpx 0;
  color: #94A3B8;
  font-size: 26rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
}
.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F1F5F9;
}
.record-item:last-child {
  border-bottom: none;
}
.record-left {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
}
.cat-icon-badge {
  width: 68rpx;
  height: 68rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 30rpx;
  flex-shrink: 0;
}
.bg-income {
  background-color: #ECFDF5;
}
.bg-expense {
  background-color: #FEF2F2;
}
.record-texts {
  flex: 1;
  overflow: hidden;
}
.record-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.record-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 4rpx;
}
.cat-tag {
  font-size: 22rpx;
  color: #64748B;
}
.date-tag {
  font-size: 22rpx;
  color: #94A3B8;
}
.record-amount {
  font-size: 32rpx;
  font-weight: 700;
  margin-left: 20rpx;
}
.record-amount.income {
  color: #00C28A;
}
.record-amount.expense {
  color: #EF4444;
}

/* 悬浮加号按钮 */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 150rpx;
  width: 100rpx;
  height: 100rpx;
  background-color: #00C28A;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 24rpx rgba(0, 194, 138, 0.35);
  z-index: 99;
}
.fab-plus {
  color: #FFFFFF;
  font-size: 52rpx;
  font-weight: 300;
  line-height: 1;
  margin-top: -4rpx;
}
</style>

