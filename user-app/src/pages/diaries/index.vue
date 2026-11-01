<template>
  <view class="diaries-page">
    <!-- 顶部卡片：统计与新建 -->
    <view class="life-card header-card">
      <view class="header-content">
        <view class="title-meta">
          <text class="page-title">📔 心情日记</text>
          <text class="page-subtitle">记录生活点滴、情绪起伏与心灵沉淀</text>
        </view>
        <button class="write-btn" @tap="openCreateModal">+ 写日记</button>
      </view>

      <view class="privacy-notice">
        <text class="notice-icon">🔒</text>
        <text class="notice-text">隐私保障：日记内容仅您本人可见，绝不进入系统常规业务日志</text>
      </view>

      <view class="diary-stats-bar">
        <view class="stat-cell">
          <text class="num">{{ totalCount }}</text>
          <text class="desc">累计篇数</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-cell">
          <text class="num text-primary">{{ thisMonthCount }}</text>
          <text class="desc">本月记录</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-cell">
          <text class="num text-success">{{ todayRecorded ? '已写' : '未写' }}</text>
          <text class="desc">今日状态</text>
        </view>
      </view>
    </view>

    <!-- 筛选工具栏 -->
    <view class="life-card filter-card">
      <view class="search-row">
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索日记正文或标题..."
          confirm-type="search"
          @confirm="onSearch"
        />
        <button v-if="searchKeyword" class="clear-btn" @tap="clearSearch">✕</button>
        <button class="search-btn" @tap="onSearch">搜索</button>
      </view>

      <!-- 心情快捷筛选 -->
      <view class="mood-chips-row">
        <view
          class="mood-chip"
          :class="{ active: selectedMood === '' }"
          @tap="selectMood('')"
        >
          全部心情
        </view>
        <view
          v-for="m in moodOptions"
          :key="m.value"
          class="mood-chip"
          :class="{ active: selectedMood === m.value }"
          @tap="selectMood(m.value)"
        >
          {{ m.icon }} {{ m.label }}
        </view>
      </view>
    </view>

    <!-- 日记卡片时间轴列表 -->
    <view class="diary-list">
      <view v-if="loading && diaries.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>

      <view v-else-if="diaries.length === 0" class="empty-state">
        <text class="empty-icon">📖</text>
        <text class="empty-text">这里还没有写过日记呢</text>
        <button class="empty-btn" @tap="openCreateModal">记下今天的点滴</button>
      </view>

      <view
        v-for="d in diaries"
        :key="d.id"
        class="life-card diary-card"
      >
        <view class="diary-header">
          <view class="date-badge">
            <text class="date-day">{{ d.diaryDate.substring(8, 10) }}</text>
            <text class="date-month">{{ d.diaryDate.substring(0, 7) }}</text>
          </view>

          <view class="mood-weather-badges">
            <text v-if="d.mood" class="pill-badge pill-mood">
              {{ getMoodDisplay(d.mood) }}
            </text>
            <text v-if="d.weather" class="pill-badge pill-weather">
              {{ getWeatherDisplay(d.weather) }}
            </text>
          </view>
        </view>

        <view v-if="d.title" class="diary-title-box">
          <text class="diary-title">{{ d.title }}</text>
        </view>

        <view class="diary-body">
          <text class="diary-text">{{ d.content }}</text>
        </view>

        <view class="diary-footer">
          <text class="time-meta">创建于 {{ d.createdAt ? d.createdAt.substring(11, 16) : '' }}</text>

          <view class="card-actions">
            <button class="action-btn" @tap="openEditModal(d)">编辑</button>
            <button class="action-btn text-danger" @tap="handleDeleteDiary(d)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 写日记 / 编辑日记弹窗 -->
    <view v-if="diaryModalVisible" class="modal-overlay" @tap.self="closeDiaryModal">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑日记' : '写新日记' }}</text>
          <text class="close-icon" @tap="closeDiaryModal">✕</text>
        </view>

        <scroll-view scroll-y class="modal-scroll-body">
          <view class="form-group">
            <text class="form-label required">日记日期</text>
            <picker
              mode="date"
              :value="diaryForm.diaryDate"
              :disabled="isEditing"
              @change="onDateChange"
            >
              <view class="picker-input" :class="{ disabled: isEditing }">
                <text>{{ diaryForm.diaryDate }}</text>
                <text class="picker-arrow">📅</text>
              </view>
            </picker>
            <text v-if="isEditing" class="field-hint">日记日期不可直接变更，如需调整请新建对应日期的日记</text>
          </view>

          <view class="form-group">
            <text class="form-label">今日心情</text>
            <view class="options-grid">
              <view
                v-for="m in moodOptions"
                :key="m.value"
                class="option-pill"
                :class="{ active: diaryForm.mood === m.value }"
                @tap="diaryForm.mood = m.value"
              >
                <text class="opt-icon">{{ m.icon }}</text>
                <text class="opt-label">{{ m.label }}</text>
              </view>
            </view>
          </view>

          <view class="form-group">
            <text class="form-label">今日天气</text>
            <view class="options-grid">
              <view
                v-for="w in weatherOptions"
                :key="w.value"
                class="option-pill"
                :class="{ active: diaryForm.weather === w.value }"
                @tap="diaryForm.weather = w.value"
              >
                <text class="opt-icon">{{ w.icon }}</text>
                <text class="opt-label">{{ w.label }}</text>
              </view>
            </view>
          </view>

          <view class="form-group">
            <text class="form-label">日记标题（可选）</text>
            <input
              v-model="diaryForm.title"
              class="form-input"
              placeholder="如：收获满满的周末、静心思考的一天"
            />
          </view>

          <view class="form-group">
            <text class="form-label required">日记正文</text>
            <textarea
              v-model="diaryForm.content"
              class="form-textarea-diary"
              placeholder="写下今天发生的事情、内心的感受、思考或值得记住的小瞬间..."
            />
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn-cancel" @tap="closeDiaryModal">取消</button>
          <button class="btn-confirm" @tap="submitDiaryForm">保存日记</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getDiariesApi,
  createDiaryApi,
  updateDiaryApi,
  deleteDiaryApi,
  getLifeSummaryApi
} from '../../api/life'
import type { Diary } from '../../types/life'

