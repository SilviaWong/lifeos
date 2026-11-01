<template>
  <view class="home-container">
    <!-- 顶部问候卡片 -->
    <view class="user-greeting-card">
      <view class="greeting-info">
        <text class="greeting-title">🌿 LifeOS 个人生活</text>
        <text class="greeting-subtitle">
          {{ homeSummary.greeting || (userStore.isLoggedIn ? `您好，${userStore.userInfo?.nickname || userStore.userInfo?.username}` : '开启井井有条的自律生活') }}
        </text>
        <text class="greeting-date">{{ homeSummary.currentDate }} {{ homeSummary.dayOfWeekDesc }}</text>
      </view>
      <view v-if="!userStore.isLoggedIn" class="login-action" @tap="goToLogin">
        <text class="login-badge">立即登录</text>
      </view>
    </view>

    <!-- 今日待办执行摘要卡片 -->
    <view class="life-card summary-card">
      <view class="life-card-title">
        <text>今日执行摘要</text>
        <text class="view-all-link" @tap="goToTodos">待办看板 ›</text>
      </view>

      <view class="summary-grid">
        <view class="grid-item" @tap="goToTodos">
          <text class="item-value text-primary">
            {{ homeSummary.todayCompletedTodoCount }} / {{ homeSummary.todayTodoCount }}
          </text>
          <text class="item-label">今日待办 (完成/总计)</text>
        </view>
        <view class="grid-item" @tap="goToTodos">
          <text class="item-value text-warning">{{ homeSummary.pendingTodoCount }}</text>
          <text class="item-label">待处理待办</text>
        </view>
        <view class="grid-item" @tap="goToTodos">
          <text class="item-value text-danger">{{ homeSummary.overdueTodoCount }}</text>
          <text class="item-label">已逾期待办</text>
        </view>
        <view class="grid-item" @tap="goToTodos">
          <text class="item-value text-success">{{ homeSummary.totalTodoCount }}</text>
          <text class="item-label">累计待办数</text>
        </view>
      </view>
    </view>

    <!-- 今日聚焦待办任务（前 5 条） -->
    <view class="life-card today-todo-card">
      <view class="life-card-title">
        <text>今日聚焦待办</text>
        <text class="new-todo-link" @tap="goToTodos">+ 添加待办</text>
      </view>

      <view v-if="loadingTodos" class="state-container">
        <text class="state-text">正在拉取待办数据...</text>
      </view>

      <view v-else-if="todayTodos.length === 0" class="empty-state">
        <text class="empty-icon">🎉</text>
        <text class="empty-text">今日暂无待办事项，轻松自律每一刻！</text>
        <button class="empty-btn" @tap="goToTodos">进入日程待办</button>
      </view>

      <view v-else class="today-todo-list">
        <view
          v-for="item in todayTodos"
          :key="item.id"
          class="home-todo-item"
          :class="{ 'is-done': item.status === 'DONE' }"
        >
          <view class="checkbox" :class="{ checked: item.status === 'DONE' }" @tap="toggleTodo(item)">
            <text v-if="item.status === 'DONE'" class="check-icon">✓</text>
          </view>
          <view class="content" @tap="goToTodos">
            <view class="title-row">
              <text class="title" :class="{ strikethrough: item.status === 'DONE' }">
                {{ item.title }}
              </text>
              <text class="priority-badge" :class="'p-' + item.priority">
                {{ item.priorityDesc }}
              </text>
            </view>
            <view v-if="item.dueAt" class="time-row">
              <text class="time-text">截止：{{ item.dueAt.substring(11, 16) }}</text>
              <text v-if="item.isOverdue" class="overdue-tag">已逾期</text>
            </view>
          </view>
        </view>
        <view class="more-bar" @tap="goToTodos">
          <text>查看更多待办事项 ›</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口卡片 -->
    <view class="life-card shortcut-card">
      <view class="life-card-title">快捷入口</view>
      <view class="shortcut-list">
        <view class="shortcut-item" @tap="goToTodos">
          <view class="icon-circle bg-blue">📝</view>
          <text class="shortcut-text">待办事项</text>
        </view>
        <view class="shortcut-item" @tap="switchTab('/pages/goals/index')">
          <view class="icon-circle bg-purple">🎯</view>
          <text class="shortcut-text">目标管理</text>
        </view>
        <view class="shortcut-item" @tap="switchTab('/pages/health/index')">
          <view class="icon-circle bg-green">🏃</view>
          <text class="shortcut-text">健康管理</text>
        </view>
        <view class="shortcut-item" @tap="switchTab('/pages/my/index')">
          <view class="icon-circle bg-orange">👤</view>
          <text class="shortcut-text">个人中心</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { getHomeSummaryApi } from '../../api/home'
import { getTodoListApi, updateTodoStatusApi } from '../../api/todo'
import type { HomeSummary } from '../../types/home'
import type { TodoItem } from '../../types/todo'

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

const loadHomeSummary = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getHomeSummaryApi()
    if (res.code === 0 && res.data) {
      Object.assign(homeSummary, res.data)
    }
  } catch (err) {
    console.error('加载首页摘要失败', err)
  }
}

const loadTodayTodos = async () => {
  if (!userStore.isLoggedIn) return
  loadingTodos.value = true
  try {
    const res = await getTodoListApi({
      filterType: 'TODAY',
      pageSize: 5
    })
    if (res.code === 0 && res.data) {
      todayTodos.value = res.data.records || []
    }
  } catch (err) {
    console.error('加载今日待办列表失败', err)
  } finally {
    loadingTodos.value = false
  }
}

