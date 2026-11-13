<template>
  <view class="container">
    <view class="hero-card">
      <view class="hero-title">{{ user.nickname || user.username }}</view>
      <view class="hero-sub">{{ user.userType }} · {{ user.username }}</view>
    </view>

    <view class="card">
      <view class="section-title">修改密码</view>
      <input v-model="oldPassword" class="form-input" password placeholder="原密码" />
      <input v-model="newPassword" class="form-input" password placeholder="新密码（至少 6 位）" />
      <input v-model="confirmPassword" class="form-input" password placeholder="确认新密码" />
      <view class="btn-primary" @click="changePassword">保存新密码</view>
    </view>

    <view class="card">
      <view class="section-title">账号</view>
      <view class="row info-row">
        <text class="muted">账号</text>
        <text>{{ user.username }}</text>
      </view>
      <view class="row info-row">
        <text class="muted">角色</text>
        <text>{{ roleText }}</text>
      </view>
      <view class="btn-ghost" @click="logout">退出登录</view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api, clearSession } from '../../api/request'

const user = ref(uni.getStorageSync('user') || {})
const roleCode = ref('')
const roleName = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const roleText = computed(() => {
  if (roleName.value) return roleName.value
  const map = {
    FRONT_DESK: '前台 / 店员',
    STORE_MANAGER: '店长',
    FINANCE: '财务',
    WAREHOUSE: '库管',
    HR: '人事'
  }
  return map[roleCode.value] || roleCode.value || '员工'
})

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
    roleCode.value = me.roleCode || ''
    roleName.value = me.roleName || ''
    uni.setStorageSync('user', user.value)
  } catch (e) {
    // 已提示
  }
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
.form-input {
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}
.info-row { padding: 14rpx 0; border-bottom: 1rpx solid #f7f9fc; font-size: 28rpx; }
.info-row:last-of-type { margin-bottom: 18rpx; }
</style>
