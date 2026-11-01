<template>
  <view class="items-page">
    <!-- 顶部汇总统计卡片 -->
    <view class="life-card summary-card">
      <view class="summary-header">
        <text class="summary-title">📦 个人物品与资产</text>
        <button class="add-btn" @tap="openCreateModal">+ 新增物品</button>
      </view>

      <view class="summary-grid">
        <view class="stat-item">
          <text class="stat-val">{{ summary.totalItemCount }}</text>
          <text class="stat-label">物品总数</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-val text-success">{{ summary.totalItemCount - summary.lentItemCount }}</text>
          <text class="stat-label">在库可用</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-val text-primary">{{ summary.lentItemCount }}</text>
          <text class="stat-label">已借出</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-val" :class="summary.overdueLendingCount > 0 ? 'text-danger' : ''">
            {{ summary.overdueLendingCount }}
          </text>
          <text class="stat-label">已逾期</text>
        </view>
      </view>
    </view>

    <!-- 筛选与搜索工具栏 -->
    <view class="life-card filter-card">
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

      <view class="search-box">
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索物品名称 / 存放位置 / 备注"
          confirm-type="search"
          @confirm="onSearch"
        />
        <button v-if="searchKeyword" class="search-clear-btn" @tap="clearSearch">✕</button>
        <button class="search-submit-btn" @tap="onSearch">搜索</button>
      </view>
    </view>

    <!-- 物品列表 -->
    <view class="item-list-container">
      <view v-if="loading && items.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>

      <view v-else-if="items.length === 0" class="empty-state">
        <text class="empty-icon">📦</text>
        <text class="empty-text">暂无物品记录</text>
        <button class="empty-btn" @tap="openCreateModal">立即添加第一件物品</button>
      </view>

      <view
        v-for="item in items"
        :key="item.id"
        class="life-card item-card"
        @tap="viewItemDetail(item)"
      >
        <view class="item-header">
          <view class="item-title-box">
            <text class="item-name">{{ item.itemName }}</text>
            <text v-if="item.category" class="item-category-tag">{{ item.category }}</text>
          </view>
          
          <view class="item-status-box">
            <text v-if="item.status === 'ARCHIVED'" class="badge badge-archived">已归档</text>
            <text v-else-if="item.isOverdue" class="badge badge-danger">已逾期</text>
            <text v-else-if="item.isLent" class="badge badge-warning">借出中</text>
            <text v-else class="badge badge-success">在库</text>
          </view>
        </view>

        <!-- 物品属性 -->
        <view class="item-meta-grid">
          <view class="meta-row">
            <text class="meta-label">数量：</text>
            <text class="meta-val">{{ item.quantity }} {{ item.unit || '件' }}</text>
          </view>
          <view v-if="item.storageLocation" class="meta-row">
            <text class="meta-label">位置：</text>
            <text class="meta-val highlight-location">📍 {{ item.storageLocation }}</text>
          </view>
          <view v-if="item.purchaseDate" class="meta-row">
            <text class="meta-label">购入：</text>
            <text class="meta-val">{{ item.purchaseDate }}</text>
          </view>
        </view>

        <!-- 当前借还状态提示 -->
        <view v-if="item.isLent" class="lent-alert-box" :class="{ 'is-overdue': item.isOverdue }">
          <view class="lent-info">
            <text class="borrower-title">借给：{{ item.currentBorrower }}</text>
            <text class="lent-date-text">借出：{{ item.currentLentAt?.substring(0, 10) }}</text>
            <text v-if="item.currentExpectedReturnAt" class="due-date-text">
              应还：{{ item.currentExpectedReturnAt.substring(0, 10) }}
            </text>
          </view>
        </view>

        <!-- 快捷操作栏 -->
        <view class="card-action-bar" @tap.stop>
          <template v-if="!item.isLent">
            <button class="action-btn btn-primary" @tap="openLendModal(item)">借出</button>
            <button class="action-btn btn-default" @tap="openEditModal(item)">编辑</button>
            <button class="action-btn btn-default" @tap="handleToggleArchive(item)">
              {{ item.status === 'ACTIVE' ? '归档' : '恢复' }}
            </button>
            <button class="action-btn btn-danger-text" @tap="handleDeleteItem(item)">删除</button>
          </template>
          <template v-else>
            <button class="action-btn btn-success" @tap="openReturnModal(item)">登记归还</button>
            <button class="action-btn btn-default" @tap="viewItemDetail(item)">借还记录</button>
          </template>
        </view>
      </view>
    </view>

    <!-- 新增 / 编辑物品弹窗 -->
    <view v-if="itemModalVisible" class="modal-overlay" @tap.self="closeItemModal">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">{{ isEditingItem ? '编辑物品' : '新增物品' }}</text>
          <text class="close-icon" @tap="closeItemModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-group">
            <text class="form-label required">物品名称</text>
            <input v-model="itemForm.itemName" class="form-input" placeholder="请输入物品名称，如：戴森吹风机" />
          </view>

          <view class="form-group">
            <text class="form-label">物品分类</text>
            <view class="category-presets">
              <view
                v-for="cat in categoryPresets"
                :key="cat"
                class="preset-chip"
                :class="{ active: itemForm.category === cat }"
                @tap="itemForm.category = cat"
              >
                {{ cat }}
              </view>
            </view>
            <input v-model="itemForm.category" class="form-input mt-8" placeholder="或自定义分类" />
          </view>

          <view class="form-row">
            <view class="form-group flex-1 mr-12">
              <text class="form-label">数量</text>
              <input v-model.number="itemForm.quantity" type="number" class="form-input" placeholder="1" />
            </view>
            <view class="form-group flex-1">
              <text class="form-label">单位</text>
              <input v-model="itemForm.unit" class="form-input" placeholder="件/个/本" />
            </view>
          </view>

          <view class="form-group">
            <text class="form-label">存放位置</text>
            <input v-model="itemForm.storageLocation" class="form-input" placeholder="如：书房B柜三层 / 卧室抽屉" />
          </view>

          <view class="form-group">
            <text class="form-label">购入日期</text>
            <picker mode="date" :value="itemForm.purchaseDate" @change="onPurchaseDateChange">
              <view class="picker-input">
                <text :class="itemForm.purchaseDate ? '' : 'placeholder-text'">
                  {{ itemForm.purchaseDate || '请选择购买日期' }}
                </text>
                <text class="picker-arrow">📅</text>
              </view>
            </picker>
          </view>

          <view class="form-group">
            <text class="form-label">备注说明</text>
            <textarea
              v-model="itemForm.remark"
              class="form-textarea"
              placeholder="规格、保修信息、配件清单等..."
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn-cancel" @tap="closeItemModal">取消</button>
          <button class="btn-confirm" @tap="submitItemForm">保存</button>
        </view>
      </view>
    </view>

    <!-- 借出弹窗 -->
    <view v-if="lendModalVisible" class="modal-overlay" @tap.self="closeLendModal">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">登记借出物品</text>
          <text class="close-icon" @tap="closeLendModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="lend-target-hint">
            <text class="hint-label">借出物品：</text>
            <text class="hint-val">{{ currentTargetItem?.itemName }}</text>
          </view>

          <view class="form-group">
            <text class="form-label required">借用人姓名 / 称呼</text>
            <input v-model="lendForm.borrowerName" class="form-input" placeholder="请输入借用人姓名或同事称呼" />
          </view>

          <view class="form-group">
            <text class="form-label">借出日期</text>
            <picker mode="date" :value="lendForm.lentAt" @change="onLendDateChange">
              <view class="picker-input">
                <text>{{ lendForm.lentAt }}</text>
                <text class="picker-arrow">📅</text>
              </view>
            </picker>
          </view>

          <view class="form-group">
            <text class="form-label">预计归还日期（可选）</text>
            <picker mode="date" :value="lendForm.expectedReturnAt" @change="onExpectedReturnDateChange">
              <view class="picker-input">
                <text :class="lendForm.expectedReturnAt ? '' : 'placeholder-text'">
                  {{ lendForm.expectedReturnAt || '请选择预计归还日期' }}
                </text>
                <text class="picker-arrow">📅</text>
              </view>
            </picker>
          </view>

          <view class="form-group">
            <text class="form-label">借出备注</text>
            <textarea
              v-model="lendForm.remark"
              class="form-textarea"
              placeholder="记录借出用途、借出时状态、配件等"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn-cancel" @tap="closeLendModal">取消</button>
          <button class="btn-confirm" @tap="submitLendForm">确认借出</button>
        </view>
      </view>
    </view>

    <!-- 归还弹窗 -->
    <view v-if="returnModalVisible" class="modal-overlay" @tap.self="closeReturnModal">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">登记归还物品</text>
          <text class="close-icon" @tap="closeReturnModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="lend-target-hint">
            <text class="hint-label">归还物品：</text>
            <text class="hint-val">{{ currentTargetItem?.itemName }}</text>
          </view>
          <view class="lend-target-hint mt-8">
            <text class="hint-label">当前借用人：</text>
            <text class="hint-val">{{ currentTargetItem?.currentBorrower }}</text>
          </view>

          <view class="form-group mt-16">
            <text class="form-label">归还日期</text>
            <picker mode="date" :value="returnForm.returnedAt" @change="onReturnDateChange">
              <view class="picker-input">
                <text>{{ returnForm.returnedAt }}</text>
                <text class="picker-arrow">📅</text>
              </view>
            </picker>
          </view>

          <view class="form-group">
            <text class="form-label">归还备注</text>
            <textarea
              v-model="returnForm.remark"
              class="form-textarea"
              placeholder="记录物品归还验收状况等（可选）"
            />
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn-cancel" @tap="closeReturnModal">取消</button>
          <button class="btn-confirm" @tap="submitReturnForm">确认归还</button>
        </view>
      </view>
    </view>

    <!-- 物品详情与借还历史抽屉/弹窗 -->
    <view v-if="detailModalVisible" class="modal-overlay" @tap.self="closeDetailModal">
      <view class="modal-card detail-modal">
        <view class="modal-header">
          <text class="modal-title">物品详情与借还历史</text>
          <text class="close-icon" @tap="closeDetailModal">✕</text>
        </view>

        <scroll-view scroll-y class="detail-scroll-body">
          <view v-if="itemDetail" class="detail-content">
            <view class="detail-header-card">
              <text class="detail-item-title">{{ itemDetail.item.itemName }}</text>
              <text class="detail-item-cat">{{ itemDetail.item.category || '未分类' }}</text>
            </view>

            <view class="detail-fields-list">
              <view class="detail-row">
                <text class="field-label">存放位置：</text>
                <text class="field-val">{{ itemDetail.item.storageLocation || '未记录' }}</text>
              </view>
              <view class="detail-row">
                <text class="field-label">数量规格：</text>
                <text class="field-val">{{ itemDetail.item.quantity }} {{ itemDetail.item.unit || '件' }}</text>
              </view>
              <view class="detail-row">
                <text class="field-label">购买日期：</text>
                <text class="field-val">{{ itemDetail.item.purchaseDate || '未记录' }}</text>
              </view>
              <view class="detail-row">
                <text class="field-label">当前状态：</text>
                <text class="field-val font-bold">
                  {{ itemDetail.item.isLent ? '借出中' : '在库正常' }}
                  {{ itemDetail.item.status === 'ARCHIVED' ? '（已归档）' : '' }}
                </text>
              </view>
              <view v-if="itemDetail.item.remark" class="detail-row">
                <text class="field-label">备注：</text>
                <text class="field-val">{{ itemDetail.item.remark }}</text>
              </view>
            </view>

            <!-- 借还历史时间轴 -->
            <view class="history-section">
              <text class="history-title">📜 完整借还记录 ({{ itemDetail.lendingHistory.length }} 条)</text>

              <view v-if="itemDetail.lendingHistory.length === 0" class="history-empty">
                <text>暂无任何借还记录</text>
              </view>

              <view
                v-for="record in itemDetail.lendingHistory"
                :key="record.id"
                class="history-card"
              >
                <view class="history-header">
                  <text class="borrower-badge">借用人：{{ record.borrowerName }}</text>
                  <text
                    class="badge"
                    :class="record.status === 'RETURNED' ? 'badge-success' : record.isOverdue ? 'badge-danger' : 'badge-warning'"
                  >
                    {{ record.statusDesc }}
                  </text>
                </view>

                <view class="history-dates">
                  <text class="date-row">借出时间：{{ record.lentAt }}</text>
                  <text v-if="record.expectedReturnAt" class="date-row">应还时间：{{ record.expectedReturnAt }}</text>
                  <text v-if="record.returnedAt" class="date-row text-success">归还时间：{{ record.returnedAt }}</text>
                </view>

                <view v-if="record.remark" class="history-remark">
                  <text>备注：{{ record.remark }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn-confirm w-full" @tap="closeDetailModal">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getItemsApi,
  getItemDetailApi,
  createItemApi,
  updateItemApi,
  deleteItemApi,
  toggleArchiveItemApi,
  lendItemApi,
  returnItemApi,
  getLifeSummaryApi
} from '../../api/life'
import type { PersonalItem, ItemDetail, LifeRecordSummary } from '../../types/life'

