<template>
  <view class="container">
    <view class="hero-card">
      <view class="hero-title">员工工作台</view>
      <view class="hero-sub">{{ user.nickname || user.username || '门店员工' }} · {{ today }}</view>
    </view>

    <view class="stat-grid">
      <view class="stat-card">
        <view class="stat-value">{{ overview.todayTotal || 0 }}</view>
        <view class="muted">今日预约</view>
      </view>
      <view class="stat-card">
        <view class="stat-value">{{ overview.pendingVerify || 0 }}</view>
        <view class="muted">待核销</view>
      </view>
      <view class="stat-card">
        <view class="stat-value">{{ overview.completed || 0 }}</view>
        <view class="muted">已完成</view>
      </view>
      <view class="stat-card">
        <view class="stat-value">{{ overview.todayOrders || 0 }}</view>
        <view class="muted">今日订单</view>
      </view>
    </view>

    <view class="card">
      <view class="section-title">到店核销</view>
      <view class="verify-row">
        <input v-model="code" class="verify-input" type="number" maxlength="6" placeholder="输入 6 位核销码" />
        <view class="btn-primary verify-btn" @click="doVerify">核销</view>
      </view>
      <view class="muted">核销后预约状态变为已完成，记录写入本门店。</view>
    </view>

    <view class="card">
      <view class="row">
        <view class="section-title">今日预约</view>
        <view class="muted">{{ appointments.length }} 条</view>
      </view>
      <view v-for="item in appointments" :key="item.id" class="task-item">
        <view class="row">
          <text class="task-title">{{ item.timeSlot }} · {{ item.contact || '客户' }}</text>
          <text class="tag">{{ statusText(item.status) }}</text>
        </view>
        <view class="muted">{{ item.appointmentNo }} · {{ item.appointType === 'HOME' ? '上门' : '到店' }}</view>
      </view>
      <view v-if="!appointments.length" class="empty">今日暂无预约</view>
    </view>

    <view class="card">
      <view class="row">
        <view class="section-title">今日订单</view>
        <view class="muted">{{ orders.length }} 条</view>
      </view>
      <view v-for="item in orders" :key="item.id" class="task-item">
        <view class="row">
          <text class="task-title">{{ item.orderNo }}</text>
          <text class="tag">{{ orderText(item.status) }}</text>
        </view>
        <view class="row">
          <text class="muted">¥{{ item.payAmount || item.totalAmount }}</text>
          <view v-if="item.status === 'PAID'" class="btn-ghost mini-btn" @click="completeOrder(item)">完成订单</view>
        </view>
      </view>
      <view v-if="!orders.length" class="empty">今日暂无订单</view>
    </view>

    <view class="card">
      <view class="section-title">今日简报</view>
      <view class="brief-grid">
        <view class="brief-item">
          <view class="brief-value">¥{{ brief.revenue || 0 }}</view>
          <view class="muted">实收</view>
        </view>
        <view class="brief-item">
          <view class="brief-value">{{ brief.newMembers || 0 }}</view>
          <view class="muted">新增会员</view>
        </view>
        <view class="brief-item">
          <view class="brief-value">{{ overview.lowStock || 0 }}</view>
          <view class="muted">低库存 SKU</view>
        </view>
        <view class="brief-item">
          <view class="brief-value">{{ schedules.length }}</view>
          <view class="muted">排班</view>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="row">
        <view class="section-title">近期排班</view>
        <view class="muted">{{ schedules.length }} 条</view>
      </view>
      <view v-for="item in schedules" :key="item.id" class="task-item">
        <view class="row">
          <text class="task-title">{{ item.workDate }} · {{ item.timeSlot }}</text>
          <text class="tag">{{ item.status || '排班' }}</text>
        </view>
        <view class="muted">已约 {{ item.booked || 0 }} / 容量 {{ item.capacity || 0 }}</view>
      </view>
      <view v-if="!schedules.length" class="empty">暂无排班</view>
    </view>

    <view class="card">
      <view class="section-title">快捷操作</view>
      <view class="quick-grid">
        <view class="quick-item" @click="go('/pages/employee/wallet')">开卡 / 储值</view>
        <view class="quick-item" @click="go('/pages/employee/stock')">库存查询</view>
        <view class="quick-item" @click="go('/pages/employee/profile')">我的 / 改密</view>
        <view class="quick-item" @click="reload">刷新数据</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/request'

const user = ref(uni.getStorageSync('user') || {})
const overview = ref({})
const appointments = ref([])
const orders = ref([])
const brief = ref({})
const schedules = ref([])
const code = ref('')
const today = new Date().toISOString().slice(0, 10)

function statusText(s) {
  return { PENDING: '待确认', CONFIRMED: '已确认', SERVING: '服务中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
}
function orderText(s) {
  return { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
}
function go(url) {
  uni.navigateTo({ url })
}

async function reload() {
  overview.value = (await api.employeeOverview()) || {}
  appointments.value = (await api.employeeAppointments()) || []
  orders.value = (await api.employeeOrders()) || []
  brief.value = (await api.employeeBrief()) || {}
  schedules.value = (await api.employeeSchedules()) || []
}

async function doVerify() {
  if (!code.value) {
    uni.showToast({ title: '请输入核销码', icon: 'none' })
    return
  }
  try {
    await api.employeeVerify(code.value)
    uni.showToast({ title: '核销成功', icon: 'success' })
    code.value = ''
    reload()
  } catch (e) {
    // 错误信息已由 request 统一提示
  }
}

async function completeOrder(item) {
  try {
    await api.employeeCompleteOrder(item.id)
    uni.showToast({ title: '订单已完成', icon: 'success' })
    reload()
  } catch (e) {
    // 已提示
  }
}

onMounted(async () => {
  try {
    const me = await api.me()
    user.value = me.user || user.value
  } catch (e) {
    // 已提示
  }
  reload()
})
</script>

<style scoped>
.hero-card {
  background: linear-gradient(135deg, #33415c, #1d2a3f);
  border-radius: 24rpx;
  padding: 36rpx;
  color: #fff;
  margin-bottom: 24rpx;
}
.hero-title { font-size: 36rpx; font-weight: 700; }
.hero-sub { margin-top: 8rpx; font-size: 24rpx; opacity: 0.75; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
  margin-bottom: 24rpx;
}
.stat-card { background: #fff; border-radius: 18rpx; padding: 24rpx; text-align: center; }
.stat-value { font-size: 38rpx; font-weight: 700; color: #ff5c8a; margin-bottom: 6rpx; }
.verify-row { display: flex; gap: 16rpx; align-items: center; margin-bottom: 12rpx; }
.verify-input {
  flex: 1;
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
}
.verify-btn { padding: 18rpx 40rpx; font-size: 26rpx; }
.task-item { padding: 20rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.task-item:last-child { border-bottom: none; }
.task-title { font-weight: 600; font-size: 28rpx; }
.mini-btn { padding: 8rpx 26rpx; font-size: 24rpx; }
.brief-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; }
.brief-item { background: #f7f9fc; border-radius: 14rpx; padding: 22rpx; text-align: center; }
.brief-value { font-size: 32rpx; font-weight: 700; color: #25324a; }
.quick-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; }
.quick-item {
  background: #fff1f5;
  color: #ff5c8a;
  border-radius: 14rpx;
  padding: 26rpx;
  text-align: center;
  font-size: 26rpx;
}
</style>
