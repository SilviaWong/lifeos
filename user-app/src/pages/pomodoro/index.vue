<template>
  <view class="pomodoro-page">
    <!-- 顶部状态栏与设置入口 -->
    <view class="header-action-row">
      <view class="session-badge" :class="'badge-' + (currentSession?.status.toLowerCase() || 'idle')">
        <text class="dot"></text>
        <text>{{ formatStatusText(currentSession?.status) }}</text>
      </view>
      <view class="setting-btn" @tap="openSettingModal">
        <text class="setting-icon">⚙️</text>
        <text class="setting-text">偏好设置</text>
      </view>
    </view>

    <!-- 模式选择 Tabs (未开始时可自由切换，进行中锁定当前模式) -->
    <view class="mode-tabs">
      <view
        v-for="m in modeOptions"
        :key="m.type"
        class="mode-tab"
        :class="{ active: currentMode === m.type, disabled: isTimerActive && currentMode !== m.type }"
        @tap="switchMode(m.type)"
      >
        <text class="mode-icon">{{ m.icon }}</text>
        <text class="mode-label">{{ m.label }} ({{ getModeDuration(m.type) }}m)</text>
      </view>
    </view>

    <!-- 关联任务卡片（可绑定进行中待办或目标） -->
    <view class="task-link-card" @tap="openTaskPicker">
      <view class="task-info">
        <text class="task-icon">{{ selectedTodo ? '📝' : (selectedGoal ? '🎯' : '📌') }}</text>
        <view class="task-text-wrap">
          <text class="task-title">
            {{ selectedTodo ? selectedTodo.title : (selectedGoal ? selectedGoal.title : '点击关联待办任务或生活目标') }}
          </text>
          <text v-if="selectedTodo" class="task-sub">关联待办 · 专注完成后可同步勾选</text>
          <text v-else-if="selectedGoal" class="task-sub">关联目标 · 计入目标专注投入</text>
          <text v-else class="task-sub">专注更加有的放矢，事半功倍</text>
        </view>
      </view>
      <text v-if="!isTimerActive && (selectedTodo || selectedGoal)" class="clear-task-btn" @tap.stop="clearSelectedTask">✕</text>
      <text v-else-if="!isTimerActive" class="arrow-icon">›</text>
    </view>

    <!-- 核心计时表盘卡片 -->
    <view class="life-card timer-card">
      <view class="timer-dial" :class="{ 'is-running': currentSession?.status === 'RUNNING', 'is-paused': currentSession?.status === 'PAUSED' }">
        <view class="time-display">
          <text class="time-text">{{ formattedTime }}</text>
          <text class="time-label">{{ modeLabel }}</text>
        </view>
      </view>

      <!-- 核心控制按钮区 -->
      <view class="controls-row">
        <!-- 未激活状态：开始专注 -->
        <template v-if="!currentSession || currentSession.status === 'COMPLETED' || currentSession.status === 'ABORTED'">
          <button class="life-btn-primary start-btn" @tap="startTimer">
            ▶ 开始{{ modeLabel }}
          </button>
        </template>

        <!-- 进行中状态：暂停 / 提前完成 / 中止 -->
        <template v-else-if="currentSession.status === 'RUNNING'">
          <button class="action-btn pause-btn" @tap="pauseTimer">⏸ 暂停</button>
          <button class="action-btn finish-btn" @tap="finishTimer">✓ 结算完成</button>
          <button class="action-btn abort-btn" @tap="confirmAbortTimer">✕ 放弃</button>
        </template>

        <!-- 已暂停状态：继续 / 提前完成 / 中止 -->
        <template v-else-if="currentSession.status === 'PAUSED'">
          <button class="action-btn resume-btn" @tap="resumeTimer">▶ 继续</button>
          <button class="action-btn finish-btn" @tap="finishTimer">✓ 结算完成</button>
          <button class="action-btn abort-btn" @tap="confirmAbortTimer">✕ 放弃</button>
        </template>
      </view>
    </view>

    <!-- 今日与本周统计卡片 -->
    <view class="life-card summary-card">
      <view class="life-card-title">专注成效统计</view>
      <view class="summary-grid">
        <view class="sum-item">
          <text class="sum-num text-primary">{{ summary.todayFocusMinutes }}</text>
          <text class="sum-unit">分钟</text>
          <text class="sum-label">今日专注</text>
        </view>
        <view class="sum-item">
          <text class="sum-num text-success">{{ summary.todayCompletedCount }}</text>
          <text class="sum-unit">次</text>
          <text class="sum-label">今日完成番茄</text>
        </view>
        <view class="sum-item">
          <text class="sum-num text-warning">{{ summary.weekFocusMinutes }}</text>
          <text class="sum-unit">分钟</text>
          <text class="sum-label">本周专注</text>
        </view>
        <view class="sum-item">
          <text class="sum-num text-info">{{ summary.totalCompletedCount }}</text>
          <text class="sum-unit">次</text>
          <text class="sum-label">历史累计番茄</text>
        </view>
      </view>
    </view>

    <!-- 最近专注历史记录明细卡片 -->
    <view class="life-card history-card">
      <view class="life-card-title">最近专注明细</view>

      <view v-if="historyLoading" class="state-container">
        <text class="state-icon">⏳</text>
        <text class="state-text">正在拉取记录...</text>
      </view>

      <view v-else-if="records.length === 0" class="state-container">
        <text class="state-icon">⏱️</text>
        <text class="state-text">暂无专注记录，立即开启今日第一轮自律！</text>
      </view>

      <view v-else class="record-list">
        <view v-for="rec in records" :key="rec.id" class="record-item">
          <view class="rec-icon-wrap" :class="'icon-' + rec.sessionType.toLowerCase()">
            <text>{{ rec.sessionType === 'FOCUS' ? '🍅' : '☕' }}</text>
          </view>
          <view class="rec-main">
            <view class="rec-title-row">
              <text class="rec-type">{{ rec.sessionTypeDesc }}</text>
              <text class="rec-time">{{ formatRecordTime(rec.startedAt) }}</text>
            </view>
            <view v-if="rec.todoTitle || rec.goalTitle" class="rec-task-row">
              <text class="rec-tag">{{ rec.todoTitle ? '待办' : '目标' }}</text>
              <text class="rec-task-name">{{ rec.todoTitle || rec.goalTitle }}</text>
            </view>
          </view>
          <view class="rec-right">
            <text class="rec-duration">{{ Math.round(rec.actualSeconds / 60) }} 分钟</text>
            <text class="status-badge" :class="'badge-' + rec.status.toLowerCase()">
              {{ rec.statusDesc }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 偏好设置 Modal -->
    <view v-if="settingModalVisible" class="modal-mask" @tap.self="closeSettingModal">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">番茄钟偏好设置</text>
          <text class="modal-close" @tap="closeSettingModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-row">
            <text class="form-label">专注时长 (分钟)</text>
            <input
              v-model.number="settingForm.focusDuration"
              type="number"
              class="form-num-input"
              maxlength="3"
            />
          </view>
          <view class="form-row">
            <text class="form-label">短休息时长 (分钟)</text>
            <input
              v-model.number="settingForm.shortBreakDuration"
              type="number"
              class="form-num-input"
              maxlength="2"
            />
          </view>
          <view class="form-row">
            <text class="form-label">长休息时长 (分钟)</text>
            <input
              v-model.number="settingForm.longBreakDuration"
              type="number"
              class="form-num-input"
              maxlength="3"
            />
          </view>
          <view class="form-row">
            <text class="form-label">长休息间隔 (专注周期数)</text>
            <input
              v-model.number="settingForm.longBreakInterval"
              type="number"
              class="form-num-input"
              maxlength="2"
            />
          </view>
          <view class="form-row switch-row">
            <text class="form-label">自动开始休息计时</text>
            <switch
              :checked="settingForm.autoStartBreaks"
              color="#1677ff"
              @change="(e: any) => settingForm.autoStartBreaks = e.detail.value"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-cancel" @tap="closeSettingModal">取消</button>
          <button class="btn btn-save" :disabled="savingSetting" @tap="saveSetting">
            {{ savingSetting ? '保存中...' : '保存配置' }}
          </button>
        </view>
      </view>
    </view>

    <!-- 关联任务选择 Modal -->
    <view v-if="taskPickerVisible" class="modal-mask" @tap.self="closeTaskPicker">
      <view class="modal-panel task-picker-panel">
        <view class="modal-header">
          <text class="modal-title">选择专注任务</text>
          <text class="modal-close" @tap="closeTaskPicker">✕</text>
        </view>

        <view class="picker-tabs">
          <view
            class="picker-tab"
            :class="{ active: taskPickerTab === 'TODOS' }"
            @tap="taskPickerTab = 'TODOS'"
          >
            待办事项 ({{ candidateTodos.length }})
          </view>
          <view
            class="picker-tab"
            :class="{ active: taskPickerTab === 'GOALS' }"
            @tap="taskPickerTab = 'GOALS'"
          >
            生活目标 ({{ candidateGoals.length }})
          </view>
        </view>

        <scroll-view class="task-scroll-list" scroll-y>
          <template v-if="taskPickerTab === 'TODOS'">
            <view v-if="candidateTodos.length === 0" class="empty-picker">
              <text>暂无待处理待办，可先在日程中创建</text>
            </view>
            <view
              v-for="t in candidateTodos"
              :key="t.id"
              class="picker-item"
              :class="{ selected: selectedTodo?.id === t.id }"
              @tap="selectTodo(t)"
            >
              <text class="item-title">{{ t.title }}</text>
              <text class="item-sub">{{ t.priorityDesc }}优先级</text>
            </view>
          </template>

          <template v-else>
            <view v-if="candidateGoals.length === 0" class="empty-picker">
              <text>暂无进行中目标，可先在目标中制定</text>
            </view>
            <view
              v-for="g in candidateGoals"
              :key="g.id"
              class="picker-item"
              :class="{ selected: selectedGoal?.id === g.id }"
              @tap="selectGoal(g)"
            >
              <text class="item-title">{{ g.title }}</text>
              <text class="item-sub">当前达成进度 {{ g.progress }}%</text>
            </view>
          </template>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-cancel" @tap="closeTaskPicker">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  startPomodoroApi,
  pausePomodoroApi,
  resumePomodoroApi,
  finishPomodoroApi,
  abortPomodoroApi,
  getCurrentPomodoroApi,
  getPomodoroSummaryApi,
  getPomodoroRecordsApi,
  getPomodoroSettingApi,
  updatePomodoroSettingApi
} from '../../api/pomodoro'
import { getTodoListApi } from '../../api/todo'
import { getGoalListApi } from '../../api/goal'
import type {
  PomodoroRecordItem,
  PomodoroSessionType,
  PomodoroSetting,
  PomodoroSummary
} from '../../types/pomodoro'
import type { TodoItem } from '../../types/todo'
import type { GoalItem } from '../../types/goal'

