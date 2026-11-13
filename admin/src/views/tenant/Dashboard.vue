<template>
  <div class="page">
    <el-alert
      v-if="notices.length"
      :title="`平台公告：${notices[0].title}`"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    >
      <div class="notice-content" v-html="notices[0].content"></div>
    </el-alert>

    <div class="stat-grid">
      <div class="stat-card clickable" @click="router.push('/tenant/members')">
        <div class="label">家庭会员</div>
        <div class="value">{{ stats.members || 0 }}</div>
      </div>
      <div class="stat-card clickable" @click="router.push('/tenant/appointments')">
        <div class="label">今日预约</div>
        <div class="value">{{ stats.appointmentsToday || 0 }}</div>
        <div class="sub">待处理 {{ stats.appointmentsPending || 0 }}</div>
      </div>
      <div class="stat-card clickable" @click="router.push('/tenant/orders')">
        <div class="label">今日订单</div>
        <div class="value">{{ stats.ordersToday || 0 }}</div>
        <div class="sub">待处理 {{ stats.ordersPending || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">累计实收(元)</div>
        <div class="value">{{ money(stats.revenue) }}</div>
      </div>
    </div>

    <div class="grid-2" style="margin-top: 16px">
      <div class="card">
        <div class="card-title">经营提醒</div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="库存预警 SKU">{{ stats.lowStock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="30 天内到期证照">{{ stats.licenseExpiring || 0 }}</el-descriptions-item>
          <el-descriptions-item label="待处理预约">{{ stats.appointmentsPending || 0 }}</el-descriptions-item>
          <el-descriptions-item label="待处理订单">{{ stats.ordersPending || 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <div class="card-title">快捷入口</div>
        <div class="quick-grid">
          <el-button @click="router.push('/tenant/members')">会员建档</el-button>
          <el-button @click="router.push('/tenant/appointments')">预约派单</el-button>
          <el-button @click="router.push('/tenant/orders')">订单处理</el-button>
          <el-button @click="router.push('/tenant/products')">商品管理</el-button>
          <el-button @click="router.push('/tenant/menu-designer')">菜单配置</el-button>
          <el-button @click="router.push('/tenant/wechat')">公众号运营</el-button>
          <el-button @click="router.push('/tenant/report')">经营报表</el-button>
          <el-button @click="router.push('/tenant/config')">全局参数</el-button>
        </div>
      </div>
    </div>

    <div class="card" style="margin-top: 16px">
      <div class="card-title">平台公告</div>
      <el-table :data="notices" size="small">
        <el-table-column prop="title" label="标题" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">{{ fmt(row.publishedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button link type="primary" @click="read(row)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!notices.length" description="暂无公告" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const router = useRouter()
const stats = ref<any>({})
const notices = ref<any[]>([])

function money(value: any) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function fmt(value: any) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

async function read(row: any) {
  await http.post(`/tenant/notices/${row.id}/read`)
  notices.value = notices.value.filter((item) => item.id !== row.id)
  ElMessage.success('已标记为已读')
}

onMounted(async () => {
  const [dashboard, noticeList]: any[] = await Promise.all([
    http.get('/tenant/dashboard'),
    http.get('/tenant/notices')
  ])
  stats.value = dashboard || {}
  notices.value = noticeList || []
})
</script>

<style scoped>
.notice-content {
  color: #606e85;
  margin-top: 4px;
}
.stat-card.clickable {
  cursor: pointer;
  transition: transform 0.15s;
}
.stat-card.clickable:hover {
  transform: translateY(-2px);
}
.stat-card .sub {
  color: #94a1b5;
  font-size: 12px;
  margin-top: 4px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
</style>
