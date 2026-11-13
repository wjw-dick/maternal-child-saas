<template>
  <view class="container">
    <view v-if="!logged" class="card">
      <view class="empty">请先登录员工 / 供应商 / 渠道商账号</view>
      <view class="btn-primary" @click="goLogin">去登录</view>
    </view>

    <template v-else>
      <view class="hero-card">
        <view class="hero-title">{{ roleName }}</view>
        <view class="hero-sub">{{ user.nickname || user.username }} · {{ user.userType }}</view>
      </view>

      <!-- 护理师 / 员工端 -->
      <template v-if="role === 'staff'">
        <view class="stat-grid">
          <view class="stat-card">
            <view class="stat-value">{{ workbench.today?.length || 0 }}</view>
            <view class="muted">今日预约</view>
          </view>
          <view class="stat-card">
            <view class="stat-value">{{ workbench.pending?.length || 0 }}</view>
            <view class="muted">待处理</view>
          </view>
          <view class="stat-card">
            <view class="stat-value">{{ income.completed || 0 }}</view>
            <view class="muted">已完成</view>
          </view>
          <view class="stat-card">
            <view class="stat-value">{{ income.amount || 0 }}</view>
            <view class="muted">服务金额</view>
          </view>
        </view>

        <view class="card">
          <view class="section-title">今日服务</view>
          <view v-for="item in workbench.today || []" :key="item.id" class="task-item" @click="openTask(item)">
            <view class="row">
              <text class="task-title">{{ item.timeSlot }} · {{ item.contact || '客户' }}</text>
              <text class="tag">{{ item.status }}</text>
            </view>
            <view class="muted">{{ item.appointmentNo }}</view>
            <view class="task-actions">
              <view v-if="item.status === 'PENDING' || item.status === 'CONFIRMED'" class="btn-ghost mini-btn" @click.stop="accept(item)">接单</view>
              <view v-if="item.status === 'SERVING'" class="btn-primary mini-btn" @click.stop="complete(item)">完成服务</view>
            </view>
          </view>
          <view v-if="!workbench.today?.length" class="empty">今日暂无排班</view>
        </view>

        <view class="entry-grid">
          <view class="entry-item" @click="goStaffIncome">收入与工时</view>
          <view class="entry-item" @click="goStaffProfile">消息 / 改密</view>
        </view>
      </template>

      <!-- 供应商端 -->
      <template v-if="role === 'supplier'">
        <view class="card">
          <view class="section-title">供货单</view>
          <view v-for="item in supplyOrders" :key="item.id" class="task-item">
            <view class="row">
              <text class="task-title">{{ item.supplyNo }}</text>
              <text class="tag">{{ item.status }}</text>
            </view>
            <view class="muted">金额 ¥{{ item.totalAmount }} · 预计到货 {{ fmt(item.expectedAt) }}</view>
            <view class="task-actions">
              <view v-if="item.status !== 'SIGNED'" class="btn-primary mini-btn" @click="sign(item)">确认签收</view>
            </view>
          </view>
          <view v-if="!supplyOrders.length" class="empty">暂无供货单</view>
        </view>
        <view class="card">
          <view class="section-title">对账单 / 结算</view>
          <view v-for="item in statements" :key="item.id" class="row" style="padding: 16rpx 0">
            <text>{{ item.period }}</text>
            <text class="muted">{{ item.status }}</text>
            <text class="price">¥{{ item.amount }}</text>
          </view>
          <view v-if="!statements.length" class="empty">暂无对账单</view>
          <view class="btn-ghost" style="margin-top: 16rpx" @click="goSupplierProfile">资质 / 消息 / 改密</view>
        </view>
      </template>

      <!-- 渠道商端 -->
      <template v-if="role === 'distributor'">
        <view class="stat-grid">
          <view class="stat-card">
            <view class="stat-value">{{ credit.creditLimit || 0 }}</view>
            <view class="muted">授信额度</view>
          </view>
          <view class="stat-card">
            <view class="stat-value">{{ credit.usedCredit || 0 }}</view>
            <view class="muted">已用额度</view>
          </view>
          <view class="stat-card">
            <view class="stat-value">{{ credit.available || 0 }}</view>
            <view class="muted">可用额度</view>
          </view>
        </view>
        <view class="card">
          <view class="section-title">报单记录</view>
          <view v-for="item in reports" :key="item.id" class="row" style="padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8">
            <view>
              <view>{{ item.reportNo }}</view>
              <view class="muted">{{ item.status }}</view>
            </view>
            <text class="price">¥{{ item.amount }}</text>
          </view>
          <view v-if="!reports.length" class="empty">暂无报单</view>
        </view>
        <view class="card">
          <view class="section-title">佣金流水</view>
          <view v-for="item in commissions" :key="item.id" class="row" style="padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8">
            <text>{{ item.flowType }}</text>
            <text class="price">¥{{ item.amount }}</text>
          </view>
          <view v-if="!commissions.length" class="empty">暂无佣金</view>
        </view>

        <view class="card">
          <view class="section-title">结算记录</view>
          <view v-for="item in settlements" :key="item.id" class="row" style="padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8">
            <view>
              <view>{{ item.settlementNo }}</view>
              <view class="muted">{{ item.period }} · {{ item.status }}</view>
            </view>
            <text class="price">¥{{ item.amount }}</text>
          </view>
          <view v-if="!settlements.length" class="empty">暂无结算记录</view>
        </view>

        <view class="card">
          <view class="section-title">我的订单</view>
          <view v-for="item in distOrders" :key="item.orderNo" class="row" style="padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8">
            <view>
              <view>{{ item.orderNo }}</view>
              <view class="muted">{{ item.reportNo }} · {{ item.status }}</view>
            </view>
            <text class="price">¥{{ item.payAmount }}</text>
          </view>
          <view v-if="!distOrders.length" class="empty">暂无关联订单</view>
        </view>

        <view class="card">
          <view class="section-title">我的下级</view>
          <view v-for="item in subordinates" :key="item.id" class="row" style="padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8">
            <view>
              <view>{{ item.name }}</view>
              <view class="muted">{{ item.code }} · {{ item.contact || '-' }}</view>
            </view>
            <text class="tag">{{ item.status }}</text>
          </view>
          <view v-if="!subordinates.length" class="empty">暂无下级渠道商</view>
          <view class="btn-ghost" style="margin-top: 16rpx" @click="goDistributorProfile">消息 / 改密</view>
        </view>
      </template>

      <view v-if="role === 'unknown'" class="card">
        <view class="empty">当前账号未绑定员工、供应商或渠道商档案</view>
      </view>

      <!-- 员工端入口（店长/前台/库管/财务） -->
      <template v-if="role === 'employee'">
        <view class="card">
          <view class="section-title">员工工作台</view>
          <view class="muted">今日预约、到店核销、开卡储值、库存查询、报表简报与修改密码。</view>
          <view class="entry-grid">
            <view class="entry-item" @click="goEmployee">进入员工工作台</view>
            <view class="entry-item" @click="goEmployeeWallet">开卡 / 储值</view>
            <view class="entry-item" @click="goEmployeeStock">库存查询</view>
            <view class="entry-item" @click="goEmployeeProfile">我的 / 改密</view>
          </view>
        </view>
      </template>
    </template>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api/request'

