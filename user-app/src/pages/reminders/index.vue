<template>
  <view class="reminders-page">
    <!-- 顶部规则看板 -->
    <view class="life-card summary-card">
      <view class="summary-header">
        <view class="title-meta">
          <text class="page-title">⚙️ 提醒规则设置</text>
          <text class="page-subtitle">配置日程、习惯、目标与日常提醒策略</text>
        </view>
        <button class="add-btn" @tap="openCreateModal">+ 新增规则</button>
      </view>

      <view class="stats-row">
        <view class="stat-cell">
          <text class="stat-num text-primary">{{ activeCount }}</text>
          <text class="stat-label">启用中规则</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-cell">
          <text class="stat-num">{{ rules.length }}</text>
          <text class="stat-label">规则总数</text>
        </view>
      </view>
    </view>

    <!-- 触发类型筛选 -->
    <view class="life-card filter-card">
      <view class="type-chips">
        <view
          v-for="t in filterTypeOptions"
          :key="t.value"
          class="chip"
          :class="{ active: selectedTriggerType === t.value }"
          @tap="selectFilterType(t.value)"
        >
          {{ t.label }}
        </view>
      </view>
    </view>

    <!-- 规则列表 -->
    <view class="rule-list">
      <view v-if="loading && rules.length === 0" class="loading-state">
        <text>加载提醒规则...</text>
      </view>

      <view v-else-if="rules.length === 0" class="empty-state">
        <text class="empty-icon">🔔</text>
        <text class="empty-text">暂无提醒规则</text>
        <button class="empty-btn" @tap="openCreateModal">立即配置一条提醒规则</button>
      </view>

      <view
        v-for="rule in rules"
        :key="rule.id"
        class="life-card rule-card"
        :class="{ inactive: rule.status !== 1 }"
      >
        <view class="rule-header">
          <view class="rule-name-box">
            <text class="rule-name">{{ rule.ruleName }}</text>
            <text class="trigger-type-badge">{{ rule.triggerTypeDesc || rule.triggerType }}</text>
          </view>
          <switch
            :checked="rule.status === 1"
            color="#1677ff"
            @change="handleToggleStatus(rule)"
          />
        </view>

        <view class="rule-details-grid">
          <view v-if="rule.remindAt" class="detail-item">
            <text class="detail-label">提醒时刻：</text>
            <text class="detail-val">{{ rule.remindAt }}</text>
          </view>
          <view v-if="rule.advanceMinutes > 0" class="detail-item">
            <text class="detail-label">提前时间：</text>
            <text class="detail-val text-primary">{{ rule.advanceMinutes }} 分钟</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">重复周期：</text>
            <text class="detail-val">{{ rule.repeatRuleDesc || rule.repeatRule }}</text>
          </view>
          <view v-if="rule.startDate || rule.endDate" class="detail-item">
            <text class="detail-label">生效区间：</text>
            <text class="detail-val">{{ rule.startDate || '长期' }} ~ {{ rule.endDate || '长期' }}</text>
          </view>
        </view>

        <view class="rule-footer" @tap.stop>
          <text class="status-desc">{{ rule.status === 1 ? '🟢 生效中' : '⚪ 已停用' }}</text>

          <view class="actions-box">
            <button class="btn-text" @tap="openEditModal(rule)">编辑</button>
            <button class="btn-text text-danger" @tap="handleDeleteRule(rule)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 新增 / 编辑规则弹窗 -->
    <view v-if="ruleModalVisible" class="modal-overlay" @tap.self="closeRuleModal">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑提醒规则' : '新建提醒规则' }}</text>
          <text class="close-icon" @tap="closeRuleModal">✕</text>
        </view>

        <scroll-view scroll-y class="modal-scroll-body">
          <view class="form-group">
            <text class="form-label required">规则名称</text>
            <input
              v-model="ruleForm.ruleName"
              class="form-input"
              placeholder="如：早起打卡提醒、紧急待办截止前提醒"
            />
          </view>

          <view class="form-group">
            <text class="form-label required">触发场景</text>
            <view class="chips-selector">
              <view
                v-for="opt in formTypeOptions"
                :key="opt.value"
                class="selector-chip"
                :class="{ active: ruleForm.triggerType === opt.value }"
                @tap="ruleForm.triggerType = opt.value"
              >
                {{ opt.icon }} {{ opt.label }}
              </view>
            </view>
          </view>

          <!-- 提前提醒分钟数（待办 / 目标场景） -->
          <view v-if="ruleForm.triggerType === 'TODO_DUE' || ruleForm.triggerType === 'GOAL_DUE'" class="form-group">
            <text class="form-label">提前提醒时间</text>
            <view class="chips-selector">
              <view
                v-for="adv in advancePresets"
                :key="adv.value"
                class="selector-chip"
                :class="{ active: ruleForm.advanceMinutes === adv.value }"
                @tap="ruleForm.advanceMinutes = adv.value"
              >
                {{ adv.label }}
              </view>
            </view>
          </view>

          <!-- 提醒时刻（习惯 / 自定义场景） -->
          <view v-if="ruleForm.triggerType === 'HABIT_CHECKIN' || ruleForm.triggerType === 'CUSTOM'" class="form-group">
            <text class="form-label">提醒时刻 (每天几点)</text>
            <picker mode="time" :value="ruleForm.remindAt" @change="onTimeChange">
              <view class="picker-input">
                <text>{{ ruleForm.remindAt || '09:00' }}</text>
                <text class="picker-arrow">⏰</text>
              </view>
            </picker>
          </view>

          <!-- 重复频率 -->
          <view class="form-group">
            <text class="form-label">重复周期</text>
            <view class="chips-selector">
              <view
                v-for="rep in repeatOptions"
                :key="rep.value"
                class="selector-chip"
                :class="{ active: ruleForm.repeatRule === rep.value }"
                @tap="ruleForm.repeatRule = rep.value"
              >
                {{ rep.label }}
              </view>
            </view>
          </view>

          <!-- 生效日期范围 -->
          <view class="form-row">
            <view class="form-group flex-1 mr-12">
              <text class="form-label">开始日期（可选）</text>
              <picker mode="date" :value="ruleForm.startDate" @change="onStartDateChange">
                <view class="picker-input">
                  <text :class="ruleForm.startDate ? '' : 'text-placeholder'">
                    {{ ruleForm.startDate || '立即开始' }}
                  </text>
                </view>
              </picker>
            </view>
            <view class="form-group flex-1">
              <text class="form-label">结束日期（可选）</text>
              <picker mode="date" :value="ruleForm.endDate" @change="onEndDateChange">
                <view class="picker-input">
                  <text :class="ruleForm.endDate ? '' : 'text-placeholder'">
                    {{ ruleForm.endDate || '长期有效' }}
                  </text>
                </view>
              </picker>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn-cancel" @tap="closeRuleModal">取消</button>
          <button class="btn-confirm" @tap="submitRuleForm">保存规则</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getReminderRulesApi,
  createReminderRuleApi,
  updateReminderRuleApi,
  deleteReminderRuleApi,
  toggleReminderRuleStatusApi
} from '../../api/reminder'
import type { ReminderRule } from '../../types/reminder'

