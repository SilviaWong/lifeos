<template>
  <view class="todos-page">
    <!-- 待办统计指标卡片 -->
    <view class="life-card summary-card">
      <view class="summary-grid">
        <view class="summary-item" @tap="switchFilter('TODAY')">
          <text class="num text-primary">{{ summary.todayTotalCount }}</text>
          <text class="label">今日待办</text>
        </view>
        <view class="summary-item" @tap="switchFilter('COMPLETED')">
          <text class="num text-success">{{ summary.todayCompletedCount }}</text>
          <text class="label">今日已完成</text>
        </view>
        <view class="summary-item" @tap="switchFilter('PENDING')">
          <text class="num text-warning">{{ summary.pendingCount }}</text>
          <text class="label">待处理</text>
        </view>
        <view class="summary-item" @tap="switchFilter('OVERDUE')">
          <text class="num text-danger">{{ summary.overdueCount }}</text>
          <text class="label">已逾期</text>
        </view>
      </view>
    </view>

    <!-- 筛选标签与操作栏 -->
    <view class="life-card search-card">
      <view class="search-box">
        <input
          v-model="queryParams.keyword"
          class="search-input"
          placeholder="搜索待办任务标题或描述..."
          confirm-type="search"
          @confirm="onSearch"
        />
        <text v-if="queryParams.keyword" class="clear-icon" @tap="clearKeyword">✕</text>
      </view>

      <view class="filter-tabs">
        <view
          v-for="tab in filterTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: queryParams.filterType === tab.value }"
          @tap="switchFilter(tab.value)"
        >
          <text>{{ tab.label }}</text>
          <text v-if="tab.value === 'OVERDUE' && summary.overdueCount > 0" class="badge-dot" />
        </view>
      </view>
    </view>

    <!-- 待办任务列表 -->
    <view class="life-card list-card">
      <view class="list-header">
        <text class="list-title">任务列表 ({{ total }})</text>
        <button class="add-btn" @tap="openCreateModal">+ 新建待办</button>
      </view>

      <!-- 加载中状态 -->
      <view v-if="loading" class="state-container">
        <text class="loading-icon">⏳</text>
        <text class="state-text">正在加载待办任务...</text>
      </view>

      <!-- 空数据状态 -->
      <view v-else-if="todoList.length === 0" class="state-container">
        <text class="empty-icon">📝</text>
        <text class="state-text">暂无相关待办任务</text>
        <button class="empty-create-btn" @tap="openCreateModal">立即创建待办</button>
      </view>

      <!-- 列表内容 -->
      <view v-else class="todo-items">
        <view
          v-for="item in todoList"
          :key="item.id"
          class="todo-item"
          :class="{ 'is-done': item.status === 'DONE' }"
        >
          <!-- 完成/取消勾选框 -->
          <view class="checkbox-wrapper" @tap="toggleTodoStatus(item)">
            <view class="custom-checkbox" :class="{ checked: item.status === 'DONE' }">
              <text v-if="item.status === 'DONE'" class="check-mark">✓</text>
            </view>
          </view>

          <!-- 任务主体信息 -->
          <view class="todo-content" @tap="openEditModal(item)">
            <view class="title-row">
              <text class="todo-title" :class="{ strikethrough: item.status === 'DONE' }">
                {{ item.title }}
              </text>
              <text class="priority-tag" :class="'priority-' + item.priority">
                {{ item.priorityDesc }}
              </text>
            </view>

            <text v-if="item.description" class="todo-desc">
              {{ item.description }}
            </text>

            <view class="meta-row">
              <view v-if="item.dueAt" class="due-tag" :class="{ overdue: item.isOverdue }">
                <text class="meta-icon">⏰</text>
                <text class="meta-text">{{ formatTime(item.dueAt) }}</text>
                <text v-if="item.isOverdue" class="overdue-text">（已逾期）</text>
              </view>
              <view v-if="item.completedAt && item.status === 'DONE'" class="completed-tag">
                <text class="meta-icon">✅</text>
                <text class="meta-text">{{ formatTime(item.completedAt) }} 完成</text>
              </view>
            </view>
          </view>

          <!-- 快捷操作按钮 -->
          <view class="todo-actions">
            <text class="action-btn edit-icon" @tap.stop="openEditModal(item)">✎</text>
            <text class="action-btn delete-icon" @tap.stop="confirmDelete(item)">🗑️</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 新建/编辑待办弹窗 Modal -->
    <view v-if="modalVisible" class="modal-mask" @tap.self="closeModal">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑待办任务' : '新建待办任务' }}</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">待办标题 <text class="required">*</text></text>
            <input
              v-model="formData.title"
              class="form-input"
              placeholder="请输入清晰的待办标题"
              maxlength="120"
            />
          </view>

          <view class="form-item">
            <text class="form-label">优先级</text>
            <view class="priority-selector">
              <view
                v-for="p in priorityOptions"
                :key="p.value"
                class="priority-option"
                :class="[{ active: formData.priority === p.value }, 'p-' + p.value]"
                @tap="formData.priority = p.value"
              >
                {{ p.label }}
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">截止日期时间（可选）</text>
            <picker mode="date" :value="pickedDate" @change="onDateChange">
              <view class="picker-display">
                <text>{{ formData.dueAt ? formData.dueAt : '点击选择截止时间 (yyyy-MM-dd HH:mm:ss)' }}</text>
                <text class="picker-arrow">📅</text>
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">详细描述（可选）</text>
            <textarea
              v-model="formData.description"
              class="form-textarea"
              placeholder="添加相关背景、要点或备注..."
              maxlength="1000"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-cancel" @tap="closeModal">取消</button>
          <button class="btn btn-submit" @tap="submitForm">确定保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import {
  createTodoApi,
  deleteTodoApi,
  getTodoListApi,
  getTodoSummaryApi,
  updateTodoApi,
  updateTodoStatusApi
} from '../../api/todo'
import type { CreateTodoDto, TodoItem, TodoQueryParams, TodoSummary } from '../../types/todo'
import { formatDateTime } from '../../utils/format'

