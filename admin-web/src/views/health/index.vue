<template>
  <PageContainer title="健康管理" description="维护系统健康参考标准及全员健康自律打卡数据监控">
    <el-tabs v-model="activeTab" class="health-tabs" type="border-card">
      <!-- 选项卡 1：健康标准配置 (13_admin_health_standards.png) -->
      <el-tab-pane label="健康标准配置" name="standards">
        <el-card shadow="never" class="table-card">
          <div class="table-toolbar">
            <div class="search-left">
              <el-input
                v-model="standardKeyword"
                placeholder="搜索标准名称或指标键..."
                clearable
                prefix-icon="Search"
                style="width: 260px;"
              />
            </div>
            <div class="action-right">
              <el-button type="primary" icon="Plus" @click="openCreateStandardModal">
                新增标准
              </el-button>
              <el-button icon="Refresh" @click="fetchStandards">刷新</el-button>
            </div>
          </div>

          <el-table
            :data="filteredStandards"
            v-loading="loadingStandards"
            style="width: 100%"
            stripe
            class="standard-table"
          >
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="metricName" label="标准名称" min-width="140">
              <template #default="{ row }">
                <span class="font-medium text-slate-800">{{ row.metricName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="metricCode" label="标准键" min-width="140">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.metricCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="参考范围 / 标准值" min-width="150" align="center">
              <template #default="{ row }">
                <span class="font-bold text-blue-600">
                  <template v-if="row.minValue !== null && row.maxValue !== null">
                    {{ row.minValue }} ~ {{ row.maxValue }}
                  </template>
                  <template v-else-if="row.maxValue !== null">
                    ≤ {{ row.maxValue }}
                  </template>
                  <template v-else-if="row.minValue !== null">
                    ≥ {{ row.minValue }}
                  </template>
                  <template v-else>
                    --
                  </template>
                </span>
                <span class="ml-1 text-slate-500 text-xs">{{ row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" align="center" />
            <el-table-column prop="applicableScope" label="适用人群" width="110" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.applicableScope === 'GENERAL' ? 'success' : 'warning'">
                  {{ row.applicableScope === 'GENERAL' ? '通用人群' : row.applicableScope }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="生活指引说明" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '生效中' : '已停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" icon="Edit" @click="openEditStandardModal(row)">
                  修改
                </el-button>
                <el-popconfirm title="确定要删除该健康参考标准吗？" @confirm="handleDeleteStandard(row.id)">
                  <template #reference>
                    <el-button link type="danger" size="small" icon="Delete">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="standardsPage.page"
              v-model:page-size="standardsPage.size"
              :total="standardsPage.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchStandards"
              @current-change="fetchStandards"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 选项卡 2：全员健康打卡记录 (12_admin_health_records.png) -->
      <el-tab-pane label="健康打卡记录" name="records">
        <el-card shadow="never" class="table-card">
          <div class="table-toolbar">
            <el-form :inline="true" :model="recordQuery" class="filter-form">
              <el-form-item label="指标类型">
                <el-select v-model="recordQuery.metricCode" placeholder="全部指标" clearable style="width: 140px;">
                  <el-option label="步数 (STEPS)" value="STEPS" />
                  <el-option label="睡眠 (SLEEP_HOURS)" value="SLEEP_HOURS" />
                  <el-option label="饮水量 (WATER_ML)" value="WATER_ML" />
                  <el-option label="体重 (WEIGHT)" value="WEIGHT" />
                  <el-option label="饮食热量 (CALORIES)" value="CALORIES" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="recordDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 240px;"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="fetchRecords">查询</el-button>
                <el-button icon="Refresh" @click="resetRecordQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table
            :data="recordsList"
            v-loading="loadingRecords"
            style="width: 100%"
            stripe
            class="record-table"
          >
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="userId" label="用户 ID" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="info">UID: {{ row.userId }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="metricName" label="健康指标" width="120" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getMetricTagType(row.metricCode)">
                  {{ row.metricName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="测量数值" width="130" align="right">
              <template #default="{ row }">
                <span class="font-bold text-slate-800 text-base">{{ row.metricValue }}</span>
                <span class="ml-1 text-xs text-slate-500">{{ row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="standardStatus" label="参考评估" width="110" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getStandardStatusType(row.standardStatus)">
                  {{ formatStandardStatus(row.standardStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordTime" label="打卡时间" width="170" align="center" />
            <el-table-column prop="note" label="饮食说明 / 运动备注" min-width="180" show-overflow-tooltip />
            <el-table-column prop="source" label="来源" width="90" align="center">
              <template #default="{ row }">
                <span class="text-xs text-slate-400">{{ row.source || 'MANUAL' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center" fixed="right">
              <template #default="{ row }">
                <el-popconfirm title="确定删除该条健康打卡记录吗？" @confirm="handleDeleteRecord(row.id)">
                  <template #reference>
                    <el-button link type="danger" size="small" icon="Delete">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="recordsPage.page"
              v-model:page-size="recordsPage.size"
              :total="recordsPage.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchRecords"
              @current-change="fetchRecords"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增 / 编辑健康标准 Dialog -->
    <el-dialog
      v-model="standardDialogVisible"
      :title="isEditStandard ? '修改健康参考标准' : '新增健康参考标准'"
      width="540px"
      destroy-on-close
    >
      <el-form
        ref="standardFormRef"
        :model="standardForm"
        :rules="standardRules"
        label-width="110px"
        style="padding-right: 16px;"
      >
        <el-form-item label="指标键编码" prop="metricCode">
          <el-input
            v-model="standardForm.metricCode"
            placeholder="例如: STEPS, SLEEP_HOURS, WEIGHT"
            :disabled="isEditStandard"
          />
        </el-form-item>
        <el-form-item label="标准名称" prop="metricName">
          <el-input v-model="standardForm.metricName" placeholder="例如: 每日步数目标" />
        </el-form-item>
        <el-form-item label="数值单位" prop="unit">
          <el-input v-model="standardForm.unit" placeholder="例如: 步、小时、kg、ml" />
        </el-form-item>
        <el-form-item label="下限参考值" prop="minValue">
          <el-input-number v-model="standardForm.minValue" :precision="1" :step="1" placeholder="最小值" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="上限参考值" prop="maxValue">
          <el-input-number v-model="standardForm.maxValue" :precision="1" :step="1" placeholder="最大值" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="适用人群" prop="applicableScope">
          <el-select v-model="standardForm.applicableScope" style="width: 100%;">
            <el-option label="通用人群 (GENERAL)" value="GENERAL" />
            <el-option label="成年男性 (MALE)" value="MALE" />
            <el-option label="成年女性 (FEMALE)" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="参考说明" prop="description">
          <el-input
            v-model="standardForm.description"
            type="textarea"
            :rows="3"
            placeholder="输入健康生活指引描述（非医疗依据）"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-switch
            v-model="standardForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="standardDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingStandard" @click="submitStandardForm">
          保存
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'
import {
  getHealthStandards,
  createHealthStandard,
  updateHealthStandard,
  deleteHealthStandard,
  getHealthRecords,
  deleteHealthRecord,
  type HealthStandard,
  type HealthRecord
} from '../../api/health'

const activeTab = ref('standards')

// ========== 1. 健康标准配置 ==========
const loadingStandards = ref(false)
const standardsList = ref<HealthStandard[]>([])
const standardKeyword = ref('')
const standardsPage = reactive({ page: 1, size: 10, total: 0 })

const filteredStandards = computed(() => {
  const list = Array.isArray(standardsList.value) ? standardsList.value : []
  if (!standardKeyword.value.trim()) return list
  const kw = standardKeyword.value.toLowerCase()
  return list.filter(
    (s) => s.metricName.toLowerCase().includes(kw) || s.metricCode.toLowerCase().includes(kw)
  )
})

const fetchStandards = async () => {
  loadingStandards.value = true
  try {
    const res = await getHealthStandards({
      pageNum: standardsPage.page,
      pageSize: standardsPage.size
    })
    const list = Array.isArray(res.data?.records) ? res.data.records : (Array.isArray(res.data) ? res.data : [])
    standardsList.value = list
    standardsPage.total = res.data?.total || list.length
  } catch (err: any) {
    ElMessage.error(err.message || '获取健康标准失败')
    standardsList.value = []
  } finally {
    loadingStandards.value = false
  }
}

// 标准 Dialog
const standardDialogVisible = ref(false)
const isEditStandard = ref(false)
const savingStandard = ref(false)
const standardFormRef = ref<FormInstance>()
const currentStandardId = ref<number | null>(null)

const standardForm = reactive({
  metricCode: '',
  metricName: '',
  unit: '',
  minValue: undefined as number | undefined,
  maxValue: undefined as number | undefined,
  applicableScope: 'GENERAL',
  description: '',
  status: 1
})

const standardRules = {
  metricCode: [{ required: true, message: '请输入指标键编码', trigger: 'blur' }],
  metricName: [{ required: true, message: '请输入标准名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
}

const openCreateStandardModal = () => {
  isEditStandard.value = false
  currentStandardId.value = null
  Object.assign(standardForm, {
    metricCode: '',
    metricName: '',
    unit: '',
    minValue: undefined,
    maxValue: undefined,
    applicableScope: 'GENERAL',
    description: '',
    status: 1
  })
  standardDialogVisible.value = true
}

const openEditStandardModal = (row: HealthStandard) => {
  isEditStandard.value = true
  currentStandardId.value = row.id!
  Object.assign(standardForm, {
    metricCode: row.metricCode,
    metricName: row.metricName,
    unit: row.unit,
    minValue: row.minValue,
    maxValue: row.maxValue,
    applicableScope: row.applicableScope,
    description: row.description,
    status: row.status
  })
  standardDialogVisible.value = true
}

const submitStandardForm = async () => {
  if (!standardFormRef.value) return
  await standardFormRef.value.validate(async (valid) => {
    if (!valid) return
    savingStandard.value = true
    try {
      if (isEditStandard.value && currentStandardId.value) {
        await updateHealthStandard(currentStandardId.value, standardForm)
        ElMessage.success('修改健康标准成功')
      } else {
        await createHealthStandard(standardForm)
        ElMessage.success('创建健康标准成功')
      }
      standardDialogVisible.value = false
      fetchStandards()
    } catch (err: any) {
      ElMessage.error(err.message || '保存失败')
    } finally {
      savingStandard.value = false
    }
  })
}

const handleDeleteStandard = async (id: number) => {
  try {
    await deleteHealthStandard(id)
    ElMessage.success('删除成功')
    fetchStandards()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败')
  }
}

// ========== 2. 全员健康打卡记录 ==========
const loadingRecords = ref(false)
const recordsList = ref<HealthRecord[]>([])
const recordDateRange = ref<[string, string] | null>(null)
const recordQuery = reactive({
  metricCode: '',
  startDate: '',
  endDate: ''
})
const recordsPage = reactive({ page: 1, size: 10, total: 0 })

const fetchRecords = async () => {
  loadingRecords.value = true
  try {
    const [start, end] = recordDateRange.value || ['', '']
    const res = await getHealthRecords({
      pageNum: recordsPage.page,
      pageSize: recordsPage.size,
      metricCode: recordQuery.metricCode || undefined,
      startDate: start || undefined,
      endDate: end || undefined
    })
    const list = Array.isArray(res.data?.records) ? res.data.records : (Array.isArray(res.data) ? res.data : [])
    recordsList.value = list
    recordsPage.total = res.data?.total || list.length
  } catch (err: any) {
    ElMessage.error(err.message || '获取健康打卡记录失败')
    recordsList.value = []
  } finally {
    loadingRecords.value = false
  }
}

const resetRecordQuery = () => {
  recordQuery.metricCode = ''
  recordDateRange.value = null
  recordsPage.page = 1
  fetchRecords()
}

const handleDeleteRecord = async (id: number) => {
  try {
    await deleteHealthRecord(id)
    ElMessage.success('删除记录成功')
    fetchRecords()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败')
  }
}

const getMetricTagType = (code: string) => {
  switch (code) {
    case 'STEPS':
      return 'success'
    case 'SLEEP_HOURS':
      return 'primary'
    case 'WATER_ML':
      return 'info'
    case 'WEIGHT':
      return 'warning'
    case 'CALORIES':
      return 'danger'
    default:
      return 'info'
  }
}

const getStandardStatusType = (status: string) => {
  switch (status) {
    case 'NORMAL':
      return 'success'
    case 'BELOW':
      return 'warning'
    case 'ABOVE':
      return 'danger'
    default:
      return 'info'
  }
}

const formatStandardStatus = (status: string) => {
  switch (status) {
    case 'NORMAL':
      return '正常达标'
    case 'BELOW':
      return '偏低'
    case 'ABOVE':
      return '偏高'
    default:
      return '未评估'
  }
}

onMounted(() => {
  fetchStandards()
  fetchRecords()
})
</script>

<style scoped>
.health-tabs {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.table-card {
  border: none;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 0;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 8px 0;
}
</style>
