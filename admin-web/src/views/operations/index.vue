<template>
  <PageContainer title="运营管理" description="平台关键模块调用活跃度排行与操作审计日志监控">
    <!-- 模块调用排行卡片 -->
    <el-card shadow="never" class="rank-card" v-loading="loadingRank">
      <template #header>
        <div class="card-header">
          <span>🏆 业务模块调用量与用户活跃排行 (近 30 天)</span>
          <el-button link type="primary" @click="fetchRankings">刷新排行</el-button>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col
          v-for="(mod, index) in rankingList"
          :key="mod.moduleCode"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="4"
        >
          <div class="mod-rank-item">
            <div class="rank-top">
              <span class="rank-badge" :class="'rank-' + (index + 1)">#{{ index + 1 }}</span>
              <span class="mod-name">{{ mod.moduleName }}</span>
              <el-tag size="small" type="info">{{ mod.moduleCode }}</el-tag>
            </div>
            <div class="rank-count">
              {{ mod.count.toLocaleString() }} <small class="unit">次调用</small>
            </div>
            <div class="rank-meta">
              <span>独立用户：<b>{{ mod.uniqueUsers }}</b> 人</span>
              <span>占比：<b>{{ mod.percentage }}%</b></span>
            </div>
            <el-progress
              :percentage="mod.percentage"
              :color="getModuleColor(mod.moduleCode)"
              :show-text="false"
              class="mt-2"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 模块操作审计日志查询与表格 -->
    <el-card shadow="never" class="mt-4 log-card">
      <template #header>
        <div class="card-header">
          <span>📋 模块关键业务操作审计日志 (实时记录)</span>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="业务模块">
          <el-select
            v-model="queryForm.moduleCode"
            placeholder="全部模块"
            clearable
            style="width: 140px;"
          >
            <el-option label="待办清单" value="TODO" />
            <el-option label="番茄专注" value="POMODORO" />
            <el-option label="习惯打卡" value="HABIT" />
            <el-option label="健康记录" value="HEALTH" />
            <el-option label="收支记账" value="FINANCE" />
            <el-option label="目标计划" value="GOAL" />
          </el-select>
        </el-form-item>

        <el-form-item label="操作动作">
          <el-select
            v-model="queryForm.actionCode"
            placeholder="全部动作"
            clearable
            style="width: 140px;"
          >
            <el-option label="新增/创建 (CREATE)" value="CREATE" />
            <el-option label="编辑/修改 (UPDATE)" value="UPDATE" />
            <el-option label="删除 (DELETE)" value="DELETE" />
            <el-option label="完成 (COMPLETE)" value="COMPLETE" />
            <el-option label="日常打卡 (CHECKIN)" value="CHECKIN" />
            <el-option label="开始会话 (START)" value="START" />
            <el-option label="正常结束 (FINISH)" value="FINISH" />
            <el-option label="放弃/中止 (ABORT)" value="ABORT" />
          </el-select>
        </el-form-item>

        <el-form-item label="用户 ID">
          <el-input
            v-model.number="queryForm.userId"
            placeholder="输入用户ID"
            clearable
            style="width: 130px;"
          />
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
          />
        </el-form-item>

        <el-form-item label="Request ID">
          <el-input
            v-model="queryForm.requestId"
            placeholder="链路跟踪 ID"
            clearable
            style="width: 160px;"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志表格 -->
      <el-table
        :data="logList"
        v-loading="loadingLogs"
        border
        stripe
        style="width: 100%"
        class="mt-2"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column label="操作用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="uname">{{ row.username }}</span>
              <span class="unick">({{ row.nickname || '无昵称' }})</span>
              <el-tag size="small" type="info" class="uid-tag">UID: {{ row.userId }}</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="业务模块" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getModuleTagType(row.moduleCode)">
              {{ row.moduleName }} ({{ row.moduleCode }})
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作动作" width="130" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" :type="getActionTagType(row.actionCode)">
              {{ row.actionName }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="businessRefId" label="关联业务主键" width="130" align="center">
          <template #default="{ row }">
            <span>{{ row.businessRefId ? '#' + row.businessRefId : '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="occurredAt" label="发生时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.occurredAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="requestId" label="链路追踪 Request ID" min-width="180">
          <template #default="{ row }">
            <span class="req-id">{{ row.requestId || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchLogs"
          @current-change="fetchLogs"
        />
      </div>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getModuleUsageLogsApi, getModuleRankingApi } from '../../api/operations'
import type { ModuleUsageLogItem, ModuleUsageLogQuery } from '../../types/operations'
import type { ModuleUsageRank } from '../../types/dashboard'

// 排行状态
const rankingList = ref<ModuleUsageRank[]>([])
const loadingRank = ref(false)

// 日志表格状态
const logList = ref<ModuleUsageLogItem[]>([])
const totalCount = ref(0)
const loadingLogs = ref(false)
const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive<ModuleUsageLogQuery>({
  moduleCode: undefined,
  actionCode: undefined,
  userId: undefined,
  requestId: undefined,
  startDate: undefined,
  endDate: undefined,
  current: 1,
  size: 10
})

// 拉取模块使用排行
const fetchRankings = async () => {
  try {
    loadingRank.value = true
    const res = await getModuleRankingApi()
    rankingList.value = res.data || []
  } catch (err) {
    console.error('拉取模块排行失败', err)
  } finally {
    loadingRank.value = false
  }
}

// 拉取日志列表
const fetchLogs = async () => {
  try {
    loadingLogs.value = true
    if (dateRange.value && dateRange.value.length === 2) {
      queryForm.startDate = dateRange.value[0]
      queryForm.endDate = dateRange.value[1]
    } else {
      queryForm.startDate = undefined
      queryForm.endDate = undefined
    }

    const res = await getModuleUsageLogsApi(queryForm)
    logList.value = res.data.records || []
    totalCount.value = res.data.total || 0
  } catch (err) {
    console.error('拉取审计日志失败', err)
  } finally {
    loadingLogs.value = false
  }
}

const handleSearch = () => {
  queryForm.current = 1
  fetchLogs()
}

const handleReset = () => {
  queryForm.moduleCode = undefined
  queryForm.actionCode = undefined
  queryForm.userId = undefined
  queryForm.requestId = undefined
  dateRange.value = null
  queryForm.current = 1
  queryForm.size = 10
  fetchLogs()
}

const formatDateTime = (val?: string) => {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

const getModuleColor = (code: string) => {
  switch (code) {
    case 'TODO': return '#1677ff'
    case 'POMODORO': return '#722ed1'
    case 'HABIT': return '#13c2c2'
    case 'HEALTH': return '#52c41a'
    case 'FINANCE': return '#fa8c16'
    case 'GOAL': return '#eb2f96'
    default: return '#8c8c8c'
  }
}

const getModuleTagType = (code: string) => {
  switch (code) {
    case 'TODO': return 'primary'
    case 'POMODORO': return 'warning'
    case 'HABIT': return 'success'
    case 'HEALTH': return 'success'
    case 'FINANCE': return 'danger'
    default: return 'info'
  }
}

const getActionTagType = (action: string) => {
  switch (action) {
    case 'CREATE': return 'primary'
    case 'UPDATE': return 'warning'
    case 'DELETE': return 'danger'
    case 'COMPLETE':
    case 'FINISH':
    case 'CHECKIN': return 'success'
    case 'ABORT':
    case 'CANCEL': return 'info'
    default: return 'info'
  }
}

onMounted(() => {
  fetchRankings()
  fetchLogs()
})
</script>

<style scoped>
.rank-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
}

.mod-rank-item {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
}

.rank-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.rank-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  background: #f0f0f0;
  color: #595959;

  &.rank-1 { background: #fff1f0; color: #f5222d; }
  &.rank-2 { background: #fff7e6; color: #fa8c16; }
  &.rank-3 { background: #e6f7ff; color: #1677ff; }
}

.mod-name {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.rank-count {
  font-size: 20px;
  font-weight: 700;
  color: #1f2d3d;
  margin: 6px 0;

  .unit {
    font-size: 12px;
    font-weight: normal;
    color: #8c8c8c;
  }
}

.rank-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 6px;
}

.log-card {
  border-radius: 8px;
}

.filter-form {
  margin-bottom: 8px;
}

.user-cell {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;

  .uname {
    font-weight: 600;
    color: #262626;
  }

  .unick {
    font-size: 12px;
    color: #8c8c8c;
  }

  .uid-tag {
    font-size: 11px;
  }
}

.req-id {
  font-family: monospace;
  font-size: 12px;
  color: #8c8c8c;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
