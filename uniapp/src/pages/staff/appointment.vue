<template>
  <view class="container">
    <view class="card" v-if="detail.appointment">
      <view class="row">
        <view class="section-title">{{ detail.service.name || '服务工单' }}</view>
        <text class="tag">{{ statusText(detail.appointment.status) }}</text>
      </view>
      <view class="muted">{{ detail.appointment.appointmentNo }} · {{ detail.appointment.appointDate }} {{ detail.appointment.timeSlot }}</view>
      <view class="muted">{{ detail.appointment.appointType === 'HOME' ? '上门服务' : '到店服务' }} · {{ detail.appointment.address || '门店' }}</view>
      <view class="info-line">客户：{{ detail.member.name || detail.appointment.contact }} · {{ maskPhone(detail.appointment.phone || detail.member.phone) }}</view>
      <view class="info-line" v-if="detail.baby.name">宝宝：{{ detail.baby.name }}（{{ detail.baby.birthday || '生日未填' }}）</view>
      <view class="info-line" v-if="detail.baby.allergy">过敏：{{ detail.baby.allergy }}</view>
      <view class="info-line" v-if="detail.service.price">服务金额：¥{{ detail.service.price }} · {{ detail.service.duration }} 分钟</view>
    </view>

    <view class="card">
      <view class="row">
        <view class="section-title">GPS 签到</view>
        <text class="tag" v-if="checkinDone">已签到</text>
      </view>
      <view class="muted">签到将记录经纬度、地址与现场照片，并进入服务中状态。</view>
      <input v-model="address" class="form-input" placeholder="服务地址 / 现场定位说明" />
      <view class="muted" v-if="coords">定位：{{ coords.lat }}, {{ coords.lng }}</view>
      <view class="op-row">
        <view class="btn-ghost op-btn" @click="locate">获取定位</view>
        <view class="btn-primary op-btn" @click="choosePhoto">现场拍照</view>
      </view>
      <view v-if="photoUrl" class="photo-box">
        <image :src="photoUrl" mode="aspectFill" class="photo" />
        <text class="muted">已上传</text>
      </view>
      <view class="btn-primary" style="margin-top: 20rpx" @click="doCheckin">提交签到</view>
    </view>

    <view class="card">
      <view class="section-title">宝宝体征 / 护理记录</view>
      <view class="op-row">
        <input v-model="record.babyTemp" class="form-input half" type="digit" placeholder="体温 ℃" />
        <input v-model="record.babyWeight" class="form-input half" type="digit" placeholder="体重 kg" />
      </view>
      <textarea v-model="record.content" class="textarea" placeholder="护理过程、喂养、睡眠、异常情况等" />
      <view class="op-row">
        <view class="btn-ghost op-btn" @click="chooseRecordImages">上传记录照片</view>
        <view class="muted">已选 {{ recordImages.length }} 张</view>
      </view>
      <view class="thumb-row" v-if="recordImages.length">
        <image v-for="(img, i) in recordImages" :key="img" :src="img" mode="aspectFill" class="thumb" />
      </view>
      <view class="btn-primary" style="margin-top: 20rpx" @click="saveRecord">保存护理记录</view>
    </view>

    <view class="card">
      <view class="section-title">客户电子签名</view>
      <canvas canvas-id="sign" id="sign" class="sign-canvas" @touchstart="startSign" @touchmove="moveSign" @touchend="endSign"></canvas>
      <view class="op-row" style="margin-top: 16rpx">
        <view class="btn-ghost op-btn" @click="clearSign">清除</view>
        <view class="btn-ghost op-btn" @click="saveSign">保存签名</view>
      </view>
      <view class="muted" v-if="signUrl">签名已上传</view>
      <view class="btn-primary" style="margin-top: 20rpx" @click="complete">完成服务并确认签名</view>
    </view>

    <view class="card" v-if="detail.records && detail.records.length">
      <view class="section-title">历史记录</view>
      <view v-for="r in detail.records" :key="r.id" class="record-row">
        <view class="muted">{{ r.createdAt }}</view>
        <view>{{ r.content }}</view>
        <view class="muted" v-if="r.babyTemp || r.babyWeight">体温 {{ r.babyTemp || '-' }} ℃ · 体重 {{ r.babyWeight || '-' }} kg</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api, getToken, getTenantCode } from '../../api/request'

const detail = reactive({ appointment: {}, member: {}, baby: {}, service: {}, records: [], checkins: [] })
const address = ref('')
const coords = ref(null)
const photoUrl = ref('')
const checkinDone = ref(false)
const record = reactive({ babyTemp: '', babyWeight: '', content: '' })
const recordImages = ref([])
const signUrl = ref('')
let appointmentId = ''
let ctx = null
let drawing = false
let hasInk = false

function statusText(s) {
  return { PENDING: '待接单', CONFIRMED: '待服务', SERVING: '服务中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
}
function maskPhone(p) {
  if (!p || p.length < 7) return p || '-'
  return p.slice(0, 3) + '****' + p.slice(-4)
}

async function load() {
  detail.appointment = {}
  const id = uni.getStorageSync('staffAppointmentId')
  if (!id) {
    uni.showToast({ title: '缺少工单参数', icon: 'none' })
    return
  }
  appointmentId = id
  const data = await api.staffAppointment(id)
  Object.assign(detail, data || {})
  address.value = detail.appointment.address || ''
  if (detail.checkins && detail.checkins.length) {
    checkinDone.value = true
    photoUrl.value = detail.checkins[0].photoUrl || ''
    if (detail.checkins[0].lng) coords.value = { lng: detail.checkins[0].lng, lat: detail.checkins[0].lat }
  }
}

function locate() {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      coords.value = { lng: res.longitude.toFixed(6), lat: res.latitude.toFixed(6) }
      uni.showToast({ title: '定位成功', icon: 'success' })
    },
    fail: () => uni.showToast({ title: '定位失败，请检查权限', icon: 'none' })
  })
}

