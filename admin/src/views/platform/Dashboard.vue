<template>
  <div class="page">
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">租户总数</div>
        <div class="value">{{ stats.tenantTotal || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">正常运营</div>
        <div class="value success">{{ stats.tenantActive || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">30 天内到期</div>
        <div class="value warning">{{ stats.tenantExpiring || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">累计回款(元)</div>
        <div class="value">{{ money(stats.revenue) }}</div>
      </div>
    </div>

    <div class="grid-2" style="margin-top: 16px">
      <div class="card">
        <div class="card-title">套餐分布</div>
        <el-table :data="plans" size="small">
          <el-table-column prop="name" label="套餐" />
          <el-table-column prop="price" label="价格" width="110" />
          <el-table-column prop="billingCycle" label="周期" width="90" />
          <el-table-column prop="seatLimit" label="席位" width="80" />
          <el-table-column prop="status" label="状态" width="90" />
        </el-table>
      </div>
      <div class="card">
        <div class="card-title">账单概览（近 30 天）</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账单金额">{{ money(summary.total) }}</el-descriptions-item>
          <el-descriptions-item label="已支付">{{ money(summary.paid) }}</el-descriptions-item>
          <el-descriptions-item label="套餐数量">{{ summary.plans || 0 }}</el-descriptions-item>
          <el-descriptions-item label="待收款">
            {{ money(Number(summary.total || 0) - Number(summary.paid || 0)) }}
          </el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" plain style="margin-top: 16px" @click="router.push('/platform/bills')">
          前往账单管理
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'

const router = useRouter()
const stats = ref<any>({})
const summary = ref<any>({})
const plans = ref<any[]>([])

function money(value: any) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
  const [dashboard, billSummary, planPage]: any[] = await Promise.all([
    http.get('/platform/dashboard'),
    http.get('/platform/bills/summary'),
    http.get('/platform/plans', { params: { page: 1, size: 20 } })
  ])
  stats.value = dashboard || {}
  summary.value = billSummary || {}
  plans.value = planPage?.records || []
})
</script>
