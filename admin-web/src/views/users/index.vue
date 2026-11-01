<template>
  <PageContainer title="用户管理" description="管理系统已注册的普通用户与管理员账号">
    <!-- 筛选查询表单 -->
    <el-form :inline="true" :model="queryForm" class="filter-form">
      <el-form-item label="用户名">
        <el-input
          v-model="queryForm.username"
          placeholder="模糊搜索用户名"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input
          v-model="queryForm.phone"
          placeholder="手机号码"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="账号状态">
        <el-select
          v-model="queryForm.status"
          placeholder="全部状态"
          clearable
          style="width: 130px"
        >
          <el-option label="正常启用" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 用户数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      empty-text="暂无用户数据"
    >
      <el-table-column prop="id" label="用户ID" width="90" align="center" />
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="130" />
      <el-table-column prop="phone" label="手机号码" min-width="130">
        <template #default="{ row }">
          {{ row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="email" label="电子邮箱" min-width="160">
        <template #default="{ row }">
          {{ row.email || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="120">
        <template #default="{ row }">
          <el-tag
            v-for="role in row.roles"
            :key="role"
            size="small"
            :type="role === 'ADMIN' || role === 'SUPER_ADMIN' ? 'danger' : 'primary'"
            style="margin-right: 4px"
          >
            {{ role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" min-width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getUserPageApi, updateUserStatusApi } from '../../api/user'
import { formatDateTime } from '../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import type { UserVO, AdminUserQueryDTO } from '../../types/user'

const loading = ref<boolean>(false)
const tableData = ref<UserVO[]>([])

const queryForm = reactive<AdminUserQueryDTO>({
  username: '',
  phone: '',
  status: undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserPageApi({
      ...queryForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    // 错误在 request 拦截器中统一展示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchUserList()
}

const handleReset = () => {
  queryForm.username = ''
  queryForm.phone = ''
  queryForm.status = undefined
  pagination.pageNum = 1
  fetchUserList()
}

const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchUserList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchUserList()
}

const handleStatusChange = async (row: UserVO) => {
  const targetStatus = row.status
  const actionText = targetStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定要将用户 [${row.username}] 的账号状态调整为 [${actionText}] 吗？`,
      '状态变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateUserStatusApi(row.id, targetStatus)
    ElMessage.success(`用户 [${row.username}] 已成功${actionText}`)
  } catch (e: any) {
    // 恢复原开关状态
    row.status = targetStatus === 1 ? 0 : 1
    if (e !== 'cancel') {
      ElMessage.error(e.message || '修改状态失败')
    }
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.filter-form {
  background-color: #fafafa;
  padding: 16px 16px 0 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

