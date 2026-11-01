<template>
  <view class="notifications-page">
    <!-- 顶部统计与快捷操作卡片 -->
    <view class="life-card header-card">
      <view class="header-main">
        <view class="title-meta">
          <text class="page-title">🔔 消息通知</text>
          <text class="page-subtitle">站内提醒中心，重要事项不再错过</text>
        </view>
        <view class="header-actions">
          <button v-if="unreadCount > 0" class="btn-read-all" @tap="handleMarkAllAsRead">
            一键已读
          </button>
          <button class="btn-trigger" @tap="handleTriggerScan">
            立即检查
          </button>
        </view>
      </view>

      <view class="stat-cells-bar">
        <view class="stat-cell" :class="{ highlight: unreadCount > 0 }">
          <text class="cell-num">{{ unreadCount }}</text>
          <text class="cell-label">未读通知</text>
        </view>
        <view class="cell-divider" />
        <view class="stat-cell">
          <text class="cell-num">{{ todayCount }}</text>
          <text class="cell-label">今日接收</text>
        </view>
        <view class="cell-divider" />
        <view class="stat-cell">
          <text class="cell-num text-muted">{{ totalMessages }}</text>
          <text class="cell-label">累计消息</text>
        </view>
      </view>
    </view>

    <!-- 筛选卡片 -->
    <view class="life-card filter-card">
      <view class="status-tab-bar">
        <view
          class="status-tab"
          :class="{ active: currentStatus === 'ALL' }"
          @tap="switchStatusTab('ALL')"
        >
          全部
        </view>
        <view
          class="status-tab"
          :class="{ active: currentStatus === 'UNREAD' }"
          @tap="switchStatusTab('UNREAD')"
        >
          未读
          <text v-if="unreadCount > 0" class="tab-badge">{{ unreadCount }}</text>
        </view>
        <view
          class="status-tab"
          :class="{ active: currentStatus === 'READ' }"
          @tap="switchStatusTab('READ')"
        >
          已读
        </view>
      </view>

      <!-- 消息类型快捷筛选 -->
      <view class="type-chips-row">
        <view
          v-for="t in typeOptions"
          :key="t.value"
          class="type-chip"
          :class="{ active: selectedType === t.value }"
          @tap="selectType(t.value)"
        >
          {{ t.icon }} {{ t.label }}
        </view>
      </view>
    </view>

    <!-- 消息列表 -->
    <view class="message-list-container">
      <view v-if="loading && messages.length === 0" class="loading-state">
        <text>正在加载消息...</text>
      </view>

      <view v-else-if="messages.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">当前暂无通知消息</text>
        <text class="empty-hint">在「提醒规则」中配置后，系统会自动在此生成站内提醒</text>
      </view>

      <view
        v-for="msg in messages"
        :key="msg.id"
        class="life-card message-card"
        :class="{ unread: !msg.isRead }"
        @tap="handleCardClick(msg)"
      >
        <view class="msg-header">
          <view class="type-tag-box">
            <text class="type-icon">{{ getTypeIcon(msg.messageType) }}</text>
            <text class="type-name">{{ msg.messageTypeDesc || msg.messageType }}</text>
            <text v-if="!msg.isRead" class="unread-dot" />
          </view>
          <text class="msg-time">{{ formatTime(msg.sentAt || msg.createdAt) }}</text>
        </view>

        <view class="msg-title-row">
          <text class="msg-title" :class="{ 'font-bold': !msg.isRead }">{{ msg.title }}</text>
        </view>

        <view v-if="msg.content" class="msg-body">
          <text class="msg-content">{{ msg.content }}</text>
        </view>

        <view class="msg-footer" @tap.stop>
          <view class="footer-left">
            <text class="status-indicator">{{ msg.isRead ? '已读' : '未读' }}</text>
          </view>
          <view class="footer-right">
            <button v-if="!msg.isRead" class="action-btn" @tap="handleMarkRead(msg)">
              标为已读
            </button>
            <button class="action-btn text-danger" @tap="handleDeleteMessage(msg)">
              删除
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getReminderMessagesApi,
  getUnreadCountApi,
  getReminderSummaryApi,
  markAsReadApi,
  markAllAsReadApi,
  deleteReminderMessageApi,
  triggerReminderScanApi
} from '../../api/reminder'
import type { ReminderMessage } from '../../types/reminder'

const typeOptions = [
  { value: '', label: '全部类型', icon: '🔍' },
  { value: 'TODO', label: '待办', icon: '📋' },
  { value: 'HABIT', label: '习惯', icon: '🏃' },
  { value: 'GOAL', label: '目标', icon: '🎯' },
  { value: 'SYSTEM', label: '系统', icon: '🔔' }
]

const currentStatus = ref<'ALL' | 'UNREAD' | 'READ'>('ALL')
const selectedType = ref('')
const loading = ref(false)

const messages = ref<ReminderMessage[]>([])
const unreadCount = ref(0)
const todayCount = ref(0)
const totalMessages = ref(0)

const getTypeIcon = (type: string) => {
  if (type === 'TODO') return '📋'
  if (type === 'HABIT') return '🏃'
  if (type === 'GOAL') return '🎯'
  if (type === 'ITEM_DUE') return '📦'
  return '🔔'
}

const formatTime = (timeStr?: string) => {
  if (!timeStr) return ''
  return timeStr.substring(0, 16)
}

const loadSummary = async () => {
  try {
    const res = await getReminderSummaryApi()
    if (res.data) {
      unreadCount.value = res.data.unreadMessagesCount
      todayCount.value = res.data.todayMessagesCount
    }
  } catch (err) {
    console.error('加载提醒概览失败', err)
  }
}

