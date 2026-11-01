<template>
  <view class="memos-page">
    <!-- 顶部概览与新增操作 -->
    <view class="life-card summary-card">
      <view class="summary-header">
        <view class="summary-title-box">
          <text class="summary-title">📌 个人备忘录</text>
          <text class="summary-subtitle">随手记录灵感、重点、合同与关键信息</text>
        </view>
        <button class="add-btn" @tap="openCreateModal">+ 新备忘</button>
      </view>

      <view class="summary-stats">
        <view class="stat-badge">
          <text class="badge-num">{{ totalCount }}</text>
          <text class="badge-text">总备忘</text>
        </view>
        <view class="stat-badge highlight">
          <text class="badge-num">📌 {{ pinnedCount }}</text>
          <text class="badge-text">已置顶</text>
        </view>
      </view>
    </view>

    <!-- 搜索与状态过滤 -->
    <view class="life-card filter-card">
      <view class="search-bar">
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索备忘标题、内容或标签..."
          confirm-type="search"
          @confirm="onSearch"
        />
        <button v-if="searchKeyword" class="search-clear-btn" @tap="clearSearch">✕</button>
        <button class="search-submit-btn" @tap="onSearch">搜索</button>
      </view>

      <!-- 快捷标签筛选 -->
      <view v-if="popularTags.length > 0" class="tag-scroll-box">
        <view
          class="tag-chip"
          :class="{ active: selectedTag === '' }"
          @tap="selectTag('')"
        >
          全部标签
        </view>
        <view
          v-for="tag in popularTags"
          :key="tag"
          class="tag-chip"
          :class="{ active: selectedTag === tag }"
          @tap="selectTag(tag)"
        >
          # {{ tag }}
        </view>
      </view>

      <view class="status-tabs">
        <view
          class="status-tab"
          :class="{ active: currentStatus === 'ACTIVE' }"
          @tap="switchStatus('ACTIVE')"
        >
          <text>正常备忘</text>
        </view>
        <view
          class="status-tab"
          :class="{ active: currentStatus === 'ARCHIVED' }"
          @tap="switchStatus('ARCHIVED')"
        >
          <text>已归档</text>
        </view>
      </view>
    </view>

    <!-- 备忘录列表 -->
    <view class="memo-list-container">
      <view v-if="loading && memos.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>

      <view v-else-if="memos.length === 0" class="empty-state">
        <text class="empty-icon">📝</text>
        <text class="empty-text">暂无相关备忘录</text>
        <button class="empty-btn" @tap="openCreateModal">立即记录第一条灵感</button>
      </view>

      <view
        v-for="memo in memos"
        :key="memo.id"
        class="life-card memo-card"
        :class="{ 'is-pinned': memo.isPinned === 1 }"
      >
        <view class="memo-header">
          <view class="memo-title-box">
            <text v-if="memo.isPinned === 1" class="pin-indicator">📌</text>
            <text class="memo-title">{{ memo.title }}</text>
          </view>

          <view class="memo-quick-actions" @tap.stop>
            <button
              class="icon-action-btn"
              :class="{ 'active-pin': memo.isPinned === 1 }"
              @tap="handleTogglePin(memo)"
            >
              {{ memo.isPinned === 1 ? '已置顶' : '置顶' }}
            </button>
          </view>
        </view>

        <!-- 备忘内容 -->
        <view v-if="memo.content" class="memo-content-box">
          <text class="memo-content-text">{{ memo.content }}</text>
        </view>

        <!-- 标签列表 -->
        <view v-if="memo.tagList && memo.tagList.length > 0" class="memo-tags-row">
          <text
            v-for="t in memo.tagList"
            :key="t"
            class="memo-tag-item"
            @tap="selectTag(t)"
          >
            #{{ t }}
          </text>
        </view>

        <!-- 底部元信息与操作 -->
        <view class="memo-footer" @tap.stop>
          <text class="memo-time">{{ memo.updatedAt ? memo.updatedAt.substring(0, 16) : memo.createdAt?.substring(0, 16) }}</text>

          <view class="footer-actions">
            <button class="btn-text" @tap="openEditModal(memo)">编辑</button>
            <button class="btn-text" @tap="handleToggleArchive(memo)">
              {{ memo.status === 'ACTIVE' ? '归档' : '恢复' }}
            </button>
            <button class="btn-text text-danger" @tap="handleDeleteMemo(memo)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 新增 / 编辑备忘录弹窗 -->
    <view v-if="memoModalVisible" class="modal-overlay" @tap.self="closeMemoModal">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑备忘' : '新建备忘' }}</text>
          <text class="close-icon" @tap="closeMemoModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-group">
            <text class="form-label required">备忘标题</text>
            <input
              v-model="memoForm.title"
              class="form-input"
              placeholder="请输入清晰扼要的标题..."
            />
          </view>

          <view class="form-group">
            <text class="form-label">备忘详情</text>
            <textarea
              v-model="memoForm.content"
              class="form-textarea-large"
              placeholder="记录具体细节、步骤、合同约定、密码提示等..."
            />
          </view>

          <view class="form-group">
            <text class="form-label">标签（用逗号或空格分隔）</text>
            <input
              v-model="memoForm.tags"
              class="form-input"
              placeholder="例如：工作, 合同, 关键配置"
            />
          </view>

          <view class="form-row align-center justify-between mt-16">
            <text class="form-label mb-0">是否置顶</text>
            <switch
              :checked="memoForm.isPinned === 1"
              color="#1677ff"
              @change="onPinSwitchChange"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn-cancel" @tap="closeMemoModal">取消</button>
          <button class="btn-confirm" @tap="submitMemoForm">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getMemosApi,
  createMemoApi,
  updateMemoApi,
  deleteMemoApi,
  togglePinMemoApi,
  toggleArchiveMemoApi
} from '../../api/life'
import type { Memo } from '../../types/life'