// --- 会话与计时核心状态 ---
const currentMode = ref<PomodoroSessionType>('FOCUS')
const currentSession = ref<PomodoroRecordItem | null>(null)
const remainingSeconds = ref<number>(25 * 60)
let timerInterval: any = null

const modeOptions: { type: PomodoroSessionType; label: string; icon: string }[] = [
  { type: 'FOCUS', label: '深度专注', icon: '🍅' },
  { type: 'SHORT_BREAK', label: '短休息', icon: '☕' },
  { type: 'LONG_BREAK', label: '长休息', icon: '🏖️' }
]

const modeLabel = computed(() => {
  const m = modeOptions.find(o => o.type === currentMode.value)
  return m ? m.label : '专注'
})

const isTimerActive = computed(() => {
  return currentSession.value && (currentSession.value.status === 'RUNNING' || currentSession.value.status === 'PAUSED')
})

const formattedTime = computed(() => {
  const sec = Math.max(0, remainingSeconds.value)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  const pad = (n: number) => (n < 10 ? '0' + n : String(n))
  return `${pad(m)}:${pad(s)}`
})

// --- 统计与明细记录 ---
const summary = ref<PomodoroSummary>({
  todayFocusSeconds: 0,
  todayFocusMinutes: 0,
  todayCompletedCount: 0,
  weekFocusSeconds: 0,
  weekFocusMinutes: 0,
  weekCompletedCount: 0,
  totalCompletedCount: 0,
  totalFocusMinutes: 0
})

