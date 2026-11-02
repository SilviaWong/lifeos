<template>
  <view class="goals-page">
    <!-- 详情抽屉视图 (对齐 04_user_goals.webp 右图) -->
    <view v-if="detailGoal" class="goal-detail-view">
      <view class="detail-top-nav" @tap="closeDetail">
        <text class="back-arrow">‹</text>
        <text class="nav-title">目标详情</text>
      </view>

      <view class="detail-card">
        <!-- 标题与状态 -->
        <view class="detail-header">
          <text class="detail-goal-title">{{ detailGoal.title }}</text>
          <text class="status-badge" :class="'status-' + detailGoal.status.toLowerCase()">
            {{ detailGoal.statusDesc }}
          </text>
        </view>
        <text v-if="detailGoal.description" class="detail-goal-desc">{{ detailGoal.description }}</text>

        <!-- 核心属性栏 (进度、状态、截止日期、创建时间) -->
        <view class="detail-meta-grid">
          <view class="meta-item">
            <text class="meta-label">进度</text>
            <text class="meta-val highlight-val">{{ detailGoal.progress }}%</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">状态</text>
            <text class="meta-val">{{ detailGoal.statusDesc }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">截止日期</text>
            <text class="meta-val">{{ detailGoal.dueDate || '长期' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">创建时间</text>
            <text class="meta-val">{{ detailGoal.createdAt || '近期' }}</text>
          </view>
        </view>

        <!-- 进度条 -->
        <view class="detail-progress-bar-bg">
          <view class="detail-progress-bar-fill" :style="{ width: detailGoal.progress + '%' }" />
        </view>

        <!-- 子任务拆解清单 -->
        <view class="subtasks-section">
          <view class="subtask-header-row">
            <text class="subtask-section-title">子任务</text>
            <button class="add-subtask-btn" @tap="showAddSubtask = true">+ 添加任务</button>
          </view>

          <!-- 快速输入行 -->
          <view v-if="showAddSubtask" class="quick-add-subtask-row">
            <input
              v-model="newSubtaskTitle"
              class="quick-subtask-input"
              placeholder="输入子任务标题回车添加..."
              confirm-type="done"
              @confirm="submitNewSubtask"
            />
            <button class="quick-subtask-btn" @tap="submitNewSubtask">添加</button>
            <button class="quick-subtask-cancel" @tap="showAddSubtask = false">取消</button>
          </view>

          <!-- 子任务清单 -->
          <view v-if="loadingSubtasks" class="subtask-state">
            <text class="state-text">正在加载子任务...</text>
          </view>
          <view v-else-if="currentSubtasks.length === 0" class="subtask-empty">
            <text class="state-text">暂无子任务，点击上方添加任务开始拆解</text>
          </view>
          <view v-else class="subtask-items-list">
            <view
              v-for="task in currentSubtasks"
              :key="task.id"
              class="subtask-card"
              :class="{ 'subtask-done': task.status === 'DONE' }"
            >
              <!-- 勾选圈圈 -->
              <view
                class="task-check-circle"
                :class="{ checked: task.status === 'DONE' }"
                @tap="toggleSubtask(task)"
              >
                <text v-if="task.status === 'DONE'" class="check-mark">✓</text>
              </view>

              <!-- 任务标题 -->
              <text class="task-title-text" :class="{ strikethrough: task.status === 'DONE' }">
                {{ task.title }}
              </text>

              <!-- 状态胶囊 -->
              <text class="task-status-pill" :class="'pill-' + task.status.toLowerCase()">
                {{ formatTaskStatus(task.status) }}
              </text>

              <!-- 删除按钮 -->
              <text class="task-delete-icon" @tap="confirmDeleteTask(task)">✕</text>
            </view>
          </view>
        </view>

        <!-- 底部操作按钮 -->
        <view class="detail-actions-row">
          <button class="detail-edit-btn" @tap="openEditGoalModal(detailGoal)">编辑目标</button>
          <button class="detail-del-btn" @tap="confirmDeleteGoal(detailGoal)">删除</button>
        </view>
      </view>
    </view>

    <!-- 目标列表主视图 (对齐 04_user_goals.webp 左图) -->
    <view v-else class="goals-main-list">
      <!-- 顶部 Header -->
      <view class="goals-hero">
        <view class="hero-left">
          <text class="hero-title">我的目标</text>
          <text class="hero-sub">专注重要的事</text>
        </view>
        <button class="hero-add-btn" @tap="openCreateGoalModal">+ 新目标</button>
      </view>

      <!-- 状态切换 Tabs (全部 / 进行中 / 已完成 / 已放弃) -->
      <view class="filter-tabs-bar">
        <view
          v-for="tab in filterTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: queryParams.filterType === tab.value }"
          @tap="switchFilter(tab.value)"
        >
          <text class="tab-label">{{ tab.label }}</text>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loading" class="state-container">
        <text class="loading-icon">⏳</text>
        <text class="state-text">正在加载目标数据...</text>
      </view>

      <!-- 空数据 -->
      <view v-else-if="goalList.length === 0" class="state-container">
        <text class="empty-icon">🎯</text>
        <text class="state-text">暂无相关目标，设定目标专注自律！</text>
        <button class="empty-create-btn" @tap="openCreateGoalModal">立即制定目标</button>
      </view>

      <!-- 目标卡片列表 -->
      <view v-else class="goal-cards-container">
        <view
          v-for="goal in goalList"
          :key="goal.id"
          class="goal-item-card"
          @tap="openDetail(goal)"
        >
          <!-- 状态胶囊 (右上角或顶部) -->
          <view class="card-status-line">
            <text class="status-pill" :class="'pill-' + goal.status.toLowerCase()">
              {{ goal.statusDesc }}
            </text>
          </view>

          <!-- 目标标题与描述 -->
          <text class="goal-item-title">{{ goal.title }}</text>
          <text v-if="goal.description" class="goal-item-desc">{{ goal.description }}</text>

          <!-- 进度条与百分比 -->
          <view class="progress-wrap">
            <view class="progress-info-line">
              <text class="progress-label">进度</text>
              <text class="progress-val">{{ goal.progress }}%</text>
            </view>
            <view class="progress-bar-bg">
              <view
                class="progress-bar-fill"
                :style="{ width: goal.progress + '%' }"
              />
            </view>
          </view>

          <!-- 截止时间与子任务统计 -->
          <view class="card-footer-line">
            <text class="due-text">截止：{{ goal.dueDate || '长期目标' }}</text>
            <text class="subtasks-count-text">
              {{ goal.completedTaskCount }}/{{ goal.totalTaskCount }} 子任务
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 悬浮创建目标按钮 (+) -->
    <view v-if="!detailGoal" class="fab-btn" @tap="openCreateGoalModal">
      <text class="fab-icon">+</text>
    </view>

    <!-- 新建 / 编辑目标弹窗 Modal -->
    <view v-if="modalVisible" class="modal-mask" @tap.self="closeModal">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑目标' : '制定新生活目标' }}</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">目标标题 <text class="required">*</text></text>
            <input
              v-model="formData.title"
              class="form-input"
              placeholder="例如: 通过PMP项目管理认证、体重减到70kg"
              maxlength="100"
            />
          </view>

          <view class="form-item">
            <text class="form-label">目标描述</text>
            <textarea
              v-model="formData.description"
              class="form-textarea"
              placeholder="简述目标背景、执行策略与期望收获..."
              maxlength="500"
            />
          </view>

          <view class="form-item">
            <text class="form-label">优先级</text>
            <view class="priority-selector">
              <view
                v-for="p in priorityOptions"
                :key="p.value"
                class="priority-chip"
                :class="{ active: formData.priority === p.value }"
                @tap="formData.priority = p.value"
              >
                <text>{{ p.label }}</text>
              </view>
            </view>
          </view>

          <view class="form-row">
            <view class="form-item flex-1">
              <text class="form-label">开始日期</text>
              <picker mode="date" :value="formData.startDate" @change="onStartDateChange">
                <view class="picker-box">
                  <text>{{ formData.startDate || '未设置' }}</text>
                </view>
              </picker>
            </view>
            <view class="form-item flex-1">
              <text class="form-label">截止日期</text>
              <picker mode="date" :value="formData.dueDate" @change="onDueDateChange">
                <view class="picker-box">
                  <text>{{ formData.dueDate || '未设置' }}</text>
                </view>
              </picker>
            </view>
          </view>

          <!-- 状态选择 (编辑模式下) -->
          <view v-if="isEditing" class="form-item">
            <text class="form-label">目标状态</text>
            <view class="priority-selector">
              <view
                v-for="s in statusOptions"
                :key="s.value"
                class="priority-chip"
                :class="{ active: formData.status === s.value }"
                @tap="formData.status = s.value"
              >
                <text>{{ s.label }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-cancel" @tap="closeModal">取消</button>
          <button class="btn btn-submit" :disabled="submitting" @tap="submitGoal">
            {{ submitting ? '保存中...' : '确认保存' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  getGoalListApi,
  getGoalDetailApi,
  createGoalApi,
  updateGoalApi,
  deleteGoalApi,
  getGoalTasksApi,
  createGoalTaskApi,
  updateGoalTaskStatusApi,
  deleteGoalTaskApi
} from '../../api/goal'
import type {
  GoalItem,
  GoalTaskItem,
  GoalQueryParams,
  CreateGoalDto
} from '../../types/goal'

// 筛选标签 (对齐 04_user_goals.webp)
const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已放弃', value: 'ARCHIVED' }
]

const priorityOptions = [
  { label: '普通', value: 'MEDIUM' },
  { label: '重要', value: 'HIGH' },
  { label: '紧迫', value: 'URGENT' },
  { label: '暂缓', value: 'LOW' }
]

const statusOptions = [
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已放弃', value: 'ARCHIVED' }
]

const loading = ref(false)
const goalList = ref<GoalItem[]>([])
const queryParams = reactive<GoalQueryParams>({
  filterType: 'ALL',
  page: 1,
  size: 30
})

// 目标详情状态
const detailGoal = ref<GoalItem | null>(null)
const currentSubtasks = ref<GoalTaskItem[]>([])
const loadingSubtasks = ref(false)
const showAddSubtask = ref(false)
const newSubtaskTitle = ref('')

// 新建/编辑弹窗
const modalVisible = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const currentEditId = ref<number | null>(null)

const formData = reactive<CreateGoalDto & { status?: string }>({
  title: '',
  description: '',
  priority: 'MEDIUM',
  startDate: '',
  dueDate: '',
  status: 'IN_PROGRESS'
})

const loadGoals = async () => {
  loading.value = true
  try {
    const res = await getGoalListApi(queryParams)
    goalList.value = res.data?.records || []
  } catch (e) {
    console.error('加载目标列表失败', e)
  } finally {
    loading.value = false
  }
}

const switchFilter = (type: string) => {
  queryParams.filterType = type
  queryParams.page = 1
  loadGoals()
}

// 目标详情
const openDetail = async (goal: GoalItem) => {
  detailGoal.value = goal
  loadingSubtasks.value = true
  try {
    const res = await getGoalTasksApi(goal.id)
    currentSubtasks.value = res.data || []
  } catch (e) {
    console.error('获取子任务失败', e)
  } finally {
    loadingSubtasks.value = false
  }
}

const closeDetail = () => {
  detailGoal.value = null
  loadGoals()
}

// 子任务
const submitNewSubtask = async () => {
  const title = newSubtaskTitle.value.trim()
  if (!title) {
    uni.showToast({ title: '请输入子任务标题', icon: 'none' })
    return
  }
  if (!detailGoal.value) return

  try {
    await createGoalTaskApi(detailGoal.value.id, { title })
    newSubtaskTitle.value = ''
    showAddSubtask.value = false
    // 重新加载子任务
    const res = await getGoalTasksApi(detailGoal.value.id)
    currentSubtasks.value = res.data || []
    // 重新加载目标详情同步最新进度
    const dRes = await getGoalDetailApi(detailGoal.value.id)
    if (dRes.data) {
      detailGoal.value = dRes.data
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '添加子任务失败', icon: 'none' })
  }
}

const toggleSubtask = async (task: GoalTaskItem) => {
  if (!detailGoal.value) return
  const targetStatus = task.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    await updateGoalTaskStatusApi(detailGoal.value.id, task.id, targetStatus)
    task.status = targetStatus
    // 刷新目标详情以更新进度条
    const dRes = await getGoalDetailApi(detailGoal.value.id)
    if (dRes.data) {
      detailGoal.value = dRes.data
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '更新任务状态失败', icon: 'none' })
  }
}

const confirmDeleteTask = (task: GoalTaskItem) => {
  if (!detailGoal.value) return
  uni.showModal({
    title: '提示',
    content: `确定要删除子任务「${task.title}」吗？`,
    confirmText: '删除',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm && detailGoal.value) {
        try {
          await deleteGoalTaskApi(detailGoal.value.id, task.id)
          const tRes = await getGoalTasksApi(detailGoal.value.id)
          currentSubtasks.value = tRes.data || []
          const dRes = await getGoalDetailApi(detailGoal.value.id)
          if (dRes.data) {
            detailGoal.value = dRes.data
          }
        } catch (e: any) {
          uni.showToast({ title: e.message || '删除子任务失败', icon: 'none' })
        }
      }
    }
  })
}

