<template>
  <div class="page">
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">会员总数</div>
        <div class="value">{{ report.memberCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">订单总数</div>
        <div class="value">{{ report.orderCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">实收金额(元)</div>
        <div class="value">{{ money(report.paidAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="label">预约总数</div>
        <div class="value">{{ report.appointmentCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">储值余额(元)</div>
        <div class="value">{{ money(report.walletBalance) }}</div>
      </div>
      <div class="stat-card">
        <div class="label">渠道佣金(元)</div>
        <div class="value">{{ money(report.commission) }}</div>
      </div>
    </div>

    <div class="card" style="margin-top: 16px">
      <div class="card-title">近 7 日订单趋势</div>
      <el-table :data="report.daily || []" size="small">
        <el-table-column prop="d" label="日期" width="150" />
        <el-table-column prop="c" label="订单数" width="120" />
        <el-table-column label="实收金额">
          <template #default="{ row }">{{ money(row.amount) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../../api/http'

const report = ref<any>({})

function money(value: any) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
  report.value = (await http.get('/tenant/report')) || {}
})
</script>
