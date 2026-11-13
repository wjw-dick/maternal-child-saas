<template>
  <view class="container">
    <view class="hero-card">
      <view class="hero-title">{{ info.name || '渠道商' }}</view>
      <view class="hero-sub">{{ info.code }} · 佣金规则 {{ info.commissionRule || '默认 10%' }}</view>
    </view>

    <view class="card">
      <view class="section-title">我的下级</view>
      <view v-for="item in subordinates" :key="item.id" class="row sub-row">
        <view>
          <view class="sub-name">{{ item.name }}</view>
          <view class="muted">{{ item.code }} · {{ item.contact || '-' }}</view>
        </view>
        <text class="tag">{{ item.status }}</text>
      </view>
      <view v-if="!subordinates.length" class="empty">暂无下级渠道商</view>
    </view>

    <view class="card">
      <view class="section-title">结算记录</view>
      <view v-for="item in settlements" :key="item.id" class="row sub-row">
        <view>
          <view class="sub-name">{{ item.settlementNo }}</view>
          <view class="muted">{{ item.period }} · {{ item.status }}</view>
        </view>
        <text class="price">¥{{ item.amount }}</text>
      </view>
      <view v-if="!settlements.length" class="empty">暂无结算记录</view>
    </view>

    <view class="card">
      <view class="section-title">消息 / 平台公告</view>
      <view v-for="n in notices" :key="n.id" class="notice-row">
        <view class="notice-title">{{ n.title }}</view>
        <view class="muted">{{ n.publishedAt }}</view>
        <view class="notice-content">{{ n.content }}</view>
      </view>
      <view v-if="!notices.length" class="empty">暂无公告</view>
    </view>

    <view class="card">
      <view class="section-title">修改密码</view>
      <input v-model="oldPassword" class="form-input" password placeholder="原密码" />
      <input v-model="newPassword" class="form-input" password placeholder="新密码（至少 6 位）" />
      <input v-model="confirmPassword" class="form-input" password placeholder="确认新密码" />
      <view class="btn-primary" @click="changePassword">保存新密码</view>
    </view>

    <view class="card">
      <view class="btn-ghost" @click="logout">退出登录</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api, clearSession } from '../../api/request'

const info = ref({})
const subordinates = ref([])
const settlements = ref([])
const notices = ref([])
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

async function changePassword() {
  if (!oldPassword.value || !newPassword.value) {
    uni.showToast({ title: '请填写原密码与新密码', icon: 'none' })
    return
  }
  if (newPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少 6 位', icon: 'none' })
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次输入不一致', icon: 'none' })
    return
  }
  try {
    await api.changePassword(oldPassword.value, newPassword.value)
    uni.showToast({ title: '密码已修改，请重新登录', icon: 'success' })
    setTimeout(() => {
      clearSession()
      uni.reLaunch({ url: '/pages/login/login' })
    }, 1200)
  } catch (e) {
    // 已提示
  }
}

function logout() {
  clearSession()
  uni.reLaunch({ url: '/pages/login/login' })
}

onMounted(async () => {
  try {
    const data = await api.distributorProfile()
    info.value = data.distributor || {}
  } catch (e) {
    // 已提示
  }
  subordinates.value = (await api.distributorSubordinates()) || []
  settlements.value = (await api.distributorSettlements()) || []
  notices.value = (await api.distributorNotices()) || []
})
</script>

<style scoped>
.hero-card { background: linear-gradient(135deg, #6b4b8a, #3d2b52); border-radius: 24rpx; padding: 36rpx; color: #fff; margin-bottom: 24rpx; }
.hero-title { font-size: 36rpx; font-weight: 700; }
.hero-sub { margin-top: 8rpx; font-size: 24rpx; opacity: 0.78; }
.sub-row { padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.sub-name { font-weight: 600; font-size: 28rpx; }
.notice-row { padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.notice-title { font-weight: 600; font-size: 28rpx; }
.notice-content { margin-top: 8rpx; font-size: 26rpx; color: #4a5a70; }
.form-input { border: 1rpx solid #e3e8f0; border-radius: 12rpx; padding: 18rpx 20rpx; font-size: 28rpx; margin-bottom: 14rpx; }
</style>
