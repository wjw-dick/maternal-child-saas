<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="period" class="toolbar-search" clearable placeholder="账期，如 2026-11" />
      <el-select v-model="status" clearable placeholder="状态" style="width: 140px" @change="reload">
        <el-option label="待支付" value="UNPAID" />
        <el-option label="已支付" value="PAID" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button type="success" @click="generate">生成账单</el-button>
    </div>

    <div class="stat-grid" style="margin-bottom: 16px">
      <div class="stat-card">
        <div class="label">近 30 天账单</div>
        <div class="value">{{ money(summary.total) }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已回款</div>
        <div class="value success">{{ money(summary.paid) }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待回款</div>
        <div class="value warning">{{ money(Number(summary.total || 0) - Number(summary.paid || 0)) }}</div>
      </div>
    </div>

    <div class="card">
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="tenantId" label="租户 ID" width="160" />
        <el-table-column prop="period" label="账期" width="110" />
        <el-table-column prop="item" label="计费项" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="amount" label="金额(元)" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PAID' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" width="170">
          <template #default="{ row }">{{ fmt(row.paidAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'PAID'" link type="success" @click="pay(row)">确认收款</el-button>
            <span v-else class="muted">已完成</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const period = ref('')
const status = ref('')
const summary = ref<any>({})

function money(value: any) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function fmt(value: any) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

async function load() {
  loading.value = true
  try {
    const data: any = await http.get('/platform/bills', {
      params: { page: page.value, size: size.value, period: period.value || undefined, status: status.value || undefined }
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

async function refreshSummary() {
  summary.value = (await http.get('/platform/bills/summary')) || {}
}

async function generate() {
  const { value } = await ElMessageBox.prompt('请输入账期（YYYY-MM），留空使用当前月', '生成账单', {
    inputValue: new Date().toISOString().slice(0, 7)
  })
  const data: any = await http.post('/platform/bills/generate', null, { params: { period: value || undefined } })
  ElMessage.success(`已生成 ${data.generated} 条账单`)
  load()
  refreshSummary()
}

async function pay(row: any) {
  await ElMessageBox.confirm('确认该账单已收款？', '确认收款', { type: 'warning' })
  await http.post(`/platform/bills/${row.id}/pay`)
  ElMessage.success('已标记为已支付')
  load()
  refreshSummary()
}

onMounted(() => {
  load()
  refreshSummary()
})
</script>