const formatTaskStatus = (status: string) => {
  switch (status) {
    case 'DONE':
      return '已完成'
    case 'IN_PROGRESS':
      return '进行中'
    default:
      return '待开始'
  }
}

// 目标增删改
const openCreateGoalModal = () => {
  isEditing.value = false
  currentEditId.value = null
  Object.assign(formData, {
    title: '',
    description: '',
    priority: 'MEDIUM',
    startDate: '',
    dueDate: '',
    status: 'IN_PROGRESS'
  })
  modalVisible.value = true
}

const openEditGoalModal = (goal: GoalItem) => {
  isEditing.value = true
  currentEditId.value = goal.id
  Object.assign(formData, {
    title: goal.title,
    description: goal.description || '',
    priority: goal.priority || 'MEDIUM',
    startDate: goal.startDate || '',
    dueDate: goal.dueDate || '',
    status: goal.status || 'IN_PROGRESS'
  })
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

const submitGoal = async () => {
  if (!formData.title.trim()) {
    uni.showToast({ title: '请输入目标标题', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    if (isEditing.value && currentEditId.value) {
      await updateGoalApi(currentEditId.value, formData)
      uni.showToast({ title: '目标更新成功', icon: 'success' })
      if (detailGoal.value && detailGoal.value.id === currentEditId.value) {
        const dRes = await getGoalDetailApi(currentEditId.value)
        if (dRes.data) detailGoal.value = dRes.data
      }
    } else {
      await createGoalApi(formData)
      uni.showToast({ title: '目标制定成功', icon: 'success' })
    }
    closeModal()
    loadGoals()
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const confirmDeleteGoal = (goal: GoalItem) => {
  uni.showModal({
    title: '提示',
    content: `确定要删除目标「${goal.title}」及其所有子任务吗？`,
    confirmText: '删除',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteGoalApi(goal.id)
          uni.showToast({ title: '目标已删除', icon: 'success' })
          if (detailGoal.value && detailGoal.value.id === goal.id) {
            closeDetail()
          } else {
            loadGoals()
          }
        } catch (e: any) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadGoals()
})

onShow(() => {
  loadGoals()
})

onPullDownRefresh(async () => {
  await loadGoals()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.goals-page {
  min-height: 100vh;
  background-color: #F8FAFC;
  padding: 24rpx;
  padding-bottom: 120rpx;
  box-sizing: border-box;
}

/* 顶部 Hero */
.goals-hero {
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

/* 状态筛选标签栏 */
.filter-tabs-bar {
  display: flex;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: 14rpx;
  transition: all 0.2s ease;
}

.tab-item.active {
  background: #00C28A;
}

.tab-label {
  font-size: 26rpx;
  color: #64748B;
  font-weight: 500;
}

.tab-item.active .tab-label {
  color: #FFFFFF;
  font-weight: 700;
}

/* 目标卡片列表 */
.goal-cards-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.goal-item-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-status-line {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 12rpx;
}

.status-pill {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  font-weight: 600;
}

.pill-in_progress, .pill-active {
  background: #E6FBF5;
  color: #00C28A;
}

.pill-completed {
  background: #DCFCE7;
  color: #166534;
}

.pill-archived {
  background: #F1F5F9;
  color: #94A3B8;
}

.goal-item-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0F172A;
  margin-bottom: 8rpx;
  display: block;
}

.goal-item-desc {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.5;
  margin-bottom: 18rpx;
  display: block;
}

/* 进度条 */
.progress-wrap {
  margin-bottom: 16rpx;
}

.progress-info-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.progress-label {
  font-size: 22rpx;
  color: #94A3B8;
}

.progress-val {
  font-size: 26rpx;
  font-weight: 700;
  color: #00C28A;
}

.progress-bar-bg {
  width: 100%;
  height: 12rpx;
  background: #F1F5F9;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: #00C28A;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.card-footer-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12rpx;
  border-top: 1rpx solid #F1F5F9;
}

.due-text {
  font-size: 22rpx;
  color: #94A3B8;
}

.subtasks-count-text {
  font-size: 22rpx;
  color: #64748B;
}

/* 详情抽屉视图 (04_user_goals.webp 右图) */
.detail-top-nav {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 8rpx 0;
}

.back-arrow {
  font-size: 44rpx;
  color: #00C28A;
  font-weight: 700;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0F172A;
}

.detail-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.detail-goal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #0F172A;
}

.status-badge {
  font-size: 22rpx;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  font-weight: 600;
}

.detail-goal-desc {
  font-size: 26rpx;
  color: #64748B;
  line-height: 1.6;
  margin-bottom: 24rpx;
  display: block;
}

.detail-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.meta-label {
  font-size: 22rpx;
  color: #94A3B8;
}

.meta-val {
  font-size: 26rpx;
  font-weight: 600;
  color: #0F172A;
}

.highlight-val {
  color: #00C28A;
  font-size: 32rpx;
  font-weight: 700;
}

.detail-progress-bar-bg {
  width: 100%;
  height: 14rpx;
  background: #E2E8F0;
  border-radius: 7rpx;
  overflow: hidden;
  margin-bottom: 32rpx;
}

.detail-progress-bar-fill {
  height: 100%;
  background: #00C28A;
  border-radius: 7rpx;
}

/* 子任务模块 */
.subtasks-section {
  border-top: 1rpx solid #F1F5F9;
  padding-top: 24rpx;
  margin-bottom: 32rpx;
}

.subtask-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.subtask-section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #0F172A;
}

.add-subtask-btn {
  background: #F1F5F9;
  color: #00C28A;
  font-size: 24rpx;
  font-weight: 600;
  padding: 0 20rpx;
  height: 52rpx;
  line-height: 52rpx;
  border-radius: 26rpx;
  border: none;
}

.quick-add-subtask-row {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.quick-subtask-input {
  flex: 1;
  background: #F8FAFC;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  height: 64rpx;
  padding: 0 16rpx;
  font-size: 24rpx;
}

.quick-subtask-btn {
  background: #00C28A;
  color: #FFFFFF;
  font-size: 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 12rpx;
  padding: 0 20rpx;
}

.quick-subtask-cancel {
  background: #F1F5F9;
  color: #64748B;
  font-size: 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 12rpx;
  padding: 0 16rpx;
}

.subtask-items-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.subtask-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  border: 1rpx solid #E2E8F0;
}

.task-check-circle {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
}

.task-check-circle.checked {
  background: #00C28A;
  border-color: #00C28A;
}

.check-mark {
  color: #FFFFFF;
  font-size: 24rpx;
  font-weight: 700;
}

.task-title-text {
  flex: 1;
  font-size: 26rpx;
  color: #0F172A;
}

.task-title-text.strikethrough {
  text-decoration: line-through;
  color: #94A3B8;
}

.task-status-pill {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

.pill-todo {
  background: #F1F5F9;
  color: #64748B;
}

.task-delete-icon {
  font-size: 26rpx;
  color: #CBD5E1;
  padding: 4rpx;
}

/* 底部操作 */
.detail-actions-row {
  display: flex;
  gap: 16rpx;
}

.detail-edit-btn {
  flex: 1;
  background: #F1F5F9;
  color: #334155;
  font-size: 28rpx;
  font-weight: 600;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 16rpx;
}

.detail-del-btn {
  background: #FEE2E2;
  color: #EF4444;
  font-size: 28rpx;
  font-weight: 600;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 16rpx;
  padding: 0 40rpx;
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

.form-input {
  background: #F8FAFC;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  height: 72rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
}

.form-textarea {
  background: #F8FAFC;
  border: 1rpx solid #CBD5E1;
  border-radius: 12rpx;
  height: 140rpx;
  padding: 16rpx 20rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}

.priority-selector {
  display: flex;
  gap: 12rpx;
}

.priority-chip {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: #64748B;
  background: #F1F5F9;
  padding: 12rpx 0;
  border-radius: 14rpx;
}

.priority-chip.active {
  background: #00C28A;
  color: #FFFFFF;
  font-weight: 600;
}

.form-row {
  display: flex;
  gap: 16rpx;
}

.flex-1 {
  flex: 1;
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

.empty-create-btn {
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
