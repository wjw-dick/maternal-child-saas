<template>
  <view class="container">
    <view class="hero-card">
      <view class="hero-title">我的收入与工时</view>
      <view class="hero-sub">{{ user.nickname || user.username }}</view>
    </view>

    <view class="stat-grid">
      <view class="stat-card">
        <view class="stat-value">¥{{ income.amount || 0 }}</view>
        <view class="muted">累计服务金额</view>
      </view>
      <view class="stat-card">
        <view class="stat-value">{{ income.completed || 0 }}</view>
        <view class="muted">完成单数</view>
      </view>
      <view class="stat-card">
        <view class="stat-value">{{ totalHours }}</view>
        <view class="muted">累计工时(h)</view>
      </view>
      <view class="stat-card">
        <view class="stat-value">{{ detail.length }}</view>
        <view class="muted">近 50 单</view>
      </view>
    </view>

    <view class="card">
      <view class="section-title">收入明细</view>
      <view v-for="(item, i) in detail" :key="i" class="row detail-row">
        <view>
          <view class="service-name">{{ item.serviceName }}</view>
          <view class="muted">{{ item.appointDate }} {{ item.timeSlot }} · {{ item.duration }} 分钟</view>
        </view>
        <text class="price">¥{{ item.price }}</text>
      </view>
      <view v-if="!detail.length" class="empty">暂无已完成服务</view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../../api/request'

const user = ref(uni.getStorageSync('user') || {})
const income = ref({})
const detail = ref([])

const totalHours = computed(() => {
  const minutes = detail.value.reduce((sum, item) => sum + (Number(item.duration) || 0), 0)
  return (minutes / 60).toFixed(1)
})

onMounted(async () => {
  try {
    const me = await api.me()
    user.value = me.user || user.value
  } catch (e) {
    // 已提示
  }
  income.value = (await api.staffIncome()) || {}
  detail.value = (await api.staffIncomeDetail()) || []
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
.stat-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; margin-bottom: 24rpx; }
.stat-card { background: #fff; border-radius: 18rpx; padding: 24rpx; text-align: center; }
.stat-value { font-size: 36rpx; font-weight: 700; color: #ff5c8a; }
.detail-row { padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.service-name { font-weight: 600; font-size: 28rpx; }
</style>