const records = ref<PomodoroRecordItem[]>([])
const historyLoading = ref<boolean>(false)

// --- 偏好配置 ---
const setting = ref<PomodoroSetting>({
  focusDuration: 25,
  shortBreakDuration: 5,
  longBreakDuration: 15,
  longBreakInterval: 4,
  autoStartBreaks: false,
  autoStartPomodoros: false
})
const settingModalVisible = ref<boolean>(false)
const savingSetting = ref<boolean>(false)
const settingForm = ref<PomodoroSetting>({ ...setting.value })

// --- 任务关联选择器 ---
const taskPickerVisible = ref<boolean>(false)
const taskPickerTab = ref<'TODOS' | 'GOALS'>('TODOS')
const candidateTodos = ref<TodoItem[]>([])
const candidateGoals = ref<GoalItem[]>([])
const selectedTodo = ref<TodoItem | null>(null)
const selectedGoal = ref<GoalItem | null>(null)

// 获取当前模式默认时长 (分钟)
const getModeDuration = (type: PomodoroSessionType): number => {
  if (type === 'SHORT_BREAK') return setting.value.shortBreakDuration || 5
  if (type === 'LONG_BREAK') return setting.value.longBreakDuration || 15
  return setting.value.focusDuration || 25
}

// 切换模式
const switchMode = (type: PomodoroSessionType) => {
  if (isTimerActive.value) {
    uni.showToast({ title: '当前会话进行中，不可切换模式', icon: 'none' })
    return
  }
  currentMode.value = type
  remainingSeconds.value = getModeDuration(type) * 60
}