const moodOptions = [
  { value: 'HAPPY', label: '开心', icon: '😄' },
  { value: 'PEACE', label: '平静', icon: '😌' },
  { value: 'EXCITED', label: '兴奋', icon: '🥳' },
  { value: 'TIRED', label: '疲惫', icon: '😴' },
  { value: 'SAD', label: '低落', icon: '😔' },
  { value: 'ANXIOUS', label: '焦虑', icon: '😰' }
]

const weatherOptions = [
  { value: 'SUNNY', label: '晴天', icon: '☀️' },
  { value: 'CLOUDY', label: '多云', icon: '⛅' },
  { value: 'OVERCAST', label: '阴天', icon: '🌫️' },
  { value: 'RAINY', label: '雨天', icon: '🌧️' },
  { value: 'SNOWY', label: '雪天', icon: '❄️' },
  { value: 'WINDY', label: '大风', icon: '💨' }
]

const getMoodDisplay = (code: string) => {
  const m = moodOptions.find((x) => x.value === code)
  return m ? `${m.icon} ${m.label}` : code
}

const getWeatherDisplay = (code: string) => {
  const w = weatherOptions.find((x) => x.value === code)
  return w ? `${w.icon} ${w.label}` : code
}

const getTodayString = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const date = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${date}`
}

const diaries = ref<Diary[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedMood = ref('')

const totalCount = ref(0)
const thisMonthCount = ref(0)

const todayStr = getTodayString()
const todayRecorded = computed(() => diaries.value.some((d) => d.diaryDate === todayStr))

const loadStats = async () => {
  try {
    const res = await getLifeSummaryApi()
    if (res.data) {
      totalCount.value = res.data.totalDiaryCount
      thisMonthCount.value = res.data.thisMonthDiaryCount
    }
  } catch (err) {
    console.error('加载日记统计失败', err)
  }
}

const loadDiaries = async () => {
  loading.value = true
  try {
    const params: {
      keyword?: string
      mood?: string
      current?: number
      size?: number
    } = {
      current: 1,
      size: 100
    }

    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    if (selectedMood.value) {
      params.mood = selectedMood.value
    }

    const res = await getDiariesApi(params)
    if (res.data && res.data.records) {
      diaries.value = res.data.records
    } else {
      diaries.value = []
    }
  } catch (err) {
    uni.showToast({ title: '加载日记失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  loadDiaries()
}

const clearSearch = () => {
  searchKeyword.value = ''
  loadDiaries()
}

const selectMood = (mood: string) => {
  selectedMood.value = mood
  loadDiaries()
}

// ================= 表单弹窗 =================
const diaryModalVisible = ref(false)
const isEditing = ref(false)
const editingDiaryId = ref<number | null>(null)

const diaryForm = ref({
  diaryDate: getTodayString(),
  title: '',
  content: '',
  mood: 'PEACE',
  weather: 'SUNNY'
})

const openCreateModal = () => {
  isEditing.value = false
  editingDiaryId.value = null
  diaryForm.value = {
    diaryDate: getTodayString(),
    title: '',
    content: '',
    mood: 'PEACE',
    weather: 'SUNNY'
  }
  diaryModalVisible.value = true
}

const openEditModal = (d: Diary) => {
  isEditing.value = true
  editingDiaryId.value = d.id
  diaryForm.value = {
    diaryDate: d.diaryDate,
    title: d.title || '',
    content: d.content,
    mood: d.mood || 'PEACE',
    weather: d.weather || 'SUNNY'
  }
  diaryModalVisible.value = true
}

const closeDiaryModal = () => {
  diaryModalVisible.value = false
}

const onDateChange = (e: any) => {
  diaryForm.value.diaryDate = e.detail.value
}

const submitDiaryForm = async () => {
  if (!diaryForm.value.content.trim()) {
    uni.showToast({ title: '请输入日记正文', icon: 'none' })
    return
  }

  try {
    if (isEditing.value && editingDiaryId.value) {
      await updateDiaryApi(editingDiaryId.value, {
        title: diaryForm.value.title.trim() || undefined,
        content: diaryForm.value.content.trim(),
        mood: diaryForm.value.mood,
        weather: diaryForm.value.weather
      })
      uni.showToast({ title: '日记修改成功', icon: 'success' })
    } else {
      await createDiaryApi({
        diaryDate: diaryForm.value.diaryDate,
        title: diaryForm.value.title.trim() || undefined,
        content: diaryForm.value.content.trim(),
        mood: diaryForm.value.mood,
        weather: diaryForm.value.weather
      })
      uni.showToast({ title: '日记保存成功', icon: 'success' })
    }

    closeDiaryModal()
    loadDiaries()
    loadStats()
  } catch (err: any) {
    uni.showToast({ title: err.message || '保存失败', icon: 'none' })
  }
}

const handleDeleteDiary = (d: Diary) => {
  uni.showModal({
    title: '删除日记',
    content: `确定要删除 ${d.diaryDate} 的这篇日记吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteDiaryApi(d.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadDiaries()
          loadStats()
        } catch (err: any) {
          uni.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadStats()
  loadDiaries()
})
</script>

