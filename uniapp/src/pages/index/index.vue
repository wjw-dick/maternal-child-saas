<template>
  <view class="container">
    <view class="hero">
      <view class="hero-text">
        <view class="hero-title">{{ home.tenant?.name || '悦宝母婴亲子' }}</view>
        <view class="hero-sub">专业护理 · 科学育儿 · 亲子成长</view>
        <view class="hero-tags">
          <text class="tag">持证护理师</text>
          <text class="tag">到店/上门</text>
          <text class="tag">会员储值</text>
        </view>
      </view>
      <view class="hero-badge">悦宝</view>
    </view>

    <view class="quick-grid">
      <view class="quick-item" @click="go('/pages/appointment/create')">
        <view class="quick-icon">预</view>
        <text>在线预约</text>
      </view>
      <view class="quick-item" @click="go('/pages/service/list')">
        <view class="quick-icon">服</view>
        <text>服务项目</text>
      </view>
      <view class="quick-item" @click="go('/pages/order/list')">
        <view class="quick-icon">单</view>
        <text>我的订单</text>
      </view>
      <view class="quick-item" @click="go('/pages/mine/index')">
        <view class="quick-icon">我</view>
        <text>会员中心</text>
      </view>
    </view>

    <view class="card">
      <view class="row">
        <text class="section-title">热门服务</text>
        <text class="muted" @click="go('/pages/service/list')">全部 ›</text>
      </view>
      <view v-if="home.services?.length" class="service-list">
        <view v-for="item in home.services" :key="item.id" class="service-item" @click="go('/pages/service/list')">
          <view class="service-avatar">{{ (item.name || '服').slice(0, 1) }}</view>
          <view class="service-info">
            <view class="service-name">{{ item.name }}</view>
            <view class="muted">{{ item.category || '母婴护理' }} · {{ item.duration || 60 }} 分钟</view>
          </view>
          <view class="price">¥{{ item.price }}</view>
        </view>
      </view>
      <view v-else class="empty">暂无服务项目</view>
    </view>

    <view class="card">
      <view class="row">
        <text class="section-title">亲子活动</text>
      </view>
      <view v-if="home.activities?.length">
        <view v-for="item in home.activities" :key="item.id" class="activity-item">
          <view>
            <view class="service-name">{{ item.title }}</view>
            <view class="muted">{{ fmt(item.startAt) }} · {{ item.address }}</view>
          </view>
          <view class="btn-ghost mini-btn" @click="signup(item)">报名</view>
        </view>
      </view>
      <view v-else class="empty">暂无活动</view>
    </view>

    <view class="card">
      <view class="row">
        <text class="section-title">育儿资讯</text>
      </view>
      <view v-for="item in home.articles" :key="item.id" class="article-item" @click="openArticle(item)">
        <view class="article-title">{{ item.title }}</view>
        <view class="muted">{{ fmt(item.publishedAt) }}</view>
      </view>
      <view v-if="!home.articles?.length" class="empty">暂无资讯</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/request'

const home = ref({})

function go(url) {
  uni.navigateTo({ url })
}

function fmt(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

async function signup(item) {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  await api.signup(item.id)
  uni.showToast({ title: '报名成功', icon: 'success' })
}

function openArticle(item) {
  uni.navigateTo({ url: `/pages/article/detail?id=${item.id}&title=${encodeURIComponent(item.title || '')}` })
}

onMounted(async () => {
  home.value = (await api.home()) || {}
})
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #ff9a76, #ff5c8a);
  border-radius: 28rpx;
  padding: 44rpx 36rpx;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 16rpx 40rpx rgba(255, 92, 138, 0.28);
}
.hero-title {
  font-size: 40rpx;
  font-weight: 700;
}
.hero-sub {
  margin-top: 12rpx;
  opacity: 0.9;
  font-size: 26rpx;
}
.hero-tags {
  margin-top: 20rpx;
}
.hero .tag {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}
.hero-badge {
  width: 110rpx;
  height: 110rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 700;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18rpx;
  margin: 26rpx 0;
}
.quick-item {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx 0;
  text-align: center;
  font-size: 24rpx;
  color: #4a5872;
  box-shadow: 0 6rpx 20rpx rgba(31, 45, 61, 0.04);
}
.quick-icon {
  width: 64rpx;
  height: 64rpx;
  margin: 0 auto 10rpx;
  border-radius: 20rpx;
  background: #fff1f5;
  color: #ff5c8a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
}
.service-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}
.service-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.service-avatar {
  width: 86rpx;
  height: 86rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #ffe1d9, #ffd1e0);
  color: #ff5c8a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
}
.service-info {
  flex: 1;
}
.service-name {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 6rpx;
}
.activity-item,
.article-item {
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
}
.article-item:last-child,
.activity-item:last-child {
  border-bottom: none;
}
.article-title {
  font-size: 28rpx;
  margin-bottom: 8rpx;
}
.mini-btn {
  padding: 10rpx 28rpx;
  font-size: 24rpx;
}
</style>
