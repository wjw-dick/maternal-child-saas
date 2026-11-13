<template>
  <view class="container">
    <view class="hero-card">
      <view class="hero-title">{{ supplier.name || '供应商' }}</view>
      <view class="hero-sub">{{ supplier.code }} · 评级 {{ supplier.rating || '-' }} · {{ supplier.status }}</view>
    </view>

    <view class="card">
      <view class="section-title">资质证件</view>
      <view v-for="q in qualifications" :key="q.id" class="qual-row">
        <view>
          <view class="qual-title">{{ q.licenseType }} · {{ q.licenseNo || '无编号' }}</view>
          <view class="muted">到期 {{ q.expireDate || '-' }} · 审核 {{ q.auditStatus }}</view>
        </view>
        <text class="link" @click="preview(q.fileUrl)">查看</text>
      </view>
      <view v-if="!qualifications.length" class="empty">暂无资质，请上传</view>
      <input v-model="form.licenseType" class="form-input" placeholder="资质类型，如食品经营许可证" />
      <input v-model="form.licenseNo" class="form-input" placeholder="证件编号" />
      <input v-model="form.expireDate" class="form-input" placeholder="到期日 YYYY-MM-DD" />
      <view class="btn-ghost" @click="chooseFile">{{ form.fileUrl ? '已选择证件，重新上传' : '上传证件照片' }}</view>
      <view class="btn-primary" style="margin-top: 16rpx" @click="saveQualification">提交资质</view>
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
import { onMounted, reactive, ref } from 'vue'
import { api, clearSession, getTenantCode, getToken } from '../../api/request'

const supplier = ref({})
const qualifications = ref([])
const notices = ref([])
const form = reactive({ licenseType: '', licenseNo: '', expireDate: '', fileUrl: '' })
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

async function upload(path) {
  const res = await new Promise((resolve, reject) => {
    uni.uploadFile({
      url: '/api/supplier/files',
      filePath: path,
      name: 'file',
      formData: { bizType: 'supplier-license' },
      header: { Authorization: `Bearer ${getToken()}`, 'X-Tenant-Code': getTenantCode() },
      success: resolve,
      fail: reject
    })
  })
  const body = JSON.parse(res.data || '{}')
  if (body.code !== 0) throw new Error(body.msg || '上传失败')
  return body.data.url
}

function chooseFile() {
  uni.chooseImage({
    count: 1,
    success: async (r) => {
      try {
        form.fileUrl = await upload(r.tempFilePaths[0])
        uni.showToast({ title: '证件已上传', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}

async function saveQualification() {
  if (!form.licenseType || !form.fileUrl) {
    uni.showToast({ title: '请填写类型并上传证件', icon: 'none' })
    return
  }
  try {
    await api.supplierAddQualification(form)
    uni.showToast({ title: '已提交审核', icon: 'success' })
    form.licenseType = ''
    form.licenseNo = ''
    form.expireDate = ''
    form.fileUrl = ''
    qualifications.value = (await api.supplierQualifications()) || []
  } catch (e) {
    // 已提示
  }
}

function preview(url) {
  if (url) uni.showToast({ title: '证件地址：' + url, icon: 'none' })
}

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
    const data = await api.supplierProfile()
    supplier.value = data.supplier || {}
  } catch (e) {
    // 已提示
  }
  qualifications.value = (await api.supplierQualifications()) || []
  notices.value = (await api.supplierNotices()) || []
})
</script>

<style scoped>
.hero-card { background: linear-gradient(135deg, #3a5a78, #1e3346); border-radius: 24rpx; padding: 36rpx; color: #fff; margin-bottom: 24rpx; }
.hero-title { font-size: 36rpx; font-weight: 700; }
.hero-sub { margin-top: 8rpx; font-size: 24rpx; opacity: 0.75; }
.qual-row { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.qual-title { font-weight: 600; font-size: 28rpx; }
.link { color: #ff5c8a; font-size: 24rpx; }
.notice-row { padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; }
.notice-title { font-weight: 600; font-size: 28rpx; }
.notice-content { margin-top: 8rpx; font-size: 26rpx; color: #4a5a70; }
.form-input { border: 1rpx solid #e3e8f0; border-radius: 12rpx; padding: 18rpx 20rpx; font-size: 28rpx; margin-bottom: 14rpx; }
</style>