<style lang="scss" scoped>
.diaries-page {
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

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
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

.write-btn {
  background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%);
  color: #ffffff;
  font-size: 26rpx;
  padding: 8rpx 28rpx;
  border-radius: 30rpx;
  line-height: 1.6;
}

.privacy-notice {
  background-color: #f6ffed;
  border: 1rpx solid #b7eb8f;
  border-radius: 10rpx;
  padding: 10rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.notice-icon {
  font-size: 24rpx;
}

.notice-text {
  font-size: 22rpx;
  color: #389e0d;
}

.diary-stats-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 14rpx 0;
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.num {
  font-size: 32rpx;
  font-weight: bold;
  color: #262626;
}

.desc {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 2rpx;
}

.stat-divider {
  width: 1rpx;
  height: 36rpx;
  background-color: #f0f0f0;
}

.filter-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.search-input {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 30rpx;
  padding: 12rpx 24rpx;
  font-size: 26rpx;
}

.clear-btn {
  font-size: 24rpx;
  color: #8c8c8c;
  background: none;
  padding: 0 10rpx;
}

.search-btn {
  background-color: #f0f0f0;
  font-size: 24rpx;
  color: #262626;
  border-radius: 30rpx;
  padding: 10rpx 20rpx;
}

.mood-chips-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.mood-chip {
  font-size: 22rpx;
  background-color: #f5f5f5;
  color: #595959;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;

  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
    font-weight: bold;
  }
}

