<template>
  <PageContainer title="联动规则" description="配置智能联动规则与生活习惯跨模块提醒策略">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索工具栏 (16_admin_reminder_rules.png) -->
      <div class="table-toolbar">
        <div class="search-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索规则名称..."
            clearable
            prefix-icon="Search"
            style="width: 260px;"
          />
          <el-select
            v-model="filterEnabled"
            placeholder="启用状态"
            clearable
            style="width: 130px;"
            @change="fetchRules"
          >
            <el-option label="全部状态" value="" />
            <el-option label="已启用" :value="true" />
            <el-option label="已停用" :value="false" />
          </el-select>
        </div>
        <div class="action-right">
          <el-button type="primary" icon="Plus" @click="openCreateModal">
            新增规则
          </el-button>
          <el-button icon="Refresh" @click="fetchRules">刷新</el-button>
        </div>
      </div>

      <!-- 规则表格 -->
      <el-table
        :data="filteredRules"
        v-loading="loading"
        style="width: 100%"
        stripe
        class="rule-table"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-800">{{ row.ruleName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ruleType" label="规则类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getRuleTypeTag(row.ruleType)">
              {{ formatRuleType(row.ruleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conditionValue" label="触发阈值" width="120" align="center">
          <template #default="{ row }">
            <span class="font-bold text-blue-600">{{ row.conditionValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="monitorWindowDays" label="监控周期" width="110" align="center">
          <template #default="{ row }">
            <span>{{ row.monitorWindowDays }} 天</span>
          </template>
        </el-table-column>
        <el-table-column prop="contentTemplate" label="提醒内容模版" min-width="220" show-overflow-tooltip />
        <el-table-column prop="enabled" label="是否启用" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleStatus(row)"
              active-text=""
              inactive-text=""
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" icon="Edit" @click="openEditModal(row)">
              修改
            </el-button>
            <el-popconfirm title="确定删除该智能联动规则吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.size"
          :total="pageParams.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchRules"
          @current-change="fetchRules"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑规则 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改联动规则' : '新增智能联动规则'"
      width="540px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        style="padding-right: 16px;"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="例如: 连续未运动提醒、待办超期提醒" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="formData.ruleType" style="width: 100%;">
            <el-option label="健康运动联动 (HEALTH_STEPS)" value="HEALTH_STEPS" />
            <el-option label="睡眠不足联动 (HEALTH_SLEEP)" value="HEALTH_SLEEP" />
            <el-option label="待办超期提醒 (TODO_OVERDUE)" value="TODO_OVERDUE" />
            <el-option label="习惯断签提醒 (HABIT_STREAK)" value="HABIT_STREAK" />
            <el-option label="月度支出超支 (FINANCE_BUDGET)" value="FINANCE_BUDGET" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件" prop="triggerCondition">
          <el-input v-model="formData.triggerCondition" placeholder="例如: STEPS_LESS_THAN, OVERDUE_HOURS" />
        </el-form-item>
        <el-form-item label="条件数值" prop="conditionValue">
          <el-input-number v-model="formData.conditionValue" :step="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="监控天数" prop="monitorWindowDays">
          <el-input-number v-model="formData.monitorWindowDays" :min="1" :max="365" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="提醒内容" prop="contentTemplate">
          <el-input
            v-model="formData.contentTemplate"
            type="textarea"
            :rows="3"
            placeholder="例如: 您已连续 {days} 天未达成步数目标，适量运动有益身心！"
          />
        </el-form-item>
        <el-form-item label="是否启用" prop="enabled">
          <el-switch v-model="formData.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import PageContainer from '../../components/PageContainer.vue'
import {
  getReminderRules,
  createReminderRule,
  updateReminderRule,
  toggleReminderRuleStatus,
  deleteReminderRule,
  type ReminderRule
} from '../../api/reminder'

const loading = ref(false)
const ruleList = ref<ReminderRule[]>([])
const searchKeyword = ref('')
const filterEnabled = ref<boolean | ''>('')
const pageParams = reactive({ page: 1, size: 10, total: 0 })

const filteredRules = computed(() => {
  let list = Array.isArray(ruleList.value) ? ruleList.value : []
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((r) => r.ruleName.toLowerCase().includes(kw))
  }
  return list
})

const fetchRules = async () => {
  loading.value = true
  try {
    const res = await getReminderRules({
      pageNum: pageParams.page,
      pageSize: pageParams.size,
      enabled: filterEnabled.value === '' ? undefined : (filterEnabled.value as boolean)
    })
    const list = Array.isArray(res.data?.records) ? res.data.records : (Array.isArray(res.data) ? res.data : [])
    ruleList.value = list
    pageParams.total = res.data?.total || list.length
  } catch (err: any) {
    ElMessage.error(err.message || '获取规则列表失败')
    ruleList.value = []
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = async (row: ReminderRule) => {
  try {
    await toggleReminderRuleStatus(row.id!)
    ElMessage.success(`规则「${row.ruleName}」状态已更新`)
  } catch (err: any) {
    row.enabled = !row.enabled
    ElMessage.error(err.message || '更新状态失败')
  }
}

// Dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const currentId = ref<number | null>(null)

const formData = reactive({
  ruleName: '',
  ruleType: 'HEALTH_STEPS',
  triggerCondition: 'LESS_THAN',
  conditionValue: 5000,
  monitorWindowDays: 3,
  contentTemplate: '',
  enabled: true
})

const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  contentTemplate: [{ required: true, message: '请输入提醒模板', trigger: 'blur' }]
}

const openCreateModal = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(formData, {
    ruleName: '',
    ruleType: 'HEALTH_STEPS',
    triggerCondition: 'LESS_THAN',
    conditionValue: 5000,
    monitorWindowDays: 3,
    contentTemplate: '您已连续多日未达成步数目标，适量运动有益健康！',
    enabled: true
  })
  dialogVisible.value = true
}

const openEditModal = (row: ReminderRule) => {
  isEdit.value = true
  currentId.value = row.id!
  Object.assign(formData, {
    ruleName: row.ruleName,
    ruleType: row.ruleType,
    triggerCondition: row.triggerCondition,
    conditionValue: row.conditionValue,
    monitorWindowDays: row.monitorWindowDays,
    contentTemplate: row.contentTemplate,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (isEdit.value && currentId.value) {
        await updateReminderRule(currentId.value, formData)
        ElMessage.success('修改规则成功')
      } else {
        await createReminderRule(formData)
        ElMessage.success('创建规则成功')
      }
      dialogVisible.value = false
      fetchRules()
    } catch (err: any) {
      ElMessage.error(err.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await deleteReminderRule(id)
    ElMessage.success('删除成功')
    fetchRules()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败')
  }
}

const getRuleTypeTag = (type: string) => {
  if (type.includes('HEALTH')) return 'success'
  if (type.includes('TODO')) return 'primary'
  if (type.includes('HABIT')) return 'warning'
  if (type.includes('FINANCE')) return 'danger'
  return 'info'
}

const formatRuleType = (type: string) => {
  switch (type) {
    case 'HEALTH_STEPS':
      return '运动步数联动'
    case 'HEALTH_SLEEP':
      return '睡眠状态联动'
    case 'TODO_OVERDUE':
      return '待办超期预警'
    case 'HABIT_STREAK':
      return '习惯断签提醒'
    case 'FINANCE_BUDGET':
      return '月度支出超支'
    default:
      return type
  }
}

onMounted(() => {
  fetchRules()
})
</script>

<style scoped>
.table-card {
  border-radius: 12px;
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

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 8px 0;
}
</style>
