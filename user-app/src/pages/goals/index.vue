<template>
  <view class="goals-page">
    <!-- 统计指标概览卡片 -->
    <view class="life-card summary-card">
      <view class="summary-grid">
        <view class="summary-item" @tap="switchFilter('ACTIVE')">
          <text class="num text-primary">{{ summary.activeCount }}</text>
          <text class="label">进行中目标</text>
        </view>
        <view class="summary-item" @tap="switchFilter('COMPLETED')">
          <text class="num text-success">{{ summary.completedCount }}</text>
          <text class="label">已达成目标</text>
        </view>
        <view class="summary-item" @tap="switchFilter('ALL')">
          <text class="num text-info">{{ summary.avgProgress }}%</text>
          <text class="label">活跃平均进度</text>
        </view>
        <view class="summary-item" @tap="switchFilter('ARCHIVED')">
          <text class="num text-muted">{{ summary.archivedCount }}</text>
          <text class="label">已归档目标</text>
        </view>
      </view>
    </view>

    <!-- 筛选标签与操作栏 -->
    <view class="life-card search-card">
      <view class="search-box">
        <input
          v-model="queryParams.keyword"
          class="search-input"
          placeholder="搜索目标名称或描述..."
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
        </view>
      </view>
    </view>

    <!-- 目标列表卡片 -->
    <view class="life-card list-card">
      <view class="list-header">
        <text class="list-title">目标清单 ({{ total }})</text>
        <button class="add-btn" @tap="openCreateGoalModal">+ 新建目标</button>
      </view>

      <!-- 加载中 -->
      <view v-if="loading" class="state-container">
        <text class="loading-icon">⏳</text>
        <text class="state-text">正在加载目标与子任务...</text>
      </view>

      <!-- 空数据 -->
      <view v-else-if="goalList.length === 0" class="state-container">
        <text class="empty-icon">🎯</text>
        <text class="state-text">暂无相关生活目标</text>
        <button class="empty-create-btn" @tap="openCreateGoalModal">立即制定新目标</button>
      </view>

      <!-- 目标卡片列表 -->
      <view v-else class="goal-list">
        <view
          v-for="goal in goalList"
          :key="goal.id"
          class="goal-card"
          :class="{ 'is-completed': goal.status === 'COMPLETED', 'is-archived': goal.status === 'ARCHIVED' }"
        >
          <!-- 目标卡片头部 -->
          <view class="goal-header">
            <view class="title-wrap">
              <text class="goal-title">{{ goal.title }}</text>
              <text class="priority-badge" :class="'p-' + goal.priority">{{ goal.priorityDesc }}</text>
            </view>
            <view class="status-badge" :class="'status-' + goal.status.toLowerCase()">
              {{ goal.statusDesc }}
            </view>
          </view>

          <!-- 描述 -->
          <text v-if="goal.description" class="goal-desc">{{ goal.description }}</text>

          <!-- 日期范围 -->
          <view v-if="goal.startDate || goal.dueDate" class="date-row">
            <text class="date-icon">📅</text>
            <text class="date-text">
              {{ goal.startDate ? goal.startDate : '待定' }} ~ {{ goal.dueDate ? goal.dueDate : '长期' }}
            </text>
          </view>

          <!-- 动态进度条 -->
          <view class="progress-section">
            <view class="progress-bar-bg">
              <view
                class="progress-bar-fill"
                :style="{ width: goal.totalTaskCount > 0 ? goal.progress + '%' : '0%' }"
              />
            </view>
            <view class="progress-meta">
              <text class="progress-text" :class="{ 'not-dissected': goal.totalTaskCount === 0 }">
                进度: {{ goal.progressText }}
              </text>
              <text class="tasks-count">
                {{ goal.completedTaskCount }} / {{ goal.totalTaskCount }} 子任务
              </text>
            </view>
          </view>

          <!-- 展开/折叠子任务触发区 -->
          <view class="subtask-toggle-bar" @tap="toggleExpandGoal(goal.id)">
            <text class="toggle-text">
              {{ expandedGoalIds.includes(goal.id) ? '收起子任务拆解 ▴' : '展开子任务拆解 ▾' }}
            </text>
          </view>

          <!-- 子任务拆解展开内容 -->
          <view v-if="expandedGoalIds.includes(goal.id)" class="subtasks-container">
            <!-- 子任务列表 -->
            <view v-if="goalSubtasks[goal.id] && goalSubtasks[goal.id].length > 0" class="subtask-list">
              <view
                v-for="task in goalSubtasks[goal.id]"
                :key="task.id"
                class="subtask-item"
                :class="{ 'subtask-done': task.status === 'DONE' }"
              >
                <!-- 勾选框 -->
                <view class="subtask-checkbox" :class="{ checked: task.status === 'DONE' }" @tap="toggleSubtask(goal, task)">
                  <text v-if="task.status === 'DONE'" class="check-icon">✓</text>
                </view>

                <!-- 任务标题与信息 -->
                <view class="subtask-content">
                  <text class="subtask-title" :class="{ strikethrough: task.status === 'DONE' }">
                    {{ task.title }}
                  </text>
                  <text v-if="task.description" class="subtask-desc">{{ task.description }}</text>
                </view>

                <!-- 删除按钮 -->
                <text class="delete-task-btn" @tap="confirmDeleteTask(goal, task)">✕</text>
              </view>
            </view>

            <view v-else class="subtask-empty">
              <text class="empty-tip">尚未添加子任务，添加后自动计算达成进度</text>
            </view>

            <!-- 快捷添加子任务输入条 -->
            <view class="quick-add-task-bar">
              <input
                v-model="newSubtaskInputs[goal.id]"
                class="quick-input"
                placeholder="输入子任务标题回车添加..."
                confirm-type="done"
                @confirm="submitQuickSubtask(goal)"
              />
              <button class="quick-add-btn" @tap="submitQuickSubtask(goal)">添加</button>
            </view>
          </view>

          <!-- 卡片操作底部工具条 -->
          <view class="card-footer-actions">
            <!-- 状态快速流转 -->
            <view class="status-actions">
              <button
                v-if="goal.status !== 'COMPLETED'"
                class="action-btn complete-btn"
                @tap="changeGoalStatus(goal, 'COMPLETED')"
              >
                标记达成
              </button>
              <button
                v-else
                class="action-btn reopen-btn"
                @tap="changeGoalStatus(goal, 'IN_PROGRESS')"
              >
                重新激活
              </button>
              <button
                v-if="goal.status !== 'ARCHIVED'"
                class="action-btn archive-btn"
                @tap="changeGoalStatus(goal, 'ARCHIVED')"
              >
                归档
              </button>
            </view>

            <view class="crud-actions">
              <text class="icon-btn edit-btn" @tap="openEditGoalModal(goal)">✎ 编辑</text>
              <text class="icon-btn delete-btn" @tap="confirmDeleteGoal(goal)">🗑️ 删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 新建/编辑目标弹窗 Modal -->
    <view v-if="modalVisible" class="modal-mask" @tap.self="closeModal">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑目标' : '新建目标' }}</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">目标标题 <text class="required">*</text></text>
            <input
              v-model="formData.title"
              class="form-input"
              placeholder="请输入明确具体的生活/工作目标"
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

          <view class="form-item date-range-item">
            <view class="date-picker-col">
              <text class="form-label">开始日期（可选）</text>
              <picker mode="date" :value="formData.startDate || ''" @change="onStartDateChange">
                <view class="picker-display">
                  <text>{{ formData.startDate ? formData.startDate : '点击选择' }}</text>
                </view>
              </picker>
            </view>
            <view class="date-picker-col">
              <text class="form-label">截止日期（可选）</text>
              <picker mode="date" :value="formData.dueDate || ''" @change="onDueDateChange">
                <view class="picker-display">
                  <text>{{ formData.dueDate ? formData.dueDate : '点击选择' }}</text>
                </view>
              </picker>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">目标描述与关键要点（可选）</text>
            <textarea
              v-model="formData.description"
              class="form-textarea"
              placeholder="描述目标的期望价值或达成标准..."
              maxlength="1000"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-cancel" @tap="closeModal">取消</button>
          <button class="btn btn-submit" @tap="submitGoalForm">确定保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import {
  createGoalApi,
  createGoalTaskApi,
  deleteGoalApi,
  deleteGoalTaskApi,
  getGoalDetailApi,
  getGoalListApi,
  getGoalSummaryApi,
  getGoalTasksApi,
  updateGoalApi,
  updateGoalStatusApi,
  updateGoalTaskStatusApi
} from '../../api/goal'
import type {
  CreateGoalDto,
  GoalItem,
  GoalQueryParams,
  GoalSummary,
  GoalTaskItem
} from '../../types/goal'