.diary-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.date-badge {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.date-day {
  font-size: 38rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.date-month {
  font-size: 24rpx;
  color: #8c8c8c;
}

.mood-weather-badges {
  display: flex;
  gap: 10rpx;
}

.pill-badge {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
}

.pill-mood {
  background-color: #fff7e6;
  color: #d46b08;
  border: 1rpx solid #ffd591;
}

.pill-weather {
  background-color: #e6f7ff;
  color: #096dd9;
  border: 1rpx solid #91d5ff;
}

.diary-title-box {
  margin-bottom: 12rpx;
}

.diary-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.diary-body {
  margin-bottom: 16rpx;
}

.diary-text {
  font-size: 28rpx;
  color: #333333;
  line-height: 1.7;
  white-space: pre-wrap;
}

.diary-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 14rpx;
}

.time-meta {
  font-size: 22rpx;
  color: #bfbfbf;
}

.card-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  font-size: 24rpx;
  color: #595959;
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

.empty-btn {
  margin-top: 24rpx;
  background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%);
  color: #ffffff;
  font-size: 26rpx;
  padding: 10rpx 32rpx;
  border-radius: 30rpx;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 40rpx;
}

.modal-card {
  width: 100%;
  max-width: 650rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.close-icon {
  font-size: 32rpx;
  color: #8c8c8c;
  padding: 8rpx;
}

.modal-scroll-body {
  padding: 32rpx;
  max-height: 60vh;
}

.form-group {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 10rpx;

  &.required::before {
    content: '* ';
    color: #ff4d4f;
  }
}

.field-hint {
  font-size: 20rpx;
  color: #faad14;
  margin-top: 6rpx;
  display: block;
}

.form-input, .picker-input {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #262626;
  display: flex;
  justify-content: space-between;
  align-items: center;

  &.disabled {
    background-color: #f0f0f0;
    color: #8c8c8c;
  }
}

.options-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.option-pill {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background-color: #f5f5f5;
  border-radius: 30rpx;
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  color: #595959;

  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
    font-weight: bold;
    border: 1rpx solid #91caff;
  }
}

.form-textarea-diary {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  width: 100%;
  height: 260rpx;
  box-sizing: border-box;
  line-height: 1.6;
}

.modal-footer {
  display: flex;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx;
}

.btn-cancel {
  flex: 1;
  background-color: #f5f5f5;
  color: #595959;
  font-size: 28rpx;
  border-radius: 30rpx;
}

.btn-confirm {
  flex: 1;
  background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%);
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 30rpx;
}

.text-primary { color: #1677ff; }
.text-success { color: #52c41a; }
.text-danger { color: #ff4d4f; }
</style>