// 筛选标签选项
const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '今日', value: 'TODAY' },
  { label: '待处理', value: 'PENDING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已逾期', value: 'OVERDUE' }
]

// 优先级选项
const priorityOptions = [
  { label: '低', value: 1 },
  { label: '中', value: 2 },
  { label: '高', value: 3 },
  { label: '紧急', value: 4 }
]

// 页面数据
const loading = ref(false)
const todoList = ref<TodoItem[]>([])
const total = ref(0)

const summary = reactive<TodoSummary>({
  totalCount: 0,
  todayTotalCount: 0,
  todayCompletedCount: 0,
  pendingCount: 0,
  completedCount: 0,
  overdueCount: 0
})

const queryParams = reactive<TodoQueryParams>({
  pageNum: 1,
  pageSize: 20,
  filterType: 'ALL',
  keyword: ''
})

// 弹窗表单控制
const modalVisible = ref(false)
const isEditing = ref(false)
const currentEditId = ref<number | null>(null)
const pickedDate = ref('')

const formData = reactive<CreateTodoDto>({
  title: '',
  description: '',
  priority: 2,
  dueAt: ''
})

// 加载待办列表
const loadTodos = async () => {
  loading.value = true
  try {
    const res = await getTodoListApi(queryParams)
    if (res.code === 0 && res.data) {
      todoList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('加载待办列表失败', err)
  } finally {
    loading.value = false
  }
}

// 加载待办统计摘要
const loadSummary = async () => {
  try {
    const res = await getTodoSummaryApi()
    if (res.code === 0 && res.data) {
      Object.assign(summary, res.data)
    }
  } catch (err) {
    console.error('加载统计摘要失败', err)
  }
}

// 刷新全部数据
const refreshAll = async () => {
  await Promise.all([loadTodos(), loadSummary()])
  uni.stopPullDownRefresh()
}

onPullDownRefresh(() => {
  refreshAll()
})

onMounted(() => {
  refreshAll()
})

// 切换筛选标签
const switchFilter = (type: string) => {
  queryParams.filterType = type
  queryParams.pageNum = 1
  loadTodos()
}

// 关键词搜索
const onSearch = () => {
  queryParams.pageNum = 1
  loadTodos()
}

const clearKeyword = () => {
  queryParams.keyword = ''
  onSearch()
}

// 快速切换任务完成状态
const toggleTodoStatus = async (item: TodoItem) => {
  const targetStatus = item.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    const res = await updateTodoStatusApi(item.id, targetStatus)
    if (res.code === 0) {
      uni.showToast({
        title: targetStatus === 'DONE' ? '恭喜完成待办 🎉' : '已重新打开待办',
        icon: 'none'
      })
      // 本地乐观更新或全量重载
      await refreshAll()
    }
  } catch (err) {
    console.error('更新待办状态失败', err)
  }
}

// 打开新建弹窗
const openCreateModal = () => {
  isEditing.value = false
  currentEditId.value = null
  formData.title = ''
  formData.description = ''
  formData.priority = 2
  formData.dueAt = ''
  pickedDate.value = ''
  modalVisible.value = true
}

