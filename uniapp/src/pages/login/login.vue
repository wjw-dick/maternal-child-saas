<template>
  <view class="container">
    <view class="login-hero">
      <view class="login-badge">悦宝</view>
      <view class="login-title">母婴亲子数字化平台</view>
      <view class="login-sub">会员 / 护理师 / 供应商 / 渠道商统一入口</view>
    </view>

    <view class="card">
      <view class="tabs">
        <view :class="['tab', mode === 'member' ? 'active' : '']" @click="mode = 'member'">会员微信登录</view>
        <view :class="['tab', mode === 'staff' ? 'active' : '']" @click="mode = 'staff'">员工账号登录</view>
      </view>

      <template v-if="mode === 'member'">
        <view class="muted" style="margin: 20rpx 0 24rpx">
          H5/小程序环境将自动获取微信授权 code；当前演示环境使用本地模拟 code。
        </view>
        <view class="field">
          <text class="label">租户编码</text>
          <input v-model="tenantCode" class="input" placeholder="demo" />
        </view>
        <view class="btn-primary" style="margin-top: 28rpx" @click="memberLogin">微信一键登录</view>
      </template>

      <template v-else>
        <view class="field">
          <text class="label">租户编码</text>
          <input v-model="tenantCode" class="input" placeholder="demo" />
        </view>
        <view class="field">
          <text class="label">账号</text>
          <input v-model="form.username" class="input" placeholder="nurse / supplier01 / dist01" />
        </view>
        <view class="field">
          <text class="label">密码</text>
          <input v-model="form.password" class="input" password placeholder="admin123" />
        </view>
        <view class="btn-primary" style="margin-top: 28rpx" @click="staffLogin">账号登录</view>
      </template>

      <view class="muted" style="margin-top: 24rpx; text-align: center">
        演示账号：admin / nurse / supplier01 / dist01，密码 admin123
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api, setSession } from '../../api/request'

const mode = ref('member')
const tenantCode = ref(uni.getStorageSync('tenantCode') || 'demo')
const form = reactive({ username: 'nurse', password: 'admin123' })

async function memberLogin() {
  uni.setStorageSync('tenantCode', tenantCode.value || 'demo')
  const code = uni.getStorageSync('wxCode') || `h5_${Date.now()}`
  const unionid = uni.getStorageSync('unionid') || `unionid_h5_${Date.now()}`
  uni.setStorageSync('unionid', unionid)
  const data = await api.miniLogin({ code, tenantCode: tenantCode.value || 'demo', unionid })
  setSession(data)
  uni.showToast({ title: '登录成功', icon: 'success' })
  setTimeout(() => uni.switchTab({ url: '/pages/mine/index' }), 500)
}

async function staffLogin() {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' })
    return
  }
  uni.setStorageSync('tenantCode', tenantCode.value || 'demo')
  const data = await api.staffLogin({
    username: form.username,
    password: form.password,
    tenantCode: tenantCode.value || 'demo'
  })
  setSession(data)
  uni.showToast({ title: '登录成功', icon: 'success' })
  setTimeout(() => uni.navigateTo({ url: '/pages/workbench/index' }), 500)
}
</script>

<style scoped>
.login-hero {
  text-align: center;
  padding: 60rpx 0 40rpx;
}
.login-badge {
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto 22rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #ff8a65, #ff5c8a);
  color: #fff;
  font-size: 44rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(255, 92, 138, 0.3);
}
.login-title {
  font-size: 40rpx;
  font-weight: 700;
}
.login-sub {
  color: #8a94a6;
  font-size: 26rpx;
  margin-top: 10rpx;
}
.tabs {
  display: flex;
  background: #f5f7fa;
  border-radius: 999rpx;
  padding: 6rpx;
  margin-bottom: 18rpx;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 999rpx;
  color: #7a8699;
  font-size: 26rpx;
}
.tab.active {
  background: #fff;
  color: #ff5c8a;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(31, 45, 61, 0.08);
}
.field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
}
.label {
  color: #56637a;
  font-size: 28rpx;
}
.input {
  text-align: right;
  font-size: 28rpx;
  min-width: 320rpx;
}
</style>