const filterTypeOptions = [
  { value: '', label: '全部规则' },
  { value: 'TODO_DUE', label: '待办截止' },
  { value: 'HABIT_CHECKIN', label: '习惯打卡' },
  { value: 'GOAL_DUE', label: '目标到期' },
  { value: 'CUSTOM', label: '自定义' }
]

const formTypeOptions = [
  { value: 'TODO_DUE', label: '待办截止', icon: '📋' },
  { value: 'HABIT_CHECKIN', label: '习惯打卡', icon: '🏃' },
  { value: 'GOAL_DUE', label: '目标到期', icon: '🎯' },
  { value: 'CUSTOM', label: '自定义提醒', icon: '🔔' }
]

const advancePresets = [
  { value: 0, label: '准时' },
  { value: 15, label: '提前15分钟' },
  { value: 30, label: '提前30分钟' },
  { value: 60, label: '提前1小时' },
  { value: 1440, label: '提前1天' }
]

const repeatOptions = [
  { value: 'DAILY', label: '每天' },
  { value: 'WORKDAY', label: '仅工作日' },
  { value: 'WEEKEND', label: '仅周末' },
  { value: 'ONCE', label: '仅单次' }
]

const rules = ref<ReminderRule[]>([])
const loading = ref(false)
const selectedTriggerType = ref('')

const activeCount = computed(() => rules.value.filter((r) => r.status === 1).length)