const filterTabs = [
  { label: '活跃中', value: 'ACTIVE' },
  { label: '全部', value: 'ALL' },
  { label: '已达成', value: 'COMPLETED' },
  { label: '已归档', value: 'ARCHIVED' }
]

const priorityOptions = [
  { label: '高优先级', value: 1 },
  { label: '中优先级', value: 2 },
  { label: '低优先级', value: 3 }
]

const loading = ref(false)
const goalList = ref<GoalItem[]>([])
const total = ref(0)

const summary = reactive<GoalSummary>({
  totalCount: 0,
  activeCount: 0,
  completedCount: 0,
  archivedCount: 0,
  avgProgress: 0
})

const queryParams = reactive<GoalQueryParams>({
  pageNum: 1,
  pageSize: 20,
  filterType: 'ACTIVE',
  keyword: ''
})

// 展开子任务的 Goal ID 集合
const expandedGoalIds = ref<number[]>([])
// 各目标子任务字典
const goalSubtasks = reactive<Record<number, GoalTaskItem[]>>({})
// 各目标快捷输入框绑定
const newSubtaskInputs = reactive<Record<number, string>>({})

// 弹窗控制
const modalVisible = ref(false)
const isEditing = ref(false)
const currentEditId = ref<number | null>(null)

const formData = reactive<CreateGoalDto>({
  title: '',
  description: '',
  priority: 2,
  startDate: '',
  dueDate: ''
})