const toggleTodo = async (item: TodoItem) => {
  const targetStatus = item.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    const res = await updateTodoStatusApi(item.id, targetStatus)
    if (res.code === 0) {
      uni.showToast({
        title: targetStatus === 'DONE' ? '已完成待办 🎉' : '已重新打开',
        icon: 'none'
      })
      await Promise.all([loadHomeSummary(), loadTodayTodos()])
    }
  } catch (err) {
    console.error('更新待办状态失败', err)
  }
}

const refreshData = async () => {
  if (userStore.isLoggedIn) {
    await Promise.all([loadHomeSummary(), loadTodayTodos()])
  }
  uni.stopPullDownRefresh()
}

onMounted(() => {
  refreshData()
})

onShow(() => {
  if (userStore.isLoggedIn) {
    loadHomeSummary()
    loadTodayTodos()
  }
})

onPullDownRefresh(() => {
  refreshData()
})

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const goToTodos = () => {
  uni.switchTab({ url: '/pages/todos/index' })
}

const switchTab = (url: string) => {
  uni.switchTab({ url })
}
</script>

<style lang="scss" scoped>
.home-container {
  padding-bottom: 50rpx;
}

.user-greeting-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%);
  color: #ffffff;
  padding: 36rpx 32rpx;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 20rpx rgba(79, 70, 229, 0.2);

  .greeting-info {
    display: flex;
    flex-direction: column;
  }

  .greeting-title {
    font-size: 34rpx;
    font-weight: 700;
    margin-bottom: 8rpx;
  }

  .greeting-subtitle {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: 8rpx;
  }

  .greeting-date {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.7);
  }

  .login-action {
    .login-badge {
      background-color: #ffffff;
      color: #4f46e5;
      font-size: 24rpx;
      font-weight: 600;
      padding: 10rpx 24rpx;
      border-radius: 30rpx;
    }
  }
}

.summary-card {
  padding: 24rpx;
}

.life-card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 24rpx;

  .view-all-link, .new-todo-link {
    font-size: 24rpx;
    color: #4f46e5;
    font-weight: 500;
  }
}

.summary-grid {
  display: flex;
  flex-wrap: wrap;
  row-gap: 20rpx;
}

.grid-item {
  width: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10rpx 0;

  .item-value {
    font-size: 38rpx;
    font-weight: 700;
    line-height: 1.2;
  }

  .item-label {
    font-size: 22rpx;
    color: #64748b;
    margin-top: 6rpx;
  }

  .text-primary { color: #4f46e5; }
  .text-success { color: #10b981; }
  .text-warning { color: #f59e0b; }
  .text-danger { color: #ef4444; }
}

/* 今日待办卡片 */
.today-todo-card {
  padding: 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;

  .empty-icon {
    font-size: 48rpx;
    margin-bottom: 12rpx;
  }

  .empty-text {
    font-size: 24rpx;
    color: #94a3b8;
    margin-bottom: 20rpx;
  }

  .empty-btn {
    font-size: 24rpx;
    color: #4f46e5;
    background-color: #eef2ff;
    border-radius: 24rpx;
    padding: 6rpx 28rpx;
    line-height: 1.6;
  }
}

.today-todo-list {
  display: flex;
  flex-direction: column;
}

.home-todo-item {
  display: flex;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f1f5f9;

  &.is-done {
    opacity: 0.6;
  }

  .checkbox {
    width: 36rpx;
    height: 36rpx;
    border: 2rpx solid #cbd5e1;
    border-radius: 50%;
    margin-right: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    &.checked {
      background-color: #10b981;
      border-color: #10b981;
    }

    .check-icon {
      color: #ffffff;
      font-size: 22rpx;
      font-weight: 700;
    }
  }

  .content {
    flex: 1;
    min-width: 0;

    .title-row {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .title {
        font-size: 26rpx;
        color: #1e293b;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        &.strikethrough {
          text-decoration: line-through;
          color: #94a3b8;
        }
      }
    }

    .time-row {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-top: 4rpx;

      .time-text {
        font-size: 20rpx;
        color: #94a3b8;
      }

      .overdue-tag {
        font-size: 18rpx;
        color: #ef4444;
        background-color: #fee2e2;
        padding: 2rpx 8rpx;
        border-radius: 4rpx;
      }
    }
  }
}

.priority-badge {
  font-size: 18rpx;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  margin-left: 10rpx;

  &.p-1 { background-color: #f1f5f9; color: #64748b; }
  &.p-2 { background-color: #eef2ff; color: #4f46e5; }
  &.p-3 { background-color: #fef3c7; color: #d97706; }
  &.p-4 { background-color: #fee2e2; color: #dc2626; }
}

.more-bar {
  text-align: center;
  padding-top: 20rpx;
  font-size: 24rpx;
  color: #4f46e5;
  font-weight: 500;
}

/* 快捷入口 */
.shortcut-card {
  padding: 24rpx;
}

.shortcut-list {
  display: flex;
  justify-content: space-around;
  padding: 10rpx 0;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  .icon-circle {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 38rpx;
    margin-bottom: 12rpx;
  }

  .shortcut-text {
    font-size: 24rpx;
    color: #475569;
  }

  .bg-blue { background-color: #eff6ff; }
  .bg-purple { background-color: #f5f3ff; }
  .bg-green { background-color: #ecfdf5; }
  .bg-orange { background-color: #fff7ed; }
}
</style>