const loadRules = async () => {
  loading.value = true
  try {
    const params: {
      triggerType?: string
      current?: number
      size?: number
    } = {
      current: 1,
      size: 100
    }

    if (selectedTriggerType.value) {
      params.triggerType = selectedTriggerType.value
    }

    const res = await getReminderRulesApi(params)
    if (res.data && res.data.records) {
      rules.value = res.data.records
    } else {
      rules.value = []
    }
  } catch (err) {
    uni.showToast({ title: '加载规则失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const selectFilterType = (type: string) => {
  selectedTriggerType.value = type
  loadRules()
}

const handleToggleStatus = async (rule: ReminderRule) => {
  try {
    const res = await toggleReminderRuleStatusApi(rule.id)
    if (res.data) {
      rule.status = res.data.status
    }
    uni.showToast({
      title: rule.status === 1 ? '规则已启用' : '规则已停用',
      icon: 'none'
    })
  } catch (err: any) {
    uni.showToast({ title: err.message || '切换失败', icon: 'none' })
  }
}

// ================= 表单与新增/编辑 =================
const ruleModalVisible = ref(false)
const isEditing = ref(false)
const editingRuleId = ref<number | null>(null)

const ruleForm = ref({
  ruleName: '',
  triggerType: 'TODO_DUE',
  remindAt: '09:00',
  advanceMinutes: 15,
  repeatRule: 'DAILY',
  startDate: '',
  endDate: ''
})

const openCreateModal = () => {
  isEditing.value = false
  editingRuleId.value = null
  ruleForm.value = {
    ruleName: '',
    triggerType: 'TODO_DUE',
    remindAt: '09:00',
    advanceMinutes: 15,
    repeatRule: 'DAILY',
    startDate: '',
    endDate: ''
  }
  ruleModalVisible.value = true
}

const openEditModal = (rule: ReminderRule) => {
  isEditing.value = true
  editingRuleId.value = rule.id
  ruleForm.value = {
    ruleName: rule.ruleName,
    triggerType: rule.triggerType,
    remindAt: rule.remindAt ? rule.remindAt.substring(0, 5) : '09:00',
    advanceMinutes: rule.advanceMinutes || 0,
    repeatRule: rule.repeatRule || 'DAILY',
    startDate: rule.startDate || '',
    endDate: rule.endDate || ''
  }
  ruleModalVisible.value = true
}

const closeRuleModal = () => {
  ruleModalVisible.value = false
}

const onTimeChange = (e: any) => {
  ruleForm.value.remindAt = e.detail.value
}

const onStartDateChange = (e: any) => {
  ruleForm.value.startDate = e.detail.value
}

const onEndDateChange = (e: any) => {
  ruleForm.value.endDate = e.detail.value
}

const submitRuleForm = async () => {
  if (!ruleForm.value.ruleName.trim()) {
    uni.showToast({ title: '请输入规则名称', icon: 'none' })
    return
  }

  try {
    const payload = {
      ruleName: ruleForm.value.ruleName.trim(),
      triggerType: ruleForm.value.triggerType,
      remindAt: ruleForm.value.remindAt ? `${ruleForm.value.remindAt}:00` : undefined,
      advanceMinutes: Number(ruleForm.value.advanceMinutes) || 0,
      repeatRule: ruleForm.value.repeatRule,
      startDate: ruleForm.value.startDate || undefined,
      endDate: ruleForm.value.endDate || undefined,
      status: 1
    }

    if (isEditing.value && editingRuleId.value) {
      await updateReminderRuleApi(editingRuleId.value, payload)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await createReminderRuleApi(payload)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }

    closeRuleModal()
    loadRules()
  } catch (err: any) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const handleDeleteRule = (rule: ReminderRule) => {
  uni.showModal({
    title: '删除规则',
    content: `确定删除提醒规则「${rule.ruleName}」吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteReminderRuleApi(rule.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadRules()
        } catch (err: any) {
          uni.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadRules()
})
</script>

<style lang="scss" scoped>
.reminders-page {
  padding: 24rpx;
  background-color: #f7f8fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.summary-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.summary-header {
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

.add-btn {
  background-color: #1677ff;
  color: #ffffff;
  font-size: 26rpx;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  line-height: 1.6;
}

.stats-row {
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
}

.stat-num {
  font-size: 36rpx;
  font-weight: bold;
  color: #262626;
}

.stat-label {
  font-size: 22rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
}

.stat-divider {
  width: 1rpx;
  height: 40rpx;
  background-color: #f0f0f0;
}

.filter-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 16rpx;
  margin-bottom: 24rpx;
}

.type-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.chip {
  font-size: 24rpx;
  background-color: #f5f5f5;
  color: #595959;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;

  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
    font-weight: bold;
  }
}

.rule-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &.inactive {
    opacity: 0.65;
  }
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.rule-name-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

.rule-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.trigger-type-badge {
  font-size: 20rpx;
  background-color: #e6f4ff;
  color: #1677ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.rule-details-grid {
  background-color: #fafafa;
  border-radius: 10rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

.detail-item {
  display: flex;
  font-size: 24rpx;
  margin-bottom: 8rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.detail-label {
  color: #8c8c8c;
  width: 150rpx;
}

.detail-val {
  color: #262626;
  flex: 1;
}

.rule-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 12rpx;
}

.status-desc {
  font-size: 22rpx;
  color: #8c8c8c;
}

.actions-box {
  display: flex;
  gap: 16rpx;
}

.btn-text {
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
  background-color: #1677ff;
  color: #ffffff;
  font-size: 26rpx;
  padding: 10rpx 32rpx;
  border-radius: 30rpx;
}

/* 弹窗通用样式 */
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

.form-input, .picker-input {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #262626;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chips-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.selector-chip {
  font-size: 24rpx;
  background-color: #f5f5f5;
  color: #595959;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;

  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
    font-weight: bold;
    border: 1rpx solid #91caff;
  }
}

.form-row {
  display: flex;
}

.flex-1 {
  flex: 1;
}

.mr-12 {
  margin-right: 12rpx;
}

.text-placeholder {
  color: #bfbfbf;
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
  background-color: #1677ff;
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 30rpx;
}

.text-primary {
  color: #1677ff;
}
</style>