const memos = ref<Memo[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedTag = ref('')
const currentStatus = ref<'ACTIVE' | 'ARCHIVED'>('ACTIVE')

const totalCount = ref(0)
const pinnedCount = computed(() => memos.value.filter((m) => m.isPinned === 1).length)

// 提取当前备忘录列表中的热门标签
const popularTags = computed(() => {
  const set = new Set<string>()
  memos.value.forEach((m) => {
    if (m.tagList) {
      m.tagList.forEach((t) => {
        if (t.trim()) set.add(t.trim())
      })
    }
  })
  return Array.from(set).slice(0, 10)
})

const loadMemos = async () => {
  loading.value = true
  try {
    const params: {
      keyword?: string
      tag?: string
      status?: string
      current?: number
      size?: number
    } = {
      status: currentStatus.value,
      current: 1,
      size: 100
    }

    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    if (selectedTag.value) {
      params.tag = selectedTag.value
    }

    const res = await getMemosApi(params)
    if (res.data && res.data.records) {
      memos.value = res.data.records
      totalCount.value = res.data.total
    } else {
      memos.value = []
      totalCount.value = 0
    }
  } catch (err) {
    uni.showToast({ title: '加载备忘录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  loadMemos()
}

const clearSearch = () => {
  searchKeyword.value = ''
  loadMemos()
}

const selectTag = (tag: string) => {
  if (selectedTag.value === tag) {
    selectedTag.value = ''
  } else {
    selectedTag.value = tag
  }
  loadMemos()
}

const switchStatus = (status: 'ACTIVE' | 'ARCHIVED') => {
  currentStatus.value = status
  loadMemos()
}

// ================= 表单与编辑 =================
const memoModalVisible = ref(false)
const isEditing = ref(false)
const editingMemoId = ref<number | null>(null)

const memoForm = ref({
  title: '',
  content: '',
  tags: '',
  isPinned: 0
})

const openCreateModal = () => {
  isEditing.value = false
  editingMemoId.value = null
  memoForm.value = {
    title: '',
    content: '',
    tags: '',
    isPinned: 0
  }
  memoModalVisible.value = true
}

const openEditModal = (memo: Memo) => {
  isEditing.value = true
  editingMemoId.value = memo.id
  memoForm.value = {
    title: memo.title,
    content: memo.content || '',
    tags: memo.tags || '',
    isPinned: memo.isPinned || 0
  }
  memoModalVisible.value = true
}

const closeMemoModal = () => {
  memoModalVisible.value = false
}

const onPinSwitchChange = (e: any) => {
  memoForm.value.isPinned = e.detail.value ? 1 : 0
}

const submitMemoForm = async () => {
  if (!memoForm.value.title.trim()) {
    uni.showToast({ title: '请输入备忘标题', icon: 'none' })
    return
  }

  try {
    const payload = {
      title: memoForm.value.title.trim(),
      content: memoForm.value.content.trim() || undefined,
      tags: memoForm.value.tags.replace(/，/g, ',').replace(/\s+/g, ',').trim() || undefined,
      isPinned: memoForm.value.isPinned
    }

    if (isEditing.value && editingMemoId.value) {
      await updateMemoApi(editingMemoId.value, payload)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await createMemoApi(payload)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }

    closeMemoModal()
    loadMemos()
  } catch (err: any) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const handleTogglePin = async (memo: Memo) => {
  try {
    const res = await togglePinMemoApi(memo.id)
    uni.showToast({
      title: res.data?.isPinned === 1 ? '已置顶' : '已取消置顶',
      icon: 'success'
    })
    loadMemos()
  } catch (err: any) {
    uni.showToast({ title: err.message || '置顶操作失败', icon: 'none' })
  }
}

const handleToggleArchive = async (memo: Memo) => {
  try {
    await toggleArchiveMemoApi(memo.id)
    uni.showToast({
      title: memo.status === 'ACTIVE' ? '已归档' : '已恢复',
      icon: 'success'
    })
    loadMemos()
  } catch (err: any) {
    uni.showToast({ title: err.message || '归档操作失败', icon: 'none' })
  }
}

const handleDeleteMemo = (memo: Memo) => {
  uni.showModal({
    title: '删除确认',
    content: `确定删除备忘「${memo.title}」吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteMemoApi(memo.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadMemos()
        } catch (err: any) {
          uni.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadMemos()
})
</script>

<style lang="scss" scoped>
.memos-page {
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
  margin-bottom: 16rpx;
}

.summary-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f1f1f;
  display: block;
}

.summary-subtitle {
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

.summary-stats {
  display: flex;
  gap: 20rpx;
}

.stat-badge {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 10rpx 20rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;

  &.highlight {
    background-color: #fffbe6;
  }
}

.badge-num {
  font-size: 28rpx;
  font-weight: bold;
  color: #262626;
}

.badge-text {
  font-size: 22rpx;
  color: #8c8c8c;
}

.filter-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.search-bar {
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

.search-clear-btn {
  font-size: 24rpx;
  color: #8c8c8c;
  background: none;
  padding: 0 10rpx;
}

.search-submit-btn {
  background-color: #f0f0f0;
  font-size: 24rpx;
  color: #262626;
  border-radius: 30rpx;
  padding: 10rpx 20rpx;
}

.tag-scroll-box {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag-chip {
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

.status-tabs {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 14rpx;
}

.status-tab {
  flex: 1;
  text-align: center;
  font-size: 26rpx;
  color: #8c8c8c;
  padding: 6rpx 0;

  &.active {
    color: #1677ff;
    font-weight: bold;
  }
}

.memo-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &.is-pinned {
    border-left: 8rpx solid #1677ff;
    background-color: #fcfeff;
  }
}

.memo-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14rpx;
}

.memo-title-box {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex: 1;
}

.pin-indicator {
  font-size: 28rpx;
}

.memo-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.icon-action-btn {
  font-size: 22rpx;
  background-color: #f5f5f5;
  color: #8c8c8c;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;

  &.active-pin {
    background-color: #fffbe6;
    color: #faad14;
    border: 1rpx solid #ffe58f;
  }
}

.memo-content-box {
  background-color: #fafafa;
  border-radius: 10rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

.memo-content-text {
  font-size: 26rpx;
  color: #595959;
  line-height: 1.6;
  white-space: pre-wrap;
}

.memo-tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.memo-tag-item {
  font-size: 20rpx;
  color: #1677ff;
  background-color: #e6f4ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.memo-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 12rpx;
}

.memo-time {
  font-size: 22rpx;
  color: #bfbfbf;
}

.footer-actions {
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

.modal-body {
  padding: 32rpx;
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

  &.mb-0 {
    margin-bottom: 0;
  }
}

.form-input {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #262626;
}

.form-textarea-large {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  width: 100%;
  height: 220rpx;
  box-sizing: border-box;
}

.form-row {
  display: flex;

  &.align-center {
    align-items: center;
  }

  &.justify-between {
    justify-content: space-between;
  }
}

.mt-16 {
  margin-top: 16rpx;
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
</style>

