<template>
  <view class="habits-page">
    <!-- 顶部习惯打卡统计卡片 -->
    <view class="life-card summary-card">
      <view class="summary-grid">
        <view class="summary-item">
          <text class="num text-primary">{{ summary.totalHabits }}</text>
          <text class="label">习惯总数</text>
        </view>
        <view class="summary-item">
          <text class="num text-warning">{{ summary.activeHabits }}</text>
          <text class="label">进行中</text>
        </view>
        <view class="summary-item">
          <text class="num text-success">{{ summary.todayCheckedHabits }}</text>
          <text class="label">今日已打卡</text>
        </view>
        <view class="summary-item">
          <text class="num text-purple">{{ summary.overallCompletionRate }}%</text>
          <text class="label">平均完成率</text>
        </view>
      </view>
    </view>

    <!-- 筛选与操作卡片 -->
    <view class="life-card action-card">
      <view class="filter-tabs">
        <view
          v-for="tab in filterTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: currentTab === tab.value }"
          @tap="switchTab(tab.value)"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 习惯卡片列表 -->
    <view class="life-card list-card">
      <view class="list-header">
        <text class="list-title">我的习惯 ({{ filteredHabits.length }})</text>
        <button class="add-btn" @tap="openCreateModal">+ 新建习惯</button>
      </view>

      <!-- 加载中状态 -->
      <view v-if="loading" class="state-container">
        <text class="loading-icon">⏳</text>
        <text class="state-text">正在加载习惯数据...</text>
      </view>

      <!-- 空数据状态 -->
      <view v-else-if="filteredHabits.length === 0" class="state-container">
        <text class="empty-icon">🌱</text>
        <text class="state-text">暂无相关习惯，坚持自律从现在开始！</text>
        <button class="empty-create-btn" @tap="openCreateModal">立即创建习惯</button>
      </view>

      <!-- 习惯卡片列表 -->
      <view v-else class="habit-items">
        <view
          v-for="habit in filteredHabits"
          :key="habit.id"
          class="habit-card-item"
          :class="{ 'is-paused': habit.status === 'PAUSED', 'is-archived': habit.status === 'ARCHIVED' }"
        >
          <!-- 习惯基本信息行 -->
          <view class="habit-main-row">
            <view class="habit-info">
              <view class="title-row">
                <text class="habit-name">{{ habit.name }}</text>
                <text class="freq-tag" :class="habit.frequency.toLowerCase()">
                  {{ habit.frequencyDesc }}{{ habit.targetCount > 1 ? ` ${habit.targetCount}次` : '' }}
                </text>
                <text v-if="habit.status !== 'ACTIVE'" class="status-tag" :class="habit.status.toLowerCase()">
                  {{ habit.statusDesc }}
                </text>
              </view>
              <text v-if="habit.description" class="habit-desc">{{ habit.description }}</text>
              <view class="meta-row">
                <text v-if="habit.reminderTime" class="meta-tag">⏰ 提醒: {{ habit.reminderTime.substring(0, 5) }}</text>
                <text class="meta-tag">📅 开始: {{ habit.startDate }}</text>
                <text v-if="habit.endDate" class="meta-tag">至 {{ habit.endDate }}</text>
              </view>
            </view>

            <!-- 今日打卡主要操作按钮 -->
            <view class="checkin-action">
              <button
                v-if="habit.status === 'ACTIVE'"
                class="checkin-btn"
                :class="{ 'is-checked': habit.todayChecked }"
                @tap="handleCheckinTap(habit)"
              >
                <text class="checkin-icon">{{ habit.todayChecked ? '✓' : '🌱' }}</text>
                <text class="checkin-text">{{ habit.todayChecked ? '已打卡' : '打卡' }}</text>
              </button>
              <text v-else class="status-badge-text">{{ habit.statusDesc }}</text>
            </view>
          </view>

          <!-- 数据指标与进度行 -->
          <view class="habit-stats-row">
            <view class="stat-badge">
              <text class="stat-icon">🔥</text>
              <text class="stat-label">连续</text>
              <text class="stat-val highlight">{{ habit.currentStreak }}</text>
              <text class="stat-unit">{{ habit.frequency === 'WEEKLY' ? '周' : '天' }}</text>
            </view>
            <view class="stat-badge">
              <text class="stat-icon">🏆</text>
              <text class="stat-label">最高</text>
              <text class="stat-val">{{ habit.maxStreak }}</text>
              <text class="stat-unit">{{ habit.frequency === 'WEEKLY' ? '周' : '天' }}</text>
            </view>
            <view class="stat-badge">
              <text class="stat-icon">📈</text>
              <text class="stat-label">完成率</text>
              <text class="stat-val">{{ habit.completionRate }}%</text>
            </view>
            <view class="stat-badge">
              <text class="stat-icon">📝</text>
              <text class="stat-label">累计</text>
              <text class="stat-val">{{ habit.totalCheckins }}次</text>
            </view>
          </view>

          <!-- 底部功能按钮条 -->
          <view class="habit-footer-row">
            <view class="footer-btn calendar-btn" @tap="openCalendar(habit)">
              <text class="btn-icon">📅</text>
              <text>打卡日历</text>
            </view>
            <view class="footer-btn" @tap="openEditModal(habit)">
              <text class="btn-icon">✏️</text>
              <text>编辑</text>
            </view>
            <view
              v-if="habit.status === 'ACTIVE'"
              class="footer-btn text-warning"
              @tap="toggleStatus(habit, 'PAUSED')"
            >
              <text class="btn-icon">⏸️</text>
              <text>暂停</text>
            </view>
            <view
              v-else-if="habit.status === 'PAUSED'"
              class="footer-btn text-success"
              @tap="toggleStatus(habit, 'ACTIVE')"
            >
              <text class="btn-icon">▶️</text>
              <text>恢复</text>
            </view>
            <view
              v-if="habit.status !== 'ARCHIVED'"
              class="footer-btn"
              @tap="toggleStatus(habit, 'ARCHIVED')"
            >
              <text class="btn-icon">🗄️</text>
              <text>归档</text>
            </view>
            <view class="footer-btn text-danger" @tap="confirmDelete(habit)">
              <text class="btn-icon">🗑️</text>
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 打卡月历弹窗 -->
    <view v-if="calendarVisible" class="modal-overlay" @tap.self="closeCalendar">
      <view class="modal-card calendar-modal">
        <view class="modal-header">
          <text class="modal-title">📅 {{ currentCalendar?.habitName }} 打卡日历</text>
          <text class="close-icon" @tap="closeCalendar">✕</text>
        </view>

        <!-- 月份切换栏 -->
        <view class="month-selector">
          <button class="month-nav-btn" @tap="prevMonth">‹ 上个月</button>
          <text class="current-month-text">{{ calendarYear }} 年 {{ calendarMonth }} 月</text>
          <button class="month-nav-btn" @tap="nextMonth">下个月 ›</button>
        </view>

        <!-- 星期标题行 -->
        <view class="calendar-week-row">
          <text v-for="w in ['日', '一', '二', '三', '四', '五', '六']" :key="w" class="week-title">{{ w }}</text>
        </view>

        <!-- 日期格子矩阵 -->
        <view class="calendar-days-grid">
          <view
            v-for="(dayItem, idx) in calendarGrid"
            :key="idx"
            class="day-cell"
            :class="{
              'is-empty': !dayItem.day,
              'is-checked': dayItem.checked,
              'is-today': dayItem.isToday
            }"
          >
            <text v-if="dayItem.day" class="day-number">{{ dayItem.day }}</text>
            <text v-if="dayItem.checked" class="check-mark">✓</text>
          </view>
        </view>

        <!-- 月度统计卡片 -->
        <view class="calendar-stats-box">
          <view class="c-stat-item">
            <text class="c-stat-label">本月打卡</text>
            <text class="c-stat-val text-success">{{ currentCalendar?.monthCheckins || 0 }} 天</text>
          </view>
          <view class="c-stat-item">
            <text class="c-stat-label">本月完成率</text>
            <text class="c-stat-val text-primary">{{ currentCalendar?.monthCompletionRate || 0 }}%</text>
          </view>
          <view class="c-stat-item">
            <text class="c-stat-label">连续打卡</text>
            <text class="c-stat-val text-warning">{{ currentCalendar?.currentStreak || 0 }} 天</text>
          </view>
          <view class="c-stat-item">
            <text class="c-stat-label">历史最高</text>
            <text class="c-stat-val">{{ currentCalendar?.maxStreak || 0 }} 天</text>
          </view>
        </view>

        <view class="modal-footer">
          <button class="life-btn-secondary" @tap="closeCalendar">关闭</button>
        </view>
      </view>
    </view>

    <!-- 打卡心得/取消打卡弹窗 -->
    <view v-if="checkinModalVisible" class="modal-overlay" @tap.self="closeCheckinModal">
      <view class="modal-card checkin-modal">
        <view class="modal-header">
          <text class="modal-title">🌱 {{ activeHabit?.name }} - 习惯打卡</text>
          <text class="close-icon" @tap="closeCheckinModal">✕</text>
        </view>

        <view class="modal-body">
          <view v-if="activeHabit?.todayChecked" class="already-checked-box">
            <text class="checked-tip-icon">🎉</text>
            <text class="checked-tip-text">今天您已经完成此习惯打卡！</text>
            <button class="cancel-checkin-btn" @tap="handleCancelCheckin">取消今日打卡</button>
          </view>

          <view v-else class="checkin-form">
            <text class="form-label">今日心得 / 备注（可选）：</text>
            <textarea
              v-model="checkinNote"
              class="form-textarea"
              placeholder="记录一下今天完成习惯的心情或收获..."
              maxlength="200"
            />
            <button class="life-btn-primary submit-checkin-btn" @tap="submitCheckin">
              确认打卡 ✓
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 创建 / 编辑习惯弹窗 -->
    <view v-if="editModalVisible" class="modal-overlay" @tap.self="closeEditModal">
      <view class="modal-card edit-modal">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑习惯' : '新建习惯' }}</text>
          <text class="close-icon" @tap="closeEditModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-item">
            <text class="label required">习惯名称</text>
            <input
              v-model="habitForm.name"
              class="form-input"
              placeholder="例如：每天晨跑 5 公里、坚持早睡"
              maxlength="50"
            />
          </view>

          <view class="form-item">
            <text class="label">习惯描述</text>
            <input
              v-model="habitForm.description"
              class="form-input"
              placeholder="可选，添加鼓励自己的一句话"
              maxlength="200"
            />
          </view>

          <view class="form-row">
            <view class="form-item flex-1">
              <text class="label">打卡频次</text>
              <picker
                :range="frequencyOptions"
                range-key="label"
                :value="selectedFreqIndex"
                @change="onFreqChange"
              >
                <view class="picker-value">
                  <text>{{ frequencyOptions[selectedFreqIndex].label }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>

            <view class="form-item flex-1">
              <text class="label">目标次数</text>
              <input
                v-model.number="habitForm.targetCount"
                type="number"
                class="form-input"
                placeholder="1"
              />
            </view>
          </view>

          <view class="form-row">
            <view class="form-item flex-1">
              <text class="label required">开始日期</text>
              <picker mode="date" :value="habitForm.startDate" @change="onStartDateChange">
                <view class="picker-value">
                  <text>{{ habitForm.startDate || '选择开始日期' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>

            <view class="form-item flex-1">
              <text class="label">结束日期</text>
              <picker mode="date" :value="habitForm.endDate" @change="onEndDateChange">
                <view class="picker-value">
                  <text>{{ habitForm.endDate || '永久持续' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
          </view>

          <view class="form-item">
            <text class="label">每日提醒时间</text>
            <picker mode="time" :value="habitForm.reminderTime" @change="onTimeChange">
              <view class="picker-value">
                <text>{{ habitForm.reminderTime || '不设置提醒' }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
        </view>

        <view class="modal-footer">
          <button class="life-btn-secondary" @tap="closeEditModal">取消</button>
          <button class="life-btn-primary" @tap="submitHabitForm">保存习惯</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import {
  cancelCheckinApi,
  checkinHabitApi,
  createHabitApi,
  deleteHabitApi,
  getHabitCalendarApi,
  getHabitListApi,
  getHabitSummaryApi,
  updateHabitApi,
  updateHabitStatusApi
} from '../../api/habit'
import type {
  CreateHabitDTO,
  HabitCalendarData,
  HabitItem,
  HabitSummaryData
} from '../../types/habit'

const loading = ref(false)
const habits = ref<HabitItem[]>([])
const currentTab = ref('ALL')

const summary = reactive<HabitSummaryData>({
  totalHabits: 0,
  activeHabits: 0,
  todayCheckedHabits: 0,
  overallCompletionRate: 0
})

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '已暂停', value: 'PAUSED' },
  { label: '已归档', value: 'ARCHIVED' }
]

const frequencyOptions = [
  { label: '每日打卡 (DAILY)', value: 'DAILY' },
  { label: '每周打卡 (WEEKLY)', value: 'WEEKLY' }
]

const selectedFreqIndex = ref(0)

const filteredHabits = computed(() => {
  if (currentTab.value === 'ALL') {
    return habits.value
  }
  return habits.value.filter((h) => h.status === currentTab.value)
})

// 月历相关状态
const calendarVisible = ref(false)
const currentCalendar = ref<HabitCalendarData | null>(null)
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth() + 1)
const calendarHabitId = ref<number | null>(null)

// 模态弹窗相关
const checkinModalVisible = ref(false)
const activeHabit = ref<HabitItem | null>(null)
const checkinNote = ref('')

const editModalVisible = ref(false)
const isEditing = ref(false)
const editingHabitId = ref<number | null>(null)

const getTodayDateStr = () => {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const habitForm = reactive<CreateHabitDTO>({
  name: '',
  description: '',
  frequency: 'DAILY',
  targetCount: 1,
  startDate: getTodayDateStr(),
  endDate: '',
  reminderTime: ''
})

// 数据加载
const loadData = async () => {
  loading.value = true
  try {
    const [listRes, sumRes] = await Promise.all([
      getHabitListApi(),
      getHabitSummaryApi()
    ])
    if (listRes.code === 0 && listRes.data) {
      habits.value = listRes.data
    }
    if (sumRes.code === 0 && sumRes.data) {
      Object.assign(summary, sumRes.data)
    }
  } catch (e) {
    uni.showToast({ title: '拉取习惯数据失败', icon: 'none' })
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

const switchTab = (tabValue: string) => {
  currentTab.value = tabValue
}

// 打卡按钮点击
const handleCheckinTap = (habit: HabitItem) => {
  activeHabit.value = habit
  checkinNote.value = ''
  checkinModalVisible.value = true
}

const closeCheckinModal = () => {
  checkinModalVisible.value = false
  activeHabit.value = null
}

const submitCheckin = async () => {
  if (!activeHabit.value) return
  try {
    const res = await checkinHabitApi(activeHabit.value.id, {
      note: checkinNote.value
    })
    if (res.code === 0) {
      uni.showToast({ title: '打卡成功！自律又进了一步 🌱', icon: 'success' })
      closeCheckinModal()
      loadData()
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '打卡失败', icon: 'none' })
  }
}

const handleCancelCheckin = async () => {
  if (!activeHabit.value) return
  uni.showModal({
    title: '取消确认',
    content: `确定取消「${activeHabit.value.name}」今日打卡吗？`,
    success: async (r) => {
      if (r.confirm && activeHabit.value) {
        try {
          const res = await cancelCheckinApi(activeHabit.value.id)
          if (res.code === 0) {
            uni.showToast({ title: '已取消今日打卡', icon: 'success' })
            closeCheckinModal()
            loadData()
          }
        } catch (e: any) {
          uni.showToast({ title: e.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
}

// 状态流转（暂停 / 恢复 / 归档）
const toggleStatus = async (habit: HabitItem, newStatus: string) => {
  try {
    const res = await updateHabitStatusApi(habit.id, newStatus)
    if (res.code === 0) {
      uni.showToast({ title: '状态更新成功', icon: 'success' })
      loadData()
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

// 删除习惯
const confirmDelete = (habit: HabitItem) => {
  uni.showModal({
    title: '删除习惯',
    content: `确定要删除「${habit.name}」吗？历史打卡数据将一同隐藏。`,
    confirmColor: '#ff4d4f',
    success: async (r) => {
      if (r.confirm) {
        try {
          const res = await deleteHabitApi(habit.id)
          if (res.code === 0) {
            uni.showToast({ title: '已删除习惯', icon: 'success' })
            loadData()
          }
        } catch (e: any) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 打开月历
const openCalendar = async (habit: HabitItem) => {
  calendarHabitId.value = habit.id
  calendarYear.value = new Date().getFullYear()
  calendarMonth.value = new Date().getMonth() + 1
  calendarVisible.value = true
  fetchCalendarData()
}

const closeCalendar = () => {
  calendarVisible.value = false
  currentCalendar.value = null
  calendarHabitId.value = null
}

const fetchCalendarData = async () => {
  if (!calendarHabitId.value) return
  try {
    const res = await getHabitCalendarApi(
      calendarHabitId.value,
      calendarYear.value,
      calendarMonth.value
    )
    if (res.code === 0 && res.data) {
      currentCalendar.value = res.data
    }
  } catch (e) {
    uni.showToast({ title: '加载月历失败', icon: 'none' })
  }
}

const prevMonth = () => {
  if (calendarMonth.value === 1) {
    calendarYear.value -= 1
    calendarMonth.value = 12
  } else {
    calendarMonth.value -= 1
  }
  fetchCalendarData()
}

const nextMonth = () => {
  if (calendarMonth.value === 12) {
    calendarYear.value += 1
    calendarMonth.value = 1
  } else {
    calendarMonth.value += 1
  }
  fetchCalendarData()
}

// 计算当前月份日历网格
const calendarGrid = computed(() => {
  const grid: { day: number | null; checked: boolean; isToday: boolean }[] = []
  const y = calendarYear.value
  const m = calendarMonth.value

  const firstDayOfWeek = new Date(y, m - 1, 1).getDay() // 0(周日) ~ 6
  const totalDays = new Date(y, m, 0).getDate()

  // 占位空白
  for (let i = 0; i < firstDayOfWeek; i++) {
    grid.push({ day: null, checked: false, isToday: false })
  }

  const checkedDates = new Set(currentCalendar.value?.checkinDates || [])
  const todayStr = getTodayDateStr()

  for (let d = 1; d <= totalDays; d++) {
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    grid.push({
      day: d,
      checked: checkedDates.has(dateStr),
      isToday: dateStr === todayStr
    })
  }

  return grid
})

// 创建与编辑弹窗
const openCreateModal = () => {
  isEditing.value = false
  editingHabitId.value = null
  selectedFreqIndex.value = 0
  habitForm.name = ''
  habitForm.description = ''
  habitForm.frequency = 'DAILY'
  habitForm.targetCount = 1
  habitForm.startDate = getTodayDateStr()
  habitForm.endDate = ''
  habitForm.reminderTime = ''
  editModalVisible.value = true
}

const openEditModal = (habit: HabitItem) => {
  isEditing.value = true
  editingHabitId.value = habit.id
  selectedFreqIndex.value = habit.frequency === 'WEEKLY' ? 1 : 0
  habitForm.name = habit.name
  habitForm.description = habit.description || ''
  habitForm.frequency = habit.frequency
  habitForm.targetCount = habit.targetCount
  habitForm.startDate = habit.startDate
  habitForm.endDate = habit.endDate || ''
  habitForm.reminderTime = habit.reminderTime ? habit.reminderTime.substring(0, 5) : ''
  editModalVisible.value = true
}

const closeEditModal = () => {
  editModalVisible.value = false
  editingHabitId.value = null
}

const onFreqChange = (e: any) => {
  const idx = Number(e.detail.value)
  selectedFreqIndex.value = idx
  habitForm.frequency = frequencyOptions[idx].value as 'DAILY' | 'WEEKLY'
}

const onStartDateChange = (e: any) => {
  habitForm.startDate = e.detail.value
}

const onEndDateChange = (e: any) => {
  habitForm.endDate = e.detail.value
}

const onTimeChange = (e: any) => {
  habitForm.reminderTime = e.detail.value
}

const submitHabitForm = async () => {
  if (!habitForm.name.trim()) {
    uni.showToast({ title: '请输入习惯名称', icon: 'none' })
    return
  }
  if (!habitForm.startDate) {
    uni.showToast({ title: '请选择开始日期', icon: 'none' })
    return
  }

  try {
    if (isEditing.value && editingHabitId.value) {
      const res = await updateHabitApi(editingHabitId.value, habitForm)
      if (res.code === 0) {
        uni.showToast({ title: '修改成功', icon: 'success' })
        closeEditModal()
        loadData()
      }
    } else {
      const res = await createHabitApi(habitForm)
      if (res.code === 0) {
        uni.showToast({ title: '创建成功', icon: 'success' })
        closeEditModal()
        loadData()
      }
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  }
}

onMounted(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.habits-page {
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
  .summary-grid {
    display: flex;
    justify-content: space-around;
    text-align: center;

    .summary-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .num {
        font-size: 38rpx;
        font-weight: 700;
        line-height: 1.2;
      }

      .label {
        font-size: 24rpx;
        color: #8c8c8c;
        margin-top: 8rpx;
      }
    }
  }
}

.text-primary { color: #1677ff; }
.text-warning { color: #fa8c16; }
.text-success { color: #52c41a; }
.text-purple { color: #722ed1; }
.text-danger { color: #ff4d4f; }

.action-card {
  padding: 16rpx 20rpx;

  .filter-tabs {
    display: flex;
    justify-content: space-between;

    .tab-item {
      padding: 12rpx 28rpx;
      font-size: 28rpx;
      color: #595959;
      border-radius: 30rpx;
      transition: all 0.2s;

      &.active {
        color: #1677ff;
        background: #e6f4ff;
        font-weight: 600;
      }
    }
  }
}

.list-card {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .list-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #262626;
    }

    .add-btn {
      margin: 0;
      padding: 0 24rpx;
      height: 60rpx;
      line-height: 60rpx;
      background: #1677ff;
      color: #ffffff;
      font-size: 26rpx;
      border-radius: 30rpx;
    }
  }
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

  .empty-create-btn {
    background: #1677ff;
    color: #ffffff;
    font-size: 28rpx;
    padding: 0 40rpx;
    height: 68rpx;
    line-height: 68rpx;
    border-radius: 34rpx;
  }
}

.habit-items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;

  .habit-card-item {
    background: #fafafa;
    border: 2rpx solid #f0f0f0;
    border-radius: 16rpx;
    padding: 24rpx;

    &.is-paused {
      opacity: 0.75;
      background: #f5f5f5;
    }

    &.is-archived {
      opacity: 0.6;
      background: #f0f0f0;
    }

    .habit-main-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 20rpx;

      .habit-info {
        flex: 1;
        margin-right: 20rpx;

        .title-row {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 12rpx;
          margin-bottom: 8rpx;

          .habit-name {
            font-size: 32rpx;
            font-weight: 600;
            color: #262626;
          }

          .freq-tag {
            font-size: 20rpx;
            padding: 2rpx 12rpx;
            border-radius: 12rpx;
            background: #e6f4ff;
            color: #1677ff;

            &.weekly {
              background: #f9f0ff;
              color: #722ed1;
            }
          }

          .status-tag {
            font-size: 20rpx;
            padding: 2rpx 12rpx;
            border-radius: 12rpx;

            &.paused {
              background: #fff7e6;
              color: #fa8c16;
            }

            &.archived {
              background: #f5f5f5;
              color: #8c8c8c;
            }
          }
        }

        .habit-desc {
          font-size: 24rpx;
          color: #595959;
          margin-bottom: 8rpx;
          display: block;
        }

        .meta-row {
          display: flex;
          flex-wrap: wrap;
          gap: 16rpx;

          .meta-tag {
            font-size: 22rpx;
            color: #8c8c8c;
          }
        }
      }

      .checkin-action {
        .checkin-btn {
          margin: 0;
          padding: 0 28rpx;
          height: 72rpx;
          line-height: 72rpx;
          background: #52c41a;
          color: #ffffff;
          border-radius: 36rpx;
          font-size: 28rpx;
          display: flex;
          align-items: center;
          gap: 8rpx;
          box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.25);

          &.is-checked {
            background: #f6ffed;
            color: #52c41a;
            border: 2rpx solid #b7eb8f;
            box-shadow: none;
          }
        }

        .status-badge-text {
          font-size: 26rpx;
          color: #8c8c8c;
        }
      }
    }

    .habit-stats-row {
      display: flex;
      justify-content: space-between;
      background: #ffffff;
      padding: 14rpx 20rpx;
      border-radius: 12rpx;
      margin-bottom: 16rpx;

      .stat-badge {
        display: flex;
        align-items: center;
        gap: 6rpx;
        font-size: 22rpx;
        color: #8c8c8c;

        .stat-val {
          font-weight: 700;
          color: #262626;

          &.highlight {
            color: #ff4d4f;
            font-size: 26rpx;
          }
        }
      }
    }

    .habit-footer-row {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 24rpx;
      padding-top: 12rpx;
      border-top: 2rpx dashed #e8e8e8;

      .footer-btn {
        font-size: 24rpx;
        color: #595959;
        display: flex;
        align-items: center;
        gap: 4rpx;

        &.calendar-btn {
          color: #1677ff;
          font-weight: 600;
          margin-right: auto;
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
  box-sizing: border-box;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 28rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #262626;
    }

    .close-icon {
      font-size: 36rpx;
      color: #8c8c8c;
    }
  }

  .modal-body {
    padding: 28rpx;
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

/* 月历弹窗 */
.calendar-modal {
  width: 680rpx;

  .month-selector {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 24rpx;

    .month-nav-btn {
      margin: 0;
      height: 52rpx;
      line-height: 52rpx;
      font-size: 24rpx;
      padding: 0 16rpx;
      background: #f5f5f5;
      border-radius: 26rpx;
      color: #595959;
    }

    .current-month-text {
      font-size: 30rpx;
      font-weight: 700;
      color: #262626;
    }
  }

  .calendar-week-row {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    text-align: center;
    padding: 10rpx 0;
    color: #8c8c8c;
    font-size: 24rpx;
  }

  .calendar-days-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8rpx;
    padding: 12rpx 20rpx;

    .day-cell {
      height: 72rpx;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border-radius: 12rpx;
      position: relative;
      background: #fafafa;

      &.is-empty {
        background: transparent;
      }

      .day-number {
        font-size: 26rpx;
        color: #262626;
      }

      &.is-today {
        border: 2rpx solid #1677ff;
      }

      &.is-checked {
        background: #f6ffed;
        border: 2rpx solid #52c41a;

        .day-number {
          color: #52c41a;
          font-weight: 700;
        }

        .check-mark {
          font-size: 16rpx;
          color: #52c41a;
          position: absolute;
          bottom: 4rpx;
        }
      }
    }
  }

  .calendar-stats-box {
    display: flex;
    justify-content: space-around;
    background: #f7f8fa;
    margin: 16rpx 24rpx;
    padding: 16rpx 10rpx;
    border-radius: 12rpx;
    text-align: center;

    .c-stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .c-stat-label {
        font-size: 22rpx;
        color: #8c8c8c;
        margin-bottom: 4rpx;
      }

      .c-stat-val {
        font-size: 28rpx;
        font-weight: 700;
      }
    }
  }
}

/* 打卡心得弹窗 */
.checkin-modal {
  .already-checked-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 0;

    .checked-tip-icon {
      font-size: 72rpx;
      margin-bottom: 12rpx;
    }

    .checked-tip-text {
      font-size: 30rpx;
      color: #262626;
      margin-bottom: 30rpx;
    }

    .cancel-checkin-btn {
      background: #fff1f0;
      color: #ff4d4f;
      border: 2rpx solid #ffa39e;
      border-radius: 36rpx;
      font-size: 28rpx;
      height: 72rpx;
      line-height: 72rpx;
      padding: 0 40rpx;
    }
  }

  .checkin-form {
    .form-label {
      font-size: 28rpx;
      color: #595959;
      margin-bottom: 16rpx;
      display: block;
    }

    .form-textarea {
      width: 100%;
      height: 180rpx;
      background: #fafafa;
      border: 2rpx solid #e8e8e8;
      border-radius: 12rpx;
      padding: 16rpx;
      font-size: 28rpx;
      box-sizing: border-box;
      margin-bottom: 24rpx;
    }

    .submit-checkin-btn {
      width: 100%;
      height: 80rpx;
      line-height: 80rpx;
      background: #52c41a;
      color: #ffffff;
      font-size: 30rpx;
      border-radius: 40rpx;
    }
  }
}

/* 编辑/创建弹窗表单 */
.edit-modal {
  .form-item {
    margin-bottom: 20rpx;

    .label {
      font-size: 26rpx;
      color: #595959;
      margin-bottom: 8rpx;
      display: block;

      &.required::before {
        content: '* ';
        color: #ff4d4f;
      }
    }

    .form-input {
      height: 72rpx;
      background: #fafafa;
      border: 2rpx solid #e8e8e8;
      border-radius: 12rpx;
      padding: 0 16rpx;
      font-size: 28rpx;
    }

    .picker-value {
      height: 72rpx;
      background: #fafafa;
      border: 2rpx solid #e8e8e8;
      border-radius: 12rpx;
      padding: 0 16rpx;
      font-size: 28rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: #262626;

      .picker-arrow {
        color: #8c8c8c;
      }
    }
  }

  .form-row {
    display: flex;
    gap: 20rpx;

    .flex-1 {
      flex: 1;
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
</style>