// --- 倒计时时钟驱动 ---
const startLocalTimer = () => {
  stopLocalTimer()
  timerInterval = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      stopLocalTimer()
      // 倒计时自然到期自动结算
      finishTimer()
    }
  }, 1000)
}

const stopLocalTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// --- API 交互：拉取当前活动会话与恢复 ---
const syncCurrentSession = async () => {
  try {
    const res = await getCurrentPomodoroApi()
    if (res.code === 0 && res.data) {
      currentSession.value = res.data
      currentMode.value = res.data.sessionType

      // 根据服务端 startedAt 计算剩余秒数
      const started = new Date(res.data.startedAt.replace(' ', 'T')).getTime()
      const now = Date.now()
      const elapsedSeconds = Math.max(0, Math.floor((now - started) / 1000))
      remainingSeconds.value = Math.max(0, res.data.plannedSeconds - elapsedSeconds)

      if (res.data.status === 'RUNNING') {
        startLocalTimer()
      } else {
        stopLocalTimer()
      }
    } else {
      currentSession.value = null
      stopLocalTimer()
      remainingSeconds.value = getModeDuration(currentMode.value) * 60
    }
  } catch (e) {
    console.error('同步当前会话失败', e)
  }
}

// 开始计时
const startTimer = async () => {
  const plannedSeconds = getModeDuration(currentMode.value) * 60
  try {
    const res = await startPomodoroApi({
      sessionType: currentMode.value,
      plannedSeconds,
      goalId: selectedGoal.value ? selectedGoal.value.id : undefined,
      todoId: selectedTodo.value ? selectedTodo.value.id : undefined
    })

    if (res.code === 0 && res.data) {
      currentSession.value = res.data
      remainingSeconds.value = plannedSeconds
      startLocalTimer()
      uni.showToast({ title: `开启${modeLabel.value}`, icon: 'success' })
    } else {
      uni.showToast({ title: res.message || '开启失败', icon: 'none' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '开启异常', icon: 'none' })
  }
}

// 暂停计时
const pauseTimer = async () => {
  if (!currentSession.value) return
  try {
    const res = await pausePomodoroApi(currentSession.value.id)
    if (res.code === 0 && res.data) {
      currentSession.value = res.data
      stopLocalTimer()
      uni.showToast({ title: '已暂停', icon: 'none' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '暂停失败', icon: 'none' })
  }
}

// 恢复继续
const resumeTimer = async () => {
  if (!currentSession.value) return
  try {
    const res = await resumePomodoroApi(currentSession.value.id)
    if (res.code === 0 && res.data) {
      currentSession.value = res.data
      startLocalTimer()
      uni.showToast({ title: '继续专注', icon: 'success' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '继续失败', icon: 'none' })
  }
}

// 完成结算
const finishTimer = async () => {
  if (!currentSession.value) return
  stopLocalTimer()
  try {
    const planned = currentSession.value.plannedSeconds
    const clientActual = Math.max(0, planned - remainingSeconds.value)
    const res = await finishPomodoroApi(currentSession.value.id, { clientActualSeconds: clientActual })

    if (res.code === 0) {
      uni.showToast({ title: '🎉 恭喜完成本轮专注！', icon: 'success' })
      currentSession.value = null
      remainingSeconds.value = getModeDuration(currentMode.value) * 60
      refreshStatsAndRecords()
    } else {
      uni.showToast({ title: res.message || '结算失败', icon: 'none' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '结算异常', icon: 'none' })
  }
}

// 放弃/中止
const confirmAbortTimer = () => {
  if (!currentSession.value) return
  uni.showModal({
    title: '放弃本次专注？',
    content: '中途放弃将记录为中止状态，确定结束当前会话吗？',
    confirmText: '放弃',
    confirmColor: '#ff4d4f',
    success: async (mRes) => {
      if (mRes.confirm && currentSession.value) {
        stopLocalTimer()
        try {
          const planned = currentSession.value.plannedSeconds
          const clientActual = Math.max(0, planned - remainingSeconds.value)
          const res = await abortPomodoroApi(currentSession.value.id, {
            reason: 'USER_ABORT',
            clientActualSeconds: clientActual
          })
          if (res.code === 0) {
            uni.showToast({ title: '已放弃本次专注', icon: 'none' })
            currentSession.value = null
            remainingSeconds.value = getModeDuration(currentMode.value) * 60
            refreshStatsAndRecords()
          }
        } catch (e: any) {
          uni.showToast({ title: e.message || '中止失败', icon: 'none' })
        }
      }
    }
  })
}

// --- 统计与记录加载 ---
const loadSummary = async () => {
  try {
    const res = await getPomodoroSummaryApi()
    if (res.code === 0 && res.data) {
      summary.value = res.data
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadRecords = async () => {
  historyLoading.value = true
  try {
    const res = await getPomodoroRecordsApi({ pageNum: 1, pageSize: 10 })
    if (res.code === 0 && res.data) {
      records.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载历史失败', e)
  } finally {
    historyLoading.value = false
  }
}

const refreshStatsAndRecords = () => {
  loadSummary()
  loadRecords()
}

// --- 配置管理 ---
const loadSetting = async () => {
  try {
    const res = await getPomodoroSettingApi()
    if (res.code === 0 && res.data) {
      setting.value = res.data
      settingForm.value = { ...res.data }
      if (!isTimerActive.value) {
        remainingSeconds.value = getModeDuration(currentMode.value) * 60
      }
    }
  } catch (e) {
    console.error('加载配置失败', e)
  }
}

const openSettingModal = () => {
  settingForm.value = { ...setting.value }
  settingModalVisible.value = true
}

const closeSettingModal = () => {
  settingModalVisible.value = false
}

const saveSetting = async () => {
  savingSetting.value = true
  try {
    const res = await updatePomodoroSettingApi(settingForm.value)
    if (res.code === 0 && res.data) {
      setting.value = res.data
      uni.showToast({ title: '配置已更新', icon: 'success' })
      closeSettingModal()
      if (!isTimerActive.value) {
        remainingSeconds.value = getModeDuration(currentMode.value) * 60
      }
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存配置异常', icon: 'none' })
  } finally {
    savingSetting.value = false
  }
}

// --- 任务选择器逻辑 ---
const openTaskPicker = async () => {
  if (isTimerActive.value) return
  taskPickerVisible.value = true
  try {
    const [todoRes, goalRes] = await Promise.all([
      getTodoListApi({ filterType: 'PENDING', pageSize: 20 }),
      getGoalListApi({ filterType: 'ACTIVE', pageSize: 20 })
    ])
    if (todoRes.code === 0 && todoRes.data) {
      candidateTodos.value = todoRes.data.records || []
    }
    if (goalRes.code === 0 && goalRes.data) {
      candidateGoals.value = goalRes.data.records || []
    }
  } catch (e) {
    console.error('加载任务候选项异常', e)
  }
}

const closeTaskPicker = () => {
  taskPickerVisible.value = false
}

const selectTodo = (todo: TodoItem) => {
  selectedTodo.value = todo
  selectedGoal.value = null
  closeTaskPicker()
}

const selectGoal = (goal: GoalItem) => {
  selectedGoal.value = goal
  selectedTodo.value = null
  closeTaskPicker()
}

const clearSelectedTask = () => {
  selectedTodo.value = null
  selectedGoal.value = null
}

// --- 格式化辅助 ---
const formatStatusText = (status?: string): string => {
  if (!status) return '就绪待开始'
  switch (status) {
    case 'RUNNING': return '进行中'
    case 'PAUSED': return '已暂停'
    case 'COMPLETED': return '已完成'
    case 'ABORTED': return '已中止'
    default: return '就绪'
  }
}

const formatRecordTime = (timeStr: string): string => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(5, 16)
}

onMounted(async () => {
  await loadSetting()
  await syncCurrentSession()
  refreshStatsAndRecords()
})

onShow(() => {
  syncCurrentSession()
  loadSummary()
})

onUnmounted(() => {
  stopLocalTimer()
})

onPullDownRefresh(async () => {
  await Promise.all([loadSetting(), syncCurrentSession(), loadSummary(), loadRecords()])
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.pomodoro-page {
  padding-bottom: 40rpx;
}

/* 顶部状态与设置行 */
.header-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 28rpx 0;
}

.session-badge {
  display: flex;
  align-items: center;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 8rpx;
}

.badge-idle {
  background-color: #f0f2f5;
  color: #8c8c8c;
  .dot { background-color: #bfbfbf; }
}

.badge-running {
  background-color: #e6f7ff;
  color: #1677ff;
  .dot { background-color: #1677ff; }
}

.badge-paused {
  background-color: #fff7e6;
  color: #fa8c16;
  .dot { background-color: #fa8c16; }
}

.badge-completed {
  background-color: #f6ffed;
  color: #52c41a;
  .dot { background-color: #52c41a; }
}

.badge-aborted {
  background-color: #fff1f0;
  color: #ff4d4f;
  .dot { background-color: #ff4d4f; }
}

.setting-btn {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  padding: 8rpx 18rpx;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.setting-icon {
  font-size: 24rpx;
  margin-right: 6rpx;
}

.setting-text {
  font-size: 22rpx;
  color: #595959;
}

/* 模式 Tabs */
.mode-tabs {
  display: flex;
  margin: 20rpx 24rpx 12rpx;
  background-color: #f0f2f5;
  border-radius: 36rpx;
  padding: 6rpx;
}

.mode-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12rpx 0;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #595959;
  transition: all 0.2s ease;
}

.mode-tab.active {
  background-color: #ffffff;
  color: #1677ff;
  font-weight: 600;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.06);
}

.mode-tab.disabled {
  opacity: 0.5;
}

.mode-icon {
  margin-right: 8rpx;
  font-size: 26rpx;
}

/* 关联任务卡片 */
.task-link-card {
  margin: 16rpx 24rpx 0;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 18rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.task-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.task-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.task-text-wrap {
  display: flex;
  flex-direction: column;
}

.task-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.task-sub {
  font-size: 20rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
}

.clear-task-btn {
  font-size: 28rpx;
  color: #8c8c8c;
  padding: 8rpx 16rpx;
}

.arrow-icon {
  font-size: 32rpx;
  color: #bfbfbf;
}

/* 计时器卡片 */
.timer-card {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50rpx 30rpx;
}

.timer-dial {
  width: 380rpx;
  height: 380rpx;
  border-radius: 50%;
  background: radial-gradient(circle, #ffffff 65%, #f0f7ff 100%);
  border: 12rpx solid #e6f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 30rpx rgba(22, 119, 255, 0.08);
  transition: all 0.3s ease;
}

.timer-dial.is-running {
  border-color: #69b1ff;
  box-shadow: 0 10rpx 40rpx rgba(22, 119, 255, 0.2);
}

.timer-dial.is-paused {
  border-color: #ffd591;
  box-shadow: 0 10rpx 40rpx rgba(250, 140, 22, 0.15);
}

.time-display {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time-text {
  font-size: 72rpx;
  font-weight: 700;
  color: #1f2d3d;
  font-family: monospace, -apple-system;
}

.time-label {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 6rpx;
}

/* 控制按钮组 */
.controls-row {
  margin-top: 48rpx;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 20rpx;
}

.start-btn {
  width: 80%;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

.pause-btn {
  background-color: #faad14;
  color: #ffffff;
}

.resume-btn {
  background-color: #1677ff;
  color: #ffffff;
}

.finish-btn {
  background-color: #52c41a;
  color: #ffffff;
}

.abort-btn {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

/* 统计卡片 Grid */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.sum-item {
  background-color: #f7f8fa;
  border-radius: 12rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sum-num {
  font-size: 44rpx;
  font-weight: 700;
  line-height: 1.2;
}

.sum-unit {
  font-size: 20rpx;
  color: #8c8c8c;
  margin-bottom: 4rpx;
}

.sum-label {
  font-size: 22rpx;
  color: #595959;
}

.text-primary { color: #1677ff; }
.text-success { color: #52c41a; }
.text-warning { color: #fa8c16; }
.text-info { color: #722ed1; }

/* 历史列表 */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  align-items: center;
  background-color: #f7f8fa;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
}

.rec-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  margin-right: 16rpx;
  background-color: #e6f4ff;
}

.rec-main {
  flex: 1;
}

.rec-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rec-type {
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.rec-time {
  font-size: 22rpx;
  color: #8c8c8c;
}

.rec-task-row {
  display: flex;
  align-items: center;
  margin-top: 6rpx;
}

.rec-tag {
  font-size: 18rpx;
  background-color: #e6f4ff;
  color: #1677ff;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-right: 8rpx;
}

.rec-task-name {
  font-size: 22rpx;
  color: #595959;
}

.rec-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.rec-duration {
  font-size: 26rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.status-badge {
  font-size: 18rpx;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-top: 6rpx;
}

.state-container {
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

.state-text {
  font-size: 24rpx;
}

/* Modals */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
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
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
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

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-label {
  font-size: 28rpx;
  color: #1f2d3d;
}

.form-num-input {
  width: 140rpx;
  height: 64rpx;
  background-color: #f7f8fa;
  border: 1rpx solid #ebeef5;
  border-radius: 10rpx;
  text-align: center;
  font-size: 28rpx;
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

.btn-save {
  background-color: #1677ff;
  color: #ffffff;
  font-weight: 600;
}

/* 任务选择器面板 */
.picker-tabs {
  display: flex;
  border-bottom: 1rpx solid #ebeef5;
  margin-bottom: 16rpx;
}

.picker-tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #595959;
}

.picker-tab.active {
  color: #1677ff;
  font-weight: 600;
  border-bottom: 4rpx solid #1677ff;
}

.task-scroll-list {
  max-height: 400rpx;
}

.picker-item {
  padding: 18rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
}

.picker-item.selected {
  background-color: #e6f4ff;
  border: 1rpx solid #1677ff;
}

.item-title {
  font-size: 26rpx;
  color: #1f2d3d;
  font-weight: 500;
}

.item-sub {
  font-size: 20rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
}

.empty-picker {
  padding: 40rpx 0;
  text-align: center;
  font-size: 24rpx;
  color: #8c8c8c;
}
</style>