const loadGoals = async () => {
  loading.value = true
  try {
    const res = await getGoalListApi(queryParams)
    if (res.code === 0 && res.data) {
      goalList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('加载目标列表失败', err)
  } finally {
    loading.value = false
  }
}

const loadSummary = async () => {
  try {
    const res = await getGoalSummaryApi()
    if (res.code === 0 && res.data) {
      Object.assign(summary, res.data)
    }
  } catch (err) {
    console.error('加载目标统计失败', err)
  }
}

const refreshAll = async () => {
  await Promise.all([loadGoals(), loadSummary()])
  // 如果有展开的目标，顺带刷新展开目标的子任务
  for (const goalId of expandedGoalIds.value) {
    await fetchSubtasks(goalId)
  }
  uni.stopPullDownRefresh()
}

onPullDownRefresh(() => {
  refreshAll()
})

onMounted(() => {
  refreshAll()
})

const switchFilter = (type: string) => {
  queryParams.filterType = type
  queryParams.pageNum = 1
  loadGoals()
}

const onSearch = () => {
  queryParams.pageNum = 1
  loadGoals()
}

const clearKeyword = () => {
  queryParams.keyword = ''
  onSearch()
}

// 展开/收起子任务
const toggleExpandGoal = async (goalId: number) => {
  const index = expandedGoalIds.value.indexOf(goalId)
  if (index > -1) {
    expandedGoalIds.value.splice(index, 1)
  } else {
    expandedGoalIds.value.push(goalId)
    await fetchSubtasks(goalId)
  }
}

const fetchSubtasks = async (goalId: number) => {
  try {
    const res = await getGoalTasksApi(goalId)
    if (res.code === 0 && res.data) {
      goalSubtasks[goalId] = res.data
    }
  } catch (err) {
    console.error('加载子任务失败', err)
  }
}

// 快速创建子任务
const submitQuickSubtask = async (goal: GoalItem) => {
  const inputTitle = (newSubtaskInputs[goal.id] || '').trim()
  if (!inputTitle) {
    uni.showToast({ title: '请输入子任务标题', icon: 'none' })
    return
  }

  try {
    const res = await createGoalTaskApi(goal.id, { title: inputTitle })
    if (res.code === 0) {
      newSubtaskInputs[goal.id] = ''
      uni.showToast({ title: '子任务已添加', icon: 'none' })
      // 重新拉取该目标详情与统计
      await updateSingleGoal(goal.id)
      await fetchSubtasks(goal.id)
      await loadSummary()
    }
  } catch (err) {
    console.error('添加子任务失败', err)
  }
}

// 切换子任务完成状态
const toggleSubtask = async (goal: GoalItem, task: GoalTaskItem) => {
  const targetStatus = task.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    const res = await updateGoalTaskStatusApi(goal.id, task.id, targetStatus)
    if (res.code === 0) {
      // 局部更新该任务
      task.status = targetStatus
      // 服务端已自动重算目标进度，重新拉取该目标详情更新进度条
      await updateSingleGoal(goal.id)
      await loadSummary()
    }
  } catch (err) {
    console.error('更新子任务状态失败', err)
  }
}

