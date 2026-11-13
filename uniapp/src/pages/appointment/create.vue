<template>
  <view class="container">
    <view class="card">
      <view class="section-title">预约信息</view>
      <view class="field">
        <text class="label">服务项目</text>
        <picker :range="serviceNames" :value="serviceIndex" @change="onService">
          <view class="picker">{{ serviceNames[serviceIndex] || '请选择服务' }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">预约门店</text>
        <picker :range="storeNames" :value="storeIndex" @change="onStore">
          <view class="picker">{{ storeNames[storeIndex] || '请选择门店' }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">预约日期</text>
        <picker mode="date" :value="form.appointDate" @change="(e) => (form.appointDate = e.detail.value)">
          <view class="picker">{{ form.appointDate || '请选择日期' }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">预约时段</text>
        <picker :range="slots" :value="slotIndex" @change="onSlot">
          <view class="picker">{{ form.timeSlot || '请选择时段' }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">联系人</text>
        <input v-model="form.contact" class="input" placeholder="请输入联系人" />
      </view>
      <view class="field">
        <text class="label">联系电话</text>
        <input v-model="form.phone" class="input" type="number" placeholder="请输入手机号" />
      </view>
      <view class="field">
        <text class="label">服务方式</text>
        <picker :range="['到店服务', '上门服务']" @change="onType">
          <view class="picker">{{ form.appointType === 'HOME' ? '上门服务' : '到店服务' }}</view>
        </picker>
      </view>
      <view v-if="form.appointType === 'HOME'" class="field">
        <text class="label">上门地址</text>
        <input v-model="form.address" class="input" placeholder="请输入详细地址" />
      </view>
      <view class="btn-primary" style="margin-top: 24rpx" @click="submit">提交预约</view>
    </view>

    <view class="card">
      <view class="section-title">我的预约</view>
      <view v-for="item in mine" :key="item.id" class="appointment-item">
        <view class="row">
          <text>{{ item.appointDate }} {{ item.timeSlot }}</text>
          <text class="tag">{{ item.status }}</text>
        </view>
        <view class="muted">单号：{{ item.appointmentNo }}</view>
      </view>
      <view v-if="!mine.length" class="empty">登录后可查看预约记录</view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../../api/request'

const services = ref([])
const stores = ref([])
const mine = ref([])
const serviceIndex = ref(0)
const storeIndex = ref(0)
const slotIndex = ref(0)
const slots = ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']
const form = reactive({
  serviceId: null,
  storeId: null,
  appointDate: '',
  timeSlot: '',
  contact: '',
  phone: '',
  appointType: 'STORE',
  address: '',
  remark: ''
})

const serviceNames = computed(() => services.value.map((item) => `${item.name}（¥${item.price}）`))
const storeNames = computed(() => stores.value.map((item) => item.name))

function onService(e) {
  serviceIndex.value = Number(e.detail.value)
  form.serviceId = services.value[serviceIndex.value]?.id
}
function onStore(e) {
  storeIndex.value = Number(e.detail.value)
  form.storeId = stores.value[storeIndex.value]?.id
}
function onSlot(e) {
  slotIndex.value = Number(e.detail.value)
  form.timeSlot = slots[slotIndex.value]
}
function onType(e) {
  form.appointType = Number(e.detail.value) === 1 ? 'HOME' : 'STORE'
}

async function submit() {
  if (!uni.getStorageSync('token')) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  if (!form.serviceId || !form.appointDate || !form.timeSlot) {
    uni.showToast({ title: '请完善预约信息', icon: 'none' })
    return
  }
  await api.createAppointment({ ...form, idempotencyKey: `appt_${Date.now()}` })
  uni.showToast({ title: '预约成功', icon: 'success' })
  loadMine()
}

async function loadMine() {
  if (!uni.getStorageSync('token')) return
  mine.value = (await api.myAppointments()) || []
}

onMounted(async () => {
  services.value = (await api.services()) || []
  stores.value = (await api.stores()) || []
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const serviceId = current?.options?.serviceId
  if (serviceId) {
    const index = services.value.findIndex((item) => String(item.id) === String(serviceId))
    if (index >= 0) {
      serviceIndex.value = index
      form.serviceId = services.value[index].id
    }
  }
  loadMine()
})
</script>

<style scoped>
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
.picker,
.input {
  text-align: right;
  color: #1f2d3d;
  font-size: 28rpx;
  min-width: 320rpx;
}
.appointment-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f4f8;
}
</style>