const categoryPresets = ['数码电子', '书籍资料', '生活日用', '工具设备', '重要证件', '运动户外']

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '在库', value: 'IN_STOCK' },
  { label: '借出中', value: 'LENT' },
  { label: '已归档', value: 'ARCHIVED' }
]

const currentTab = ref('ALL')
const searchKeyword = ref('')
const loading = ref(false)

const summary = ref<LifeRecordSummary>({
  totalItemCount: 0,
  lentItemCount: 0,
  overdueLendingCount: 0,
  totalMemoCount: 0,
  pinnedMemoCount: 0,
  totalDiaryCount: 0,
  thisMonthDiaryCount: 0
})

const items = ref<PersonalItem[]>([])

const loadSummary = async () => {
  try {
    const res = await getLifeSummaryApi()
    if (res.data) {
      summary.value = res.data
    }
  } catch (err) {
    console.error('加载生活汇总统计失败', err)
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const params: {
      keyword?: string
      status?: string
      isLent?: boolean
      current?: number
      size?: number
    } = {
      current: 1,
      size: 100
    }

    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    if (currentTab.value === 'IN_STOCK') {
      params.status = 'ACTIVE'
      params.isLent = false
    } else if (currentTab.value === 'LENT') {
      params.status = 'ACTIVE'
      params.isLent = true
    } else if (currentTab.value === 'ARCHIVED') {
      params.status = 'ARCHIVED'
    }

    const res = await getItemsApi(params)
    if (res.data && res.data.records) {
      items.value = res.data.records
    } else {
      items.value = []
    }
  } catch (err) {
    uni.showToast({ title: '加载物品列表失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const switchTab = (tabVal: string) => {
  currentTab.value = tabVal
  loadItems()
}

const onSearch = () => {
  loadItems()
}

const clearSearch = () => {
  searchKeyword.value = ''
  loadItems()
}

// ================= 物品表单 =================
const itemModalVisible = ref(false)
const isEditingItem = ref(false)
const editingItemId = ref<number | null>(null)

const itemForm = ref({
  itemName: '',
  category: '',
  quantity: 1,
  unit: '件',
  storageLocation: '',
  purchaseDate: '',
  remark: ''
})

const openCreateModal = () => {
  isEditingItem.value = false
  editingItemId.value = null
  itemForm.value = {
    itemName: '',
    category: '',
    quantity: 1,
    unit: '件',
    storageLocation: '',
    purchaseDate: '',
    remark: ''
  }
  itemModalVisible.value = true
}

const openEditModal = (item: PersonalItem) => {
  isEditingItem.value = true
  editingItemId.value = item.id
  itemForm.value = {
    itemName: item.itemName,
    category: item.category || '',
    quantity: item.quantity,
    unit: item.unit || '件',
    storageLocation: item.storageLocation || '',
    purchaseDate: item.purchaseDate || '',
    remark: item.remark || ''
  }
  itemModalVisible.value = true
}

const closeItemModal = () => {
  itemModalVisible.value = false
}

const onPurchaseDateChange = (e: any) => {
  itemForm.value.purchaseDate = e.detail.value
}

const submitItemForm = async () => {
  if (!itemForm.value.itemName.trim()) {
    uni.showToast({ title: '请输入物品名称', icon: 'none' })
    return
  }

  try {
    const payload = {
      itemName: itemForm.value.itemName.trim(),
      category: itemForm.value.category.trim() || undefined,
      quantity: Number(itemForm.value.quantity) || 1,
      unit: itemForm.value.unit.trim() || undefined,
      storageLocation: itemForm.value.storageLocation.trim() || undefined,
      purchaseDate: itemForm.value.purchaseDate || undefined,
      remark: itemForm.value.remark.trim() || undefined
    }

    if (isEditingItem.value && editingItemId.value) {
      await updateItemApi(editingItemId.value, payload)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await createItemApi(payload)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }

    closeItemModal()
    loadItems()
    loadSummary()
  } catch (err: any) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const handleToggleArchive = async (item: PersonalItem) => {
  try {
    await toggleArchiveItemApi(item.id)
    uni.showToast({
      title: item.status === 'ACTIVE' ? '已归档' : '已恢复',
      icon: 'success'
    })
    loadItems()
    loadSummary()
  } catch (err: any) {
    uni.showToast({ title: err.message || '操作失败', icon: 'none' })
  }
}

const handleDeleteItem = (item: PersonalItem) => {
  if (item.isLent) {
    uni.showToast({ title: '物品借出中不能删除，请先登记归还', icon: 'none' })
    return
  }

  uni.showModal({
    title: '删除确认',
    content: `确定删除物品「${item.itemName}」吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteItemApi(item.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadItems()
          loadSummary()
        } catch (err: any) {
          uni.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// ================= 借出表单 =================
const lendModalVisible = ref(false)
const currentTargetItem = ref<PersonalItem | null>(null)
const lendForm = ref({
  borrowerName: '',
  lentAt: '',
  expectedReturnAt: '',
  remark: ''
})

const getTodayString = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const date = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${date}`
}

const openLendModal = (item: PersonalItem) => {
  currentTargetItem.value = item
  lendForm.value = {
    borrowerName: '',
    lentAt: getTodayString(),
    expectedReturnAt: '',
    remark: ''
  }
  lendModalVisible.value = true
}

const closeLendModal = () => {
  lendModalVisible.value = false
}

const onLendDateChange = (e: any) => {
  lendForm.value.lentAt = e.detail.value
}

const onExpectedReturnDateChange = (e: any) => {
  lendForm.value.expectedReturnAt = e.detail.value
}

const submitLendForm = async () => {
  if (!currentTargetItem.value) return
  if (!lendForm.value.borrowerName.trim()) {
    uni.showToast({ title: '请输入借用人姓名', icon: 'none' })
    return
  }

  try {
    await lendItemApi(currentTargetItem.value.id, {
      borrowerName: lendForm.value.borrowerName.trim(),
      lentAt: lendForm.value.lentAt ? `${lendForm.value.lentAt} 09:00:00` : undefined,
      expectedReturnAt: lendForm.value.expectedReturnAt ? `${lendForm.value.expectedReturnAt} 18:00:00` : undefined,
      remark: lendForm.value.remark.trim() || undefined
    })

    uni.showToast({ title: '借出登记成功', icon: 'success' })
    closeLendModal()
    loadItems()
    loadSummary()
  } catch (err: any) {
    uni.showToast({ title: err.message || '借出登记失败', icon: 'none' })
  }
}

// ================= 归还表单 =================
const returnModalVisible = ref(false)
const returnForm = ref({
  returnedAt: '',
  remark: ''
})

const openReturnModal = (item: PersonalItem) => {
  currentTargetItem.value = item
  returnForm.value = {
    returnedAt: getTodayString(),
    remark: ''
  }
  returnModalVisible.value = true
}

const closeReturnModal = () => {
  returnModalVisible.value = false
}

const onReturnDateChange = (e: any) => {
  returnForm.value.returnedAt = e.detail.value
}

const submitReturnForm = async () => {
  if (!currentTargetItem.value || !currentTargetItem.value.currentLendingId) return

  try {
    await returnItemApi(currentTargetItem.value.currentLendingId, {
      returnedAt: returnForm.value.returnedAt ? `${returnForm.value.returnedAt} 18:00:00` : undefined,
      remark: returnForm.value.remark.trim() || undefined
    })

    uni.showToast({ title: '归还登记成功', icon: 'success' })
    closeReturnModal()
    loadItems()
    loadSummary()
  } catch (err: any) {
    uni.showToast({ title: err.message || '归还登记失败', icon: 'none' })
  }
}

// ================= 物品详情与借还历史 =================
const detailModalVisible = ref(false)
const itemDetail = ref<ItemDetail | null>(null)

const viewItemDetail = async (item: PersonalItem) => {
  try {
    const res = await getItemDetailApi(item.id)
    if (res.data) {
      itemDetail.value = res.data
      detailModalVisible.value = true
    }
  } catch (err: any) {
    uni.showToast({ title: '获取详情失败', icon: 'none' })
  }
}

const closeDetailModal = () => {
  detailModalVisible.value = false
}

onMounted(() => {
  loadSummary()
  loadItems()
})
</script>

<style lang="scss" scoped>
.items-page {
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

.summary-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.add-btn {
  background-color: #1677ff;
  color: #ffffff;
  font-size: 26rpx;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  line-height: 1.6;
}

.summary-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 16rpx 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-val {
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
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.filter-tabs {
  display: flex;
  border-bottom: 1rpx solid #f0f0f0;
  padding-bottom: 16rpx;
  margin-bottom: 16rpx;
}

.tab-item {
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

.search-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
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

.item-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.item-title-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

.item-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.item-category-tag {
  font-size: 20rpx;
  background-color: #e6f4ff;
  color: #1677ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.badge {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.badge-success {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1rpx solid #b7eb8f;
}

.badge-warning {
  background-color: #fffbe6;
  color: #faad14;
  border: 1rpx solid #ffe58f;
}

.badge-danger {
  background-color: #fff2f0;
  color: #ff4d4f;
  border: 1rpx solid #ffccc7;
}

.badge-archived {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.item-meta-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 24rpx;
  color: #595959;
  margin-bottom: 16rpx;
}

.meta-row {
  display: flex;
  align-items: center;
}

.highlight-location {
  color: #1677ff;
  font-weight: 500;
}

.lent-alert-box {
  background-color: #fffbe6;
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  margin-bottom: 16rpx;
  border-left: 6rpx solid #faad14;

  &.is-overdue {
    background-color: #fff2f0;
    border-left-color: #ff4d4f;
  }
}

.lent-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 24rpx;
  color: #8c6b00;
}

.due-date-text {
  color: #ff4d4f;
  font-weight: 500;
}

.card-action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 16rpx;
}

.action-btn {
  font-size: 24rpx;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  line-height: 1.6;
}

.btn-primary {
  background-color: #1677ff;
  color: #ffffff;
}

.btn-success {
  background-color: #52c41a;
  color: #ffffff;
}

.btn-default {
  background-color: #f5f5f5;
  color: #595959;
}

.btn-danger-text {
  background: none;
  color: #ff4d4f;
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

.modal-body {
  padding: 32rpx;
  overflow-y: auto;
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

.placeholder-text {
  color: #bfbfbf;
}

.category-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.preset-chip {
  font-size: 22rpx;
  background-color: #f5f5f5;
  color: #595959;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;

  &.active {
    background-color: #e6f4ff;
    color: #1677ff;
    font-weight: 500;
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

.mt-8 {
  margin-top: 8rpx;
}

.mt-16 {
  margin-top: 16rpx;
}

.form-textarea {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  width: 100%;
  height: 140rpx;
  box-sizing: border-box;
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

.w-full {
  width: 100%;
}

.lend-target-hint {
  font-size: 26rpx;
  color: #595959;
}

.hint-val {
  font-weight: bold;
  color: #1677ff;
}

/* 详情抽屉样式 */
.detail-modal {
  max-height: 80vh;
}

.detail-scroll-body {
  padding: 32rpx;
  max-height: 60vh;
}

.detail-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.detail-item-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.detail-item-cat {
  font-size: 24rpx;
  background-color: #e6f4ff;
  color: #1677ff;
  padding: 6rpx 16rpx;
  border-radius: 10rpx;
}

.detail-fields-list {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 30rpx;
}

.detail-row {
  display: flex;
  font-size: 26rpx;
  margin-bottom: 12rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.field-label {
  color: #8c8c8c;
  width: 150rpx;
}

.field-val {
  color: #262626;
  flex: 1;
}

.font-bold {
  font-weight: bold;
}

.history-section {
  margin-top: 20rpx;
}

.history-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #262626;
  margin-bottom: 16rpx;
  display: block;
}

.history-empty {
  font-size: 24rpx;
  color: #8c8c8c;
  text-align: center;
  padding: 20rpx 0;
}

.history-card {
  background-color: #ffffff;
  border: 1rpx solid #f0f0f0;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.borrower-badge {
  font-size: 26rpx;
  font-weight: bold;
  color: #1f1f1f;
}

.history-dates {
  font-size: 22rpx;
  color: #595959;
}

.date-row {
  display: block;
  margin-bottom: 4rpx;
}

.history-remark {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8c8c8c;
}

.text-primary { color: #1677ff; }
.text-success { color: #52c41a; }
.text-danger { color: #ff4d4f; }
</style>