// 删除子任务
const confirmDeleteTask = (goal: GoalItem, task: GoalTaskItem) => {
  uni.showModal({
    title: '删除子任务',
    content: `确定删除子任务「${task.title}」吗？`,
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const apiRes = await deleteGoalTaskApi(goal.id, task.id)
          if (apiRes.code === 0) {
            await updateSingleGoal(goal.id)
            await fetchSubtasks(goal.id)
            await loadSummary()
          }
        } catch (err) {
          console.error('删除子任务失败', err)
        }
      }
    }
  })
}

// 单个目标重新拉取并就地替换
const updateSingleGoal = async (goalId: number) => {
  try {
    const res = await getGoalDetailApi(goalId)
    if (res.code === 0 && res.data) {
      const idx = goalList.value.findIndex(g => g.id === goalId)
      if (idx > -1) {
        goalList.value[idx] = res.data
      }
    }
  } catch (err) {
    console.error('刷新单个目标失败', err)
  }
}

// 变更目标状态
const changeGoalStatus = async (goal: GoalItem, targetStatus: string) => {
  try {
    const res = await updateGoalStatusApi(goal.id, targetStatus)
    if (res.code === 0) {
      uni.showToast({
        title: targetStatus === 'COMPLETED' ? '达成目标！恭喜 🎉' : '状态已更新',
        icon: 'none'
      })
      await refreshAll()
    }
  } catch (err) {
    console.error('更新目标状态失败', err)
  }
}

// 目标创建 Modal
const openCreateGoalModal = () => {
  isEditing.value = false
  currentEditId.value = null
  formData.title = ''
  formData.description = ''
  formData.priority = 2
  formData.startDate = ''
  formData.dueDate = ''
  modalVisible.value = true
}

// 目标编辑 Modal
const openEditGoalModal = (goal: GoalItem) => {
  isEditing.value = true
  currentEditId.value = goal.id
  formData.title = goal.title
  formData.description = goal.description || ''
  formData.priority = goal.priority || 2
  formData.startDate = goal.startDate || ''
  formData.dueDate = goal.dueDate || ''
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
}

const onStartDateChange = (e: any) => {
  formData.startDate = e.detail.value
}

const onDueDateChange = (e: any) => {
  formData.dueDate = e.detail.value
}

