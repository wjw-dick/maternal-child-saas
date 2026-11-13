<template>
  <view class="container">
    <view class="card" v-for="item in services" :key="item.id">
      <view class="row">
        <view>
          <view class="service-name">{{ item.name }}</view>
          <view class="muted">{{ item.category || '母婴护理' }} · {{ item.duration || 60 }} 分钟 · {{ item.ageRange || '全年龄' }}</view>
        </view>
        <view class="price">¥{{ item.price }}</view>
      </view>
      <view class="desc">{{ item.description || '专业护理团队提供标准化服务，支持到店与上门。' }}</view>
      <view class="op-row">
        <view class="btn-primary op-btn" @click="book(item)">立即预约</view>
        <view class="btn-ghost op-btn" @click="share(item)">分享</view>
      </view>
    </view>
    <view v-if="!services.length" class="empty">暂无服务项目</view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/request'

const services = ref([])

function book(item) {
  uni.navigateTo({ url: `/pages/appointment/create?serviceId=${item.id}` })
}

function share(item) {
  // #ifdef H5
  const url = `${window.location.origin}${window.location.pathname}?source=share&ref=service-${item.id}`
  uni.setClipboardData({
    data: url,
    success: () => uni.showToast({ title: '分享链接已复制', icon: 'success' })
  })
  // #endif
  // #ifndef H5
  uni.showToast({ title: '请使用右上角转发', icon: 'none' })
  // #endif
}

onMounted(async () => {
  services.value = (await api.services()) || []
})
</script>

<style scoped>
.service-name {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}
.desc {
  color: #6b7a90;
  font-size: 26rpx;
  line-height: 1.7;
  margin: 20rpx 0;
}
.op-row {
  display: flex;
  gap: 16rpx;
}
.op-btn {
  flex: 1;
  padding: 20rpx 0;
}
</style>
