<template>
  <view class="container">
    <view v-if="!logged" class="card">
      <view class="empty">登录后查看订单</view>
      <view class="btn-primary" @click="goLogin">去登录</view>
    </view>
    <template v-else>
      <view class="card" v-for="item in orders" :key="item.id">
        <view class="row">
          <text class="order-no">{{ item.orderNo }}</text>
          <text class="tag">{{ item.status }}</text>
        </view>
        <view class="row" style="margin-top: 14rpx">
          <text class="muted">{{ fmt(item.createdAt) }} · {{ item.source || 'MINI' }}</text>
          <text class="price">¥{{ item.payAmount }}</text>
        </view>
        <view class="row" style="margin-top: 18rpx">
          <text class="muted">支付状态：{{ item.payStatus }}</text>
          <view v-if="item.payStatus !== 'PAID'" class="btn-ghost mini-btn" @click="pay(item)">去支付</view>
        </view>
      </view>
      <view v-if="!orders.length" class="empty">暂无订单</view>
    </template>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/request'

const logged = ref(!!uni.getStorageSync('token'))
const orders = ref([])

function fmt(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}
function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}
async function pay(item) {
  await api.payOrder(item.id)
  uni.showModal({
    title: '支付成功',
    content: '支付结果已回传，点击确定返回订单列表',
    showCancel: false,
    success: () => {
      load()
    }
  })
}
async function load() {
  orders.value = (await api.myOrders()) || []
}
onMounted(() => {
  if (logged.value) load()
})
</script>

<style scoped>
.order-no {
  font-weight: 600;
  font-size: 28rpx;
}
.mini-btn {
  padding: 10rpx 30rpx;
  font-size: 24rpx;
}
</style>
