<template>
  <view class="container">
    <view class="profile-card">
      <view class="avatar">{{ (name || '悦').slice(0, 1) }}</view>
      <view class="profile-info">
        <view class="profile-name">{{ name }}</view>
        <view class="profile-sub">{{ logged ? `会员编号：${center.member?.memberNo || '-'}` : '登录享会员权益' }}</view>
      </view>
      <view v-if="!logged" class="btn-ghost mini-btn" @click="goLogin">登录</view>
      <view v-else class="btn-ghost mini-btn" @click="logout">退出</view>
    </view>

    <view class="card" v-if="logged">
      <view class="wallet-row">
        <view class="wallet-item">
          <view class="wallet-value">{{ center.wallet?.balance || 0 }}</view>
          <view class="muted">储值余额</view>
        </view>
        <view class="wallet-item">
          <view class="wallet-value">{{ center.member?.points || 0 }}</view>
          <view class="muted">积分</view>
        </view>
        <view class="wallet-item">
          <view class="wallet-value">{{ center.coupons?.length || 0 }}</view>
          <view class="muted">优惠券</view>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="section-title">宝宝档案</view>
      <view v-for="baby in center.babies || []" :key="baby.id" class="baby-item">
        <view class="baby-avatar">{{ (baby.name || '宝').slice(0, 1) }}</view>
        <view>
          <view class="service-name">{{ baby.name }}</view>
          <view class="muted">{{ baby.birthday || '未填写生日' }} · {{ baby.gender === 'M' ? '男宝' : baby.gender === 'F' ? '女宝' : '未知' }}</view>
        </view>
      </view>
      <view v-if="!center.babies?.length" class="empty">暂无宝宝档案</view>
    </view>

    <view class="card">
      <view class="section-title">业务工作台</view>
      <view class="muted" style="margin-bottom: 16rpx">护理师 / 员工 / 供应商 / 渠道商账号登录后可进入对应工作台。</view>
      <view class="btn-ghost" @click="goWorkbench">进入工作台</view>
    </view>

    <view class="card">
      <view class="menu-row" @click="go('/pages/order/list')">我的订单 <text>›</text></view>
      <view class="menu-row" @click="go('/pages/appointment/create')">我的预约 <text>›</text></view>
      <view class="menu-row" @click="go('/pages/service/list')">服务项目 <text>›</text></view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../../api/request'

const logged = ref(!!uni.getStorageSync('token'))
const center = ref({})
const user = ref(uni.getStorageSync('user') || {})

const name = computed(() => center.value.member?.name || user.value?.nickname || user.value?.name || '悦宝会员')

function go(url) {
  uni.navigateTo({ url })
}
function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}
function goWorkbench() {
  if (!logged.value) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/workbench/index' })
}
function logout() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
  uni.removeStorageSync('userType')
  logged.value = false
  center.value = {}
  uni.showToast({ title: '已退出', icon: 'none' })
}

onMounted(async () => {
  if (!logged.value) return
  try {
    center.value = (await api.center()) || {}
  } catch (e) {
    // 员工账号没有会员中心，忽略
  }
})
</script>

<style scoped>
.profile-card {
  background: linear-gradient(135deg, #ff9a76, #ff5c8a);
  border-radius: 24rpx;
  padding: 34rpx;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 22rpx;
  margin-bottom: 24rpx;
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  font-weight: 700;
}
.profile-info {
  flex: 1;
}
.profile-name {
  font-size: 34rpx;
  font-weight: 600;
}
.profile-sub {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 6rpx;
}
.profile-card .btn-ghost {
  border-color: #fff;
  color: #fff;
  background: transparent;
  padding: 12rpx 28rpx;
  font-size: 24rpx;
}
.wallet-row {
  display: flex;
  justify-content: space-around;
  text-align: center;
}
.wallet-value {
  font-size: 38rpx;
  font-weight: 700;
  color: #ff5c8a;
  margin-bottom: 6rpx;
}
.baby-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 16rpx 0;
}
.baby-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: #fff1f5;
  color: #ff5c8a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.menu-row {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
  color: #33415c;
  font-size: 28rpx;
}
.menu-row:last-child {
  border-bottom: none;
}
</style>
