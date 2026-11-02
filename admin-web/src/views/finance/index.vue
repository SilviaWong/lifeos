<template>
  <PageContainer title="收支分类" description="维护系统全局收支基础分类，统一用户端记账来源">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索工具栏 (14_admin_finance_categories.png) -->
      <div class="table-toolbar">
        <div class="search-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分类名称..."
            clearable
            prefix-icon="Search"
            style="width: 240px;"
          />
          <el-select
            v-model="filterType"
            placeholder="收支方向"
            clearable
            style="width: 140px;"
            @change="fetchCategories"
          >
            <el-option label="全部分类" value="" />
            <el-option label="收入分类" value="INCOME" />
            <el-option label="支出分类" value="EXPENSE" />
          </el-select>
        </div>
        <div class="action-right">
          <el-button type="primary" icon="Plus" @click="openCreateModal">
            新增分类
          </el-button>
          <el-button icon="Refresh" @click="fetchCategories">刷新</el-button>
        </div>
      </div>

      <!-- 分类数据表格 -->
      <el-table
        :data="filteredCategories"
        v-loading="loading"
        style="width: 100%"
        stripe
        class="category-table"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="categoryName" label="分类名称" min-width="140">
          <template #default="{ row }">
            <span class="font-medium text-slate-800">{{ row.categoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="分类图标" width="100" align="center">
          <template #default="{ row }">
            <span class="category-icon-preview">{{ row.icon || '🏷️' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryType" label="收支方向" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.categoryType === 'INCOME' ? 'success' : 'danger'"
              effect="light"
            >
              {{ row.categoryType === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序序号" width="100" align="center" />
        <el-table-column prop="isSystem" label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isSystem === 1 ? 'info' : 'warning'">
              {{ row.isSystem === 1 ? '系统预设' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" icon="Edit" @click="openEditModal(row)">
              修改
            </el-button>
            <el-popconfirm title="确定删除该收支分类吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑分类 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改收支分类' : '新增系统收支分类'"
      width="460px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        style="padding-right: 16px;"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" placeholder="例如: 餐饮、交通、工资、理财收益" />
        </el-form-item>
        <el-form-item label="收支方向" prop="categoryType">
          <el-radio-group v-model="formData.categoryType" :disabled="isEdit">
            <el-radio value="EXPENSE">支出</el-radio>
            <el-radio value="INCOME">收入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="输入 Emoji 或图标，例如 🍔、🚗、💰" />
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="1" :max="999" style="width: 100%;" />
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
  getFinanceCategories,
  createFinanceCategory,
  updateFinanceCategory,
  deleteFinanceCategory,
  type FinanceCategory
} from '../../api/finance'

const loading = ref(false)
const categoryList = ref<FinanceCategory[]>([])
const searchKeyword = ref('')
const filterType = ref('')

const filteredCategories = computed(() => {
  let list = Array.isArray(categoryList.value) ? categoryList.value : []
  if (filterType.value) {
    list = list.filter((c) => c.categoryType === filterType.value)
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((c) => c.categoryName.toLowerCase().includes(kw))
  }
  return list
})

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getFinanceCategories(filterType.value || undefined)
    const list = Array.isArray(res.data) ? res.data : ((res.data as any)?.records || [])
    categoryList.value = Array.isArray(list) ? list : []
  } catch (err: any) {
    ElMessage.error(err.message || '获取收支分类失败')
    categoryList.value = []
  } finally {
    loading.value = false
  }
}

// Dialog 表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const currentId = ref<number | null>(null)

const formData = reactive({
  categoryName: '',
  categoryType: 'EXPENSE' as 'INCOME' | 'EXPENSE',
  icon: '🏷️',
  sortOrder: 10
})

const formRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryType: [{ required: true, message: '请选择收支方向', trigger: 'change' }]
}

const openCreateModal = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(formData, {
    categoryName: '',
    categoryType: 'EXPENSE',
    icon: '🏷️',
    sortOrder: 10
  })
  dialogVisible.value = true
}

const openEditModal = (row: FinanceCategory) => {
  isEdit.value = true
  currentId.value = row.id!
  Object.assign(formData, {
    categoryName: row.categoryName,
    categoryType: row.categoryType,
    icon: row.icon || '🏷️',
    sortOrder: row.sortOrder || 10
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
        await updateFinanceCategory(currentId.value, formData)
        ElMessage.success('修改收支分类成功')
      } else {
        await createFinanceCategory(formData)
        ElMessage.success('创建收支分类成功')
      }
      dialogVisible.value = false
      fetchCategories()
    } catch (err: any) {
      ElMessage.error(err.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await deleteFinanceCategory(id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败')
  }
}

onMounted(() => {
  fetchCategories()
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

.category-icon-preview {
  font-size: 20px;
  display: inline-block;
}
</style>