const logged = ref(!!uni.getStorageSync('token'))
const user = ref({})
const workbench = ref({})
const income = ref({})
const supplyOrders = ref([])
const statements = ref([])
const reports = ref([])
const commissions = ref([])
const credit = ref({})
const settlements = ref([])
const distOrders = ref([])
const subordinates = ref([])

const role = computed(() => {
  if (user.value.staffId) return 'staff'
  if (user.value.supplierId) return 'supplier'
  if (user.value.distributorId) return 'distributor'
  if (user.value.userType === 'TENANT') return 'employee'
  return 'unknown'
})
const roleName = computed(() => ({
  staff: '一线服务工作台',
  supplier: '供应商工作台',
  distributor: '渠道商工作台',
  employee: '员工工作台',
  unknown: '业务工作台'
}[role.value]))

function goEmployee() {
  uni.navigateTo({ url: '/pages/employee/index' })
}
function goEmployeeWallet() {
  uni.navigateTo({ url: '/pages/employee/wallet' })
}
function goEmployeeStock() {
  uni.navigateTo({ url: '/pages/employee/stock' })
}
function goEmployeeProfile() {
  uni.navigateTo({ url: '/pages/employee/profile' })
}
function openTask(item) {
  uni.setStorageSync('staffAppointmentId', item.id)
  uni.navigateTo({ url: '/pages/staff/appointment' })
}
function goStaffIncome() {
  uni.navigateTo({ url: '/pages/staff/income' })
}
function goStaffProfile() {
  uni.navigateTo({ url: '/pages/staff/profile' })
}
function goSupplierProfile() {
  uni.navigateTo({ url: '/pages/supplier/profile' })
}
function goDistributorProfile() {
  uni.navigateTo({ url: '/pages/distributor/profile' })
}

function fmt(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}
function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}
async function accept(item) {
  await api.acceptAppointment(item.id)
  uni.showToast({ title: '已接单', icon: 'success' })
  load()
}
async function complete(item) {
  await api.completeAppointment(item.id)
  uni.showToast({ title: '服务已完成', icon: 'success' })
  load()
}
async function sign(item) {
  await api.signSupply(item.id)
  uni.showToast({ title: '签收成功，库存已入库', icon: 'success' })
  load()
}

async function load() {
  if (!logged.value) return
  const me = await api.me()
  user.value = me.user || {}
  uni.setStorageSync('user', user.value)
  if (role.value === 'staff') {
    workbench.value = (await api.workbench()) || {}
    income.value = (await api.staffIncome()) || {}
  } else if (role.value === 'supplier') {
    supplyOrders.value = (await api.supplyOrders()) || []
    statements.value = (await api.statements()) || []
  } else if (role.value === 'distributor') {
    reports.value = (await api.distributorReports()) || []
    commissions.value = (await api.commissions()) || []
    credit.value = (await api.credit()) || {}
    settlements.value = (await api.distributorSettlements()) || []
    distOrders.value = (await api.distributorOrders()) || []
    subordinates.value = (await api.distributorSubordinates()) || []
  }
}

onShow(load)
</script>

<style scoped>
.hero-card {
  background: linear-gradient(135deg, #33415c, #1d2a3f);
  border-radius: 24rpx;
  padding: 36rpx;
  color: #fff;
  margin-bottom: 24rpx;
}
.hero-title {
  font-size: 36rpx;
  font-weight: 700;
}
.hero-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.75;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
  margin-bottom: 24rpx;
}
.stat-card {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  text-align: center;
}
.stat-value {
  font-size: 38rpx;
  font-weight: 700;
  color: #ff5c8a;
  margin-bottom: 6rpx;
}
.task-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
}
.task-item:last-child {
  border-bottom: none;
}
.task-title {
  font-weight: 600;
  font-size: 28rpx;
}
.task-actions {
  margin-top: 14rpx;
  display: flex;
  gap: 16rpx;
}
.mini-btn {
  padding: 10rpx 30rpx;
  font-size: 24rpx;
}
.entry-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 20rpx;
}
.entry-item {
  background: #fff1f5;
  color: #ff5c8a;
  border-radius: 14rpx;
  padding: 24rpx;
  text-align: center;
  font-size: 26rpx;
}
</style>