const loadMessages = async () => {
  loading.value = true
  try {
    const params: {
      status?: string
      messageType?: string
      current?: number
      size?: number
    } = {
      current: 1,
      size: 100
    }

    if (currentStatus.value !== 'ALL') {
      params.status = currentStatus.value
    }

    if (selectedType.value) {
      params.messageType = selectedType.value
    }

    const res = await getReminderMessagesApi(params)
    if (res.data && res.data.records) {
      messages.value = res.data.records
      totalMessages.value = res.data.total
    } else {
      messages.value = []
      totalMessages.value = 0
    }
  } catch (err) {
    uni.showToast({ title: '加载消息失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const switchStatusTab = (status: 'ALL' | 'UNREAD' | 'READ') => {
  currentStatus.value = status
  loadMessages()
}

const selectType = (type: string) => {
  selectedType.value = type
  loadMessages()
}

const handleCardClick = async (msg: ReminderMessage) => {
  if (!msg.isRead) {
    await handleMarkRead(msg)
  }
}

const handleMarkRead = async (msg: ReminderMessage) => {
  try {
    await markAsReadApi(msg.id)
    msg.isRead = true
    msg.status = 'READ'
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
    uni.showToast({ title: '已标为已读', icon: 'none' })
  } catch (err: any) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const handleMarkAllAsRead = async () => {
  try {
    const res = await markAllAsReadApi()
    uni.showToast({ title: `已将全部 ${res.data || 0} 条标为已读`, icon: 'success' })
    unreadCount.value = 0
    loadMessages()
    loadSummary()
  } catch (err: any) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const handleDeleteMessage = (msg: ReminderMessage) => {
  uni.showModal({
    title: '删除确认',
    content: `确定删除该条通知吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteReminderMessageApi(msg.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadMessages()
          loadSummary()
        } catch (err: any) {
          uni.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const handleTriggerScan = async () => {
  try {
    uni.showLoading({ title: '正在扫描待办与习惯...' })
    const res = await triggerReminderScanApi()
    uni.hideLoading()
    const count = res.data || 0
    uni.showToast({
      title: count > 0 ? `扫描完成，新增 ${count} 条新提醒` : '无新增待触发提醒',
      icon: 'none'
    })
    loadMessages()
    loadSummary()
  } catch (err: any) {
    uni.hideLoading()
    uni.showToast({ title: err.message || '扫描失败', icon: 'none' })
  }
}

onMounted(() => {
  loadSummary()
  loadMessages()
})
</script>

<style lang="scss" scoped>
.notifications-page {
  padding: 24rpx;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.header-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.page-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f1f1f;
  display: block;
}

.page-subtitle {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
  display: block;
}

.header-actions {
  display: flex;
  gap: 12rpx;
}

.btn-read-all {
  background-color: #f5f5f5;
  color: #1677ff;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  line-height: 1.6;
}

.btn-trigger {
  background-color: #1677ff;
  color: #ffffff;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  line-height: 1.6;
}

.stat-cells-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 16rpx 0;
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;

  &.highlight .cell-num {
    color: #ff4d4f;
  }
}

.cell-num {
  font-size: 34rpx;
  font-weight: bold;
  color: #262626;
}

.cell-label {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
}

.cell-divider {
  width: 1rpx;
  height: 40rpx;
  background-color: #f0f0f0;
}

.filter-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.status-tab-bar {
  display: flex;
  border-bottom: 1rpx solid #f0f0f0;
  padding-bottom: 16rpx;
  margin-bottom: 16rpx;
}

.status-tab {
  flex: 1;
  text-align: center;
  font-size: 26rpx;
  color: #595959;
  padding: 6rpx 0;
  position: relative;

  &.active {
    color: #1677ff;
    font-weight: bold;

    &::after {
      content: '';
      position: absolute;
      bottom: -16rpx;
      left: 25%;
      width: 50%;
      height: 4rpx;
      background-color: #1677ff;
      border-radius: 2rpx;
    }
  }
}

.tab-badge {
  font-size: 20rpx;
  background-color: #ff4d4f;
  color: #ffffff;
  padding: 2rpx 10rpx;
  border-radius: 16rpx;
  margin-left: 6rpx;
}

.type-chips-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.type-chip {
  font-size: 22rpx;
  background-color: #f5f5f5;
  color: #595959;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;

  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
    font-weight: bold;
  }
}

.message-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;

  &.unread {
    border-left: 8rpx solid #1677ff;
    background-color: #fcfeff;
  }
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.type-tag-box {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.type-icon {
  font-size: 26rpx;
}

.type-name {
  font-size: 22rpx;
  color: #1677ff;
  background-color: #e6f4ff;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
}

.unread-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 6rpx;
  background-color: #ff4d4f;
}

.msg-time {
  font-size: 22rpx;
  color: #bfbfbf;
}

.msg-title-row {
  margin-bottom: 10rpx;
}

.msg-title {
  font-size: 30rpx;
  color: #1f1f1f;

  &.font-bold {
    font-weight: bold;
  }
}

.msg-body {
  background-color: #fafafa;
  border-radius: 10rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

.msg-content {
  font-size: 26rpx;
  color: #595959;
  line-height: 1.6;
}

.msg-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 12rpx;
}

.status-indicator {
  font-size: 22rpx;
  color: #8c8c8c;
}

.footer-right {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  font-size: 24rpx;
  color: #1677ff;
  background: none;
  padding: 0 8rpx;

  &.text-danger {
    color: #ff4d4f;
  }
}

.empty-state, .loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-hint {
  font-size: 22rpx;
  color: #bfbfbf;
  margin-top: 8rpx;
}

.text-muted {
  color: #8c8c8c;
}
</style>