// 提交目标表单
const submitGoalForm = async () => {
  if (!formData.title || !formData.title.trim()) {
    uni.showToast({ title: '请输入目标标题', icon: 'none' })
    return
  }

  const payload: CreateGoalDto = {
    title: formData.title.trim(),
    description: formData.description ? formData.description.trim() : undefined,
    priority: formData.priority,
    startDate: formData.startDate || null,
    dueDate: formData.dueDate || null
  }

  try {
    if (isEditing.value && currentEditId.value) {
      const res = await updateGoalApi(currentEditId.value, payload)
      if (res.code === 0) {
        uni.showToast({ title: '目标已更新', icon: 'success' })
        closeModal()
        await refreshAll()
      }
    } else {
      const res = await createGoalApi(payload)
      if (res.code === 0) {
        uni.showToast({ title: '目标制定成功', icon: 'success' })
        closeModal()
        await refreshAll()
      }
    }
  } catch (err) {
    console.error('保存目标失败', err)
  }
}

// 确认删除目标
const confirmDeleteGoal = (goal: GoalItem) => {
  uni.showModal({
    title: '删除目标确认',
    content: `删除目标「${goal.title}」将一并清除其子任务，关联的待办事项将安全保留并解除关联。确定删除吗？`,
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          const apiRes = await deleteGoalApi(goal.id)
          if (apiRes.code === 0) {
            uni.showToast({ title: '目标已删除', icon: 'none' })
            await refreshAll()
          }
        } catch (err) {
          console.error('删除目标失败', err)
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.goals-page {
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
  .text-info { color: #0284c7; }
  .text-muted { color: #94a3b8; }
}

/* 搜索卡片 */
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
  font-size: 26rpx;
  color: #64748b;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
  font-weight: 500;

  &.active {
    color: #4f46e5;
    background-color: #eef2ff;
    font-weight: 600;
  }
}

/* 列表卡片 */
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

/* 目标卡片单项 */
.goal-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.goal-card {
  background-color: #f8fafc;
  border: 1rpx solid #e2e8f0;
  border-radius: 16rpx;
  padding: 24rpx;
  transition: all 0.2s ease;

  &.is-completed {
    border-color: #d1fae5;
    background-color: #f0fdf4;
  }

  &.is-archived {
    opacity: 0.7;
    background-color: #f1f5f9;
  }
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;

  .title-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    min-width: 0;

    .goal-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #1e293b;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.priority-badge {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  margin-left: 12rpx;
  font-weight: 500;

  &.p-1 { background-color: #fee2e2; color: #dc2626; }
  &.p-2 { background-color: #eef2ff; color: #4f46e5; }
  &.p-3 { background-color: #f1f5f9; color: #64748b; }
}

.status-badge {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
  font-weight: 500;

  &.status-not_started { background-color: #f1f5f9; color: #64748b; }
  &.status-in_progress { background-color: #eef2ff; color: #4f46e5; }
  &.status-completed { background-color: #dcfce7; color: #15803d; }
  &.status-archived { background-color: #e2e8f0; color: #475569; }
}

.goal-desc {
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 14rpx;
  display: block;
}

.date-row {
  display: flex;
  align-items: center;
  font-size: 22rpx;
  color: #64748b;
  margin-bottom: 16rpx;

  .date-icon {
    font-size: 22rpx;
    margin-right: 8rpx;
  }
}

/* 进度条 */
.progress-section {
  margin-bottom: 16rpx;

  .progress-bar-bg {
    width: 100%;
    height: 14rpx;
    background-color: #e2e8f0;
    border-radius: 7rpx;
    overflow: hidden;
    margin-bottom: 8rpx;
  }

  .progress-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #4f46e5 0%, #10b981 100%);
    border-radius: 7rpx;
    transition: width 0.3s ease;
  }

  .progress-meta {
    display: flex;
    justify-content: space-between;
    font-size: 22rpx;
    color: #475569;

    .progress-text {
      font-weight: 600;

      &.not-dissected {
        color: #94a3b8;
        font-weight: normal;
      }
    }
  }
}

/* 展开子任务触发栏 */
.subtask-toggle-bar {
  text-align: center;
  padding: 10rpx 0;
  border-top: 1rpx dashed #e2e8f0;
  border-bottom: 1rpx dashed #e2e8f0;

  .toggle-text {
    font-size: 22rpx;
    color: #4f46e5;
    font-weight: 500;
  }
}

/* 子任务容器 */
.subtasks-container {
  padding: 16rpx 0;
}

.subtask-empty {
  padding: 16rpx 0;
  text-align: center;
  .empty-tip {
    font-size: 22rpx;
    color: #94a3b8;
  }
}

.subtask-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.subtask-item {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border: 1rpx solid #e2e8f0;
  border-radius: 10rpx;
  padding: 12rpx 16rpx;

  &.subtask-done {
    opacity: 0.65;
    background-color: #f8fafc;
  }

  .subtask-checkbox {
    width: 32rpx;
    height: 32rpx;
    border: 2rpx solid #cbd5e1;
    border-radius: 50%;
    margin-right: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    &.checked {
      background-color: #10b981;
      border-color: #10b981;
    }

    .check-icon {
      color: #ffffff;
      font-size: 20rpx;
      font-weight: 700;
    }
  }

  .subtask-content {
    flex: 1;
    min-width: 0;

    .subtask-title {
      font-size: 24rpx;
      color: #1e293b;
      display: block;

      &.strikethrough {
        text-decoration: line-through;
        color: #94a3b8;
      }
    }

    .subtask-desc {
      font-size: 20rpx;
      color: #64748b;
      margin-top: 2rpx;
      display: block;
    }
  }

  .delete-task-btn {
    font-size: 24rpx;
    color: #94a3b8;
    padding: 6rpx 10rpx;
  }
}

.quick-add-task-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 8rpx;

  .quick-input {
    flex: 1;
    height: 64rpx;
    background-color: #ffffff;
    border: 1rpx solid #cbd5e1;
    border-radius: 8rpx;
    padding: 0 16rpx;
    font-size: 24rpx;
    color: #1e293b;
  }

  .quick-add-btn {
    font-size: 22rpx;
    background-color: #4f46e5;
    color: #ffffff;
    border-radius: 8rpx;
    padding: 8rpx 20rpx;
    line-height: 1.4;
    margin: 0;
  }
}

/* 底部操作条 */
.card-footer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 14rpx;
  border-top: 1rpx solid #e2e8f0;
}

.status-actions {
  display: flex;
  gap: 12rpx;

  .action-btn {
    font-size: 20rpx;
    border-radius: 6rpx;
    padding: 4rpx 14rpx;
    line-height: 1.5;
    margin: 0;

    &.complete-btn { background-color: #dcfce7; color: #15803d; }
    &.reopen-btn { background-color: #eef2ff; color: #4f46e5; }
    &.archive-btn { background-color: #f1f5f9; color: #475569; }
  }
}

.crud-actions {
  display: flex;
  gap: 16rpx;

  .icon-btn {
    font-size: 22rpx;
    color: #64748b;
    padding: 4rpx;

    &.delete-btn { color: #ef4444; }
  }
}

/* 模态弹窗 */
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
  }
}

.form-item {
  margin-bottom: 24rpx;

  .form-label {
    font-size: 26rpx;
    font-weight: 500;
    color: #334155;
    margin-bottom: 12rpx;
    display: block;

    .required { color: #ef4444; }
  }

  .form-input {
    width: 100%;
    height: 80rpx;
    background-color: #f8fafc;
    border: 1rpx solid #e2e8f0;
    border-radius: 12rpx;
    padding: 0 20rpx;
    font-size: 28rpx;
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
    box-sizing: border-box;
  }
}

.date-range-item {
  display: flex;
  gap: 20rpx;

  .date-picker-col {
    flex: 1;
  }
}

.picker-display {
  display: flex;
  align-items: center;
  height: 80rpx;
  background-color: #f8fafc;
  border: 1rpx solid #e2e8f0;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #475569;
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
    &.p-1 { border-color: #ef4444; color: #dc2626; background-color: #fee2e2; }
    &.p-2 { border-color: #4f46e5; color: #4f46e5; background-color: #eef2ff; }
    &.p-3 { border-color: #94a3b8; color: #475569; background-color: #f1f5f9; }
  }
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