async function upload(path) {
  const res = await new Promise((resolve, reject) => {
    uni.uploadFile({
      url: '/api/staff/files',
      filePath: path,
      name: 'file',
      formData: { bizType: 'staff-service' },
      header: { Authorization: `Bearer ${getToken()}`, 'X-Tenant-Code': getTenantCode() },
      success: resolve,
      fail: reject
    })
  })
  const body = JSON.parse(res.data || '{}')
  if (body.code !== 0) throw new Error(body.msg || '上传失败')
  return body.data.url
}

function choosePhoto() {
  uni.chooseImage({
    count: 1,
    success: async (r) => {
      try {
        photoUrl.value = await upload(r.tempFilePaths[0])
        uni.showToast({ title: '照片已上传', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}

function chooseRecordImages() {
  uni.chooseImage({
    count: 3,
    success: async (r) => {
      for (const path of r.tempFilePaths) {
        try {
          recordImages.value.push(await upload(path))
        } catch (e) {
          // 忽略单张失败
        }
      }
    }
  })
}

async function doCheckin() {
  if (!coords.value) {
    uni.showToast({ title: '请先获取定位', icon: 'none' })
    return
  }
  try {
    await api.staffCheckin(appointmentId, {
      lng: Number(coords.value.lng),
      lat: Number(coords.value.lat),
      address: address.value,
      photoUrl: photoUrl.value,
      checkinType: 'GPS'
    })
    checkinDone.value = true
    uni.showToast({ title: '签到成功', icon: 'success' })
    load()
  } catch (e) {
    // 已提示
  }
}

async function saveRecord() {
  if (!record.content && !record.babyTemp && !record.babyWeight) {
    uni.showToast({ title: '请填写护理记录', icon: 'none' })
    return
  }
  try {
    await api.staffRecord(appointmentId, {
      content: record.content,
      babyTemp: record.babyTemp ? Number(record.babyTemp) : null,
      babyWeight: record.babyWeight ? Number(record.babyWeight) : null,
      images: recordImages.value.join(',')
    })
    uni.showToast({ title: '记录已保存', icon: 'success' })
    record.content = ''
    recordImages.value = []
    load()
  } catch (e) {
    // 已提示
  }
}

function initCanvas() {
  ctx = uni.createCanvasContext('sign')
  ctx.setStrokeStyle('#1f2d3d')
  ctx.setLineWidth(3)
}
function startSign(e) {
  if (!ctx) initCanvas()
  drawing = true
  hasInk = true
  const t = e.touches[0]
  ctx.beginPath()
  ctx.moveTo(t.x, t.y)
}
function moveSign(e) {
  if (!drawing) return
  const t = e.touches[0]
  ctx.lineTo(t.x, t.y)
  ctx.stroke()
  ctx.draw(true)
}
function endSign() {
  drawing = false
}
function clearSign() {
  if (!ctx) initCanvas()
  ctx.clearRect(0, 0, 600, 260)
  ctx.draw()
  hasInk = false
  signUrl.value = ''
}
function saveSign() {
  if (!hasInk) {
    uni.showToast({ title: '请先签名', icon: 'none' })
    return
  }
  uni.canvasToTempFilePath({
    canvasId: 'sign',
    success: async (res) => {
      try {
        signUrl.value = await upload(res.tempFilePath)
        uni.showToast({ title: '签名已保存', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '签名上传失败', icon: 'none' })
      }
    },
    fail: () => uni.showToast({ title: '签名导出失败', icon: 'none' })
  })
}

async function complete() {
  if (!signUrl.value) {
    uni.showToast({ title: '请先保存客户签名', icon: 'none' })
    return
  }
  uni.showModal({
    title: '完成服务',
    content: '确认服务已完成且客户已签名？',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await api.staffComplete(appointmentId, signUrl.value)
        uni.showToast({ title: '服务已完成', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e) {
        // 已提示
      }
    }
  })
}

load()
</script>

<style scoped>
.form-input {
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  margin-bottom: 14rpx;
  flex: 1;
}
.half { width: 48%; }
.textarea {
  width: 100%;
  min-height: 160rpx;
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 14rpx;
}
.op-row { display: flex; gap: 16rpx; align-items: center; }
.op-btn { flex: 1; padding: 18rpx 0; font-size: 26rpx; }
.info-line { margin-top: 10rpx; font-size: 27rpx; }
.photo-box { display: flex; align-items: center; gap: 16rpx; margin-top: 14rpx; }
.photo { width: 180rpx; height: 130rpx; border-radius: 12rpx; }
.thumb-row { display: flex; gap: 12rpx; margin-top: 12rpx; }
.thumb { width: 150rpx; height: 150rpx; border-radius: 10rpx; }
.sign-canvas { width: 100%; height: 260rpx; border: 1rpx dashed #c8d1e0; border-radius: 12rpx; background: #fbfcfe; }
.record-row { padding: 16rpx 0; border-bottom: 1rpx solid #f2f4f8; font-size: 26rpx; }
</style>