// 打开编辑弹窗
const openEditModal = (item: TodoItem) => {
  isEditing.value = true
  currentEditId.value = item.id
  formData.title = item.title
  formData.description = item.description || ''
  formData.priority = item.priority || 2
  formData.dueAt = item.dueAt || ''
  pickedDate.value = item.dueAt ? item.dueAt.substring(0, 10) : ''
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
}

// 截止日期 Picker 改变事件
const onDateChange = (e: any) => {
  const dateStr = e.detail.value
  pickedDate.value = dateStr
  // 默认设置为当天 18:00:00
  formData.dueAt = `${dateStr} 18:00:00`
}

// 提交表单保存
const submitForm = async () => {
  if (!formData.title || !formData.title.trim()) {
    uni.showToast({ title: '请输入待办标题', icon: 'none' })
    return
  }

  const payload = {
    title: formData.title.trim(),
    description: formData.description ? formData.description.trim() : undefined,
    priority: formData.priority,
    dueAt: formData.dueAt ? formData.dueAt : null
  }

  try {
    if (isEditing.value && currentEditId.value) {
      const res = await updateTodoApi(currentEditId.value, payload)
      if (res.code === 0) {
        uni.showToast({ title: '更新成功', icon: 'success' })
        closeModal()
        await refreshAll()
      }
    } else {
      const res = await createTodoApi(payload)
      if (res.code === 0) {
        uni.showToast({ title: '创建成功', icon: 'success' })
        closeModal()
        await refreshAll()
      }
    }
  } catch (err) {
    console.error('保存待办失败', err)
  }
}

// 确认删除待办
const confirmDelete = (item: TodoItem) => {
  uni.showModal({
    title: '删除确认',
    content: `确定删除待办「${item.title}」吗？`,
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const apiRes = await deleteTodoApi(item.id)
          if (apiRes.code === 0) {
            uni.showToast({ title: '删除成功', icon: 'none' })
            await refreshAll()
          }
        } catch (err) {
          console.error('删除待办失败', err)
        }
      }
    }
  })
}

const formatTime = (timeStr?: string | null) => {
  if (!timeStr) return ''
  return formatDateTime(timeStr, 'MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.todos-page {
  padding-bottom: 50rpx;
}

/* 统计卡片 */
.summary-card {
  padding: 24rpx;
}

.summary-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  .num {
    font-size: 40rpx;
    font-weight: 700;
    line-height: 1.2;
  }

  .label {
    font-size: 22rpx;
    color: #64748b;
    margin-top: 6rpx;
  }

  .text-primary { color: #4f46e5; }
  .text-success { color: #10b981; }
  .text-warning { color: #f59e0b; }
  .text-danger { color: #ef4444; }
}

/* 搜索与筛选卡片 */
.search-card {
  padding: 20rpx 24rpx;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background-color: #f1f5f9;
  border-radius: 12rpx;
  padding: 14rpx 20rpx;
  margin-bottom: 20rpx;

  .search-input {
    flex: 1;
    font-size: 26rpx;
    color: #1e293b;
  }

  .clear-icon {
    font-size: 26rpx;
    color: #94a3b8;
    padding: 0 10rpx;
  }
}

.filter-tabs {
  display: flex;
  justify-content: space-between;
  border-top: 1rpx solid #f1f5f9;
  padding-top: 16rpx;
}

.tab-item {
  position: relative;
  font-size: 26rpx;
  color: #64748b;
  padding: 10rpx 16rpx;
  border-radius: 8rpx;
  font-weight: 500;
  transition: all 0.2s ease;

  &.active {
    color: #4f46e5;
    background-color: #eef2ff;
    font-weight: 600;
  }

  .badge-dot {
    position: absolute;
    top: 6rpx;
    right: 2rpx;
    width: 12rpx;
    height: 12rpx;
    background-color: #ef4444;
    border-radius: 50%;
  }
}

/* 任务列表卡片 */
.list-card {
  padding: 24rpx;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;

  .list-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1e293b;
  }

  .add-btn {
    font-size: 24rpx;
    color: #ffffff;
    background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%);
    border-radius: 30rpx;
    padding: 8rpx 24rpx;
    line-height: 1.5;
    margin: 0;
  }
}

/* 列表状态 */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;

  .loading-icon, .empty-icon {
    font-size: 64rpx;
    margin-bottom: 16rpx;
  }

  .state-text {
    font-size: 26rpx;
    color: #94a3b8;
  }

  .empty-create-btn {
    margin-top: 24rpx;
    font-size: 26rpx;
    color: #4f46e5;
    background-color: #eef2ff;
    border-radius: 24rpx;
    padding: 10rpx 32rpx;
    line-height: 1.5;
  }
}

/* 待办单项 */
.todo-item {
  display: flex;
  align-items: flex-start;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }

  &.is-done {
    opacity: 0.65;
  }
}

