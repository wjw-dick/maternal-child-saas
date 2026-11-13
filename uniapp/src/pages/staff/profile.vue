<template>
  <view class="container">
    <view class="hero-card">
      <view class="hero-title">{{ user.nickname || user.username }}</view>
      <view class="hero-sub">{{ staff.name || '护理师' }} · {{ staff.level || '' }}</view>
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
      <view class="section-title">服务协议</view>
      <view class="agreement">
        1. 护理师须按预约时间到达，并在服务前完成 GPS 签到与现场拍照。
        2. 服务过程须如实记录宝宝体温、体重与护理情况，不得伪造。
        3. 服务完成后须由客户电子签名确认，签名记录随工单归档。
        4. 客户信息仅限本次服务使用，不得外传或用于其他用途。
      </view>
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

const user = ref(uni.getStorageSync('user') || {})
const staff = ref({})
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
    const me = await api.me()
    user.value = me.user || user.value
  } catch (e) {
    // 已提示
  }
  try {
    const data = await api.staffProfile()
    staff.value = data.staff || {}
  } catch (e) {
    // 忽略
  }
  notices.value = (await api.staffNotices()) || []
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
.notice-row { padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.notice-title { font-weight: 600; font-size: 28rpx; }
.notice-content { margin-top: 8rpx; font-size: 26rpx; color: #4a5a70; }
.agreement { font-size: 26rpx; line-height: 1.7; color: #4a5a70; }
.form-input {
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}
</style>