.checkbox-wrapper {
  padding: 4rpx 16rpx 0 0;
}

.custom-checkbox {
  width: 38rpx;
  height: 38rpx;
  border: 2rpx solid #cbd5e1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &.checked {
    background-color: #10b981;
    border-color: #10b981;
  }

  .check-mark {
    color: #ffffff;
    font-size: 24rpx;
    font-weight: 700;
  }
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;

  .todo-title {
    font-size: 28rpx;
    font-weight: 500;
    color: #1e293b;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.strikethrough {
      text-decoration: line-through;
      color: #94a3b8;
    }
  }
}

.priority-tag {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  margin-left: 12rpx;
  font-weight: 500;

  &.priority-1 { background-color: #f1f5f9; color: #64748b; }
  &.priority-2 { background-color: #eef2ff; color: #4f46e5; }
  &.priority-3 { background-color: #fef3c7; color: #d97706; }
  &.priority-4 { background-color: #fee2e2; color: #dc2626; }
}

.todo-desc {
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.due-tag, .completed-tag {
  display: flex;
  align-items: center;
  font-size: 22rpx;
  color: #64748b;

  .meta-icon {
    font-size: 22rpx;
    margin-right: 6rpx;
  }

  &.overdue {
    color: #ef4444;
    font-weight: 500;
  }
}

.todo-actions {
  display: flex;
  align-items: center;
  padding-left: 16rpx;

  .action-btn {
    font-size: 32rpx;
    padding: 8rpx;
    color: #94a3b8;

    &:active {
      opacity: 0.7;
    }
  }

  .delete-icon {
    font-size: 28rpx;
    margin-left: 8rpx;
  }
}

/* 弹窗 Modal 样式 */
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
  width: 100%;
  background-color: #ffffff;
  border-top-left-radius: 32rpx;
  border-top-right-radius: 32rpx;
  padding: 32rpx 32rpx 60rpx;
  max-height: 85vh;
  box-sizing: border-box;
  animation: slideUp 0.25s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;

  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1e293b;
  }

  .modal-close {
    font-size: 36rpx;
    color: #94a3b8;
    padding: 4rpx;
  }
}

.modal-body {
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 24rpx;

  .form-label {
    font-size: 26rpx;
    font-weight: 500;
    color: #334155;
    margin-bottom: 12rpx;
    display: block;

    .required {
      color: #ef4444;
    }
  }

  .form-input {
    width: 100%;
    height: 80rpx;
    background-color: #f8fafc;
    border: 1rpx solid #e2e8f0;
    border-radius: 12rpx;
    padding: 0 20rpx;
    font-size: 28rpx;
    color: #1e293b;
    box-sizing: border-box;
  }

  .form-textarea {
    width: 100%;
    height: 140rpx;
    background-color: #f8fafc;
    border: 1rpx solid #e2e8f0;
    border-radius: 12rpx;
    padding: 16rpx 20rpx;
    font-size: 28rpx;
    color: #1e293b;
    box-sizing: border-box;
  }
}

.priority-selector {
  display: flex;
  gap: 16rpx;
}

.priority-option {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  font-size: 26rpx;
  border-radius: 10rpx;
  border: 1rpx solid #e2e8f0;
  color: #64748b;
  background-color: #f8fafc;

  &.active {
    font-weight: 600;
    &.p-1 { border-color: #94a3b8; color: #475569; background-color: #f1f5f9; }
    &.p-2 { border-color: #4f46e5; color: #4f46e5; background-color: #eef2ff; }
    &.p-3 { border-color: #f59e0b; color: #d97706; background-color: #fef3c7; }
    &.p-4 { border-color: #ef4444; color: #dc2626; background-color: #fee2e2; }
  }
}

.picker-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80rpx;
  background-color: #f8fafc;
  border: 1rpx solid #e2e8f0;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #475569;
}

.modal-footer {
  display: flex;
  gap: 20rpx;

  .btn {
    flex: 1;
    height: 84rpx;
    line-height: 84rpx;
    font-size: 28rpx;
    border-radius: 16rpx;
    font-weight: 500;
  }

  .btn-cancel {
    background-color: #f1f5f9;
    color: #475569;
  }

  .btn-submit {
    background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%);
    color: #ffffff;
  }
}
</style>
