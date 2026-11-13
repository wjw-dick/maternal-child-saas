<template>
  <view class="container">
    <view class="card">
      <view class="row">
        <view class="section-title">会员查询 / 开卡</view>
        <view class="muted">{{ members.length }} 条</view>
      </view>
      <view class="search-row">
        <input v-model="keyword" class="search-input" placeholder="会员编号 / 姓名 / 手机号" @confirm="search" />
        <view class="btn-primary search-btn" @click="search">查询</view>
      </view>
      <view class="btn-ghost" @click="showOpen = !showOpen">{{ showOpen ? '收起开卡表单' : '为新会员开卡' }}</view>

      <view v-if="showOpen" class="open-form">
        <input v-model="form.name" class="form-input" placeholder="会员姓名（必填）" />
        <input v-model="form.phone" class="form-input" type="number" maxlength="11" placeholder="手机号（必填）" />
        <input v-model="form.babyName" class="form-input" placeholder="宝宝姓名（选填）" />
        <input v-model="form.babyBirthday" class="form-input" placeholder="宝宝生日 YYYY-MM-DD（选填）" />
        <input v-model="form.remark" class="form-input" placeholder="备注（选填）" />
        <view class="btn-primary" @click="openCard">确认开卡</view>
      </view>
    </view>

    <view v-for="m in members" :key="m.id" class="card">
      <view class="row" @click="selectMember(m)">
        <text class="member-name">{{ m.name }}</text>
        <text class="tag">{{ m.memberNo }}</text>
      </view>
      <view class="muted">{{ maskPhone(m.phone) }} · 余额 ¥{{ m.balance || 0 }} · 积分 {{ m.points || 0 }}</view>
      <view v-if="current && current.member && current.member.id === m.id" class="wallet-box">
        <view class="wallet-balance">
          <view class="muted">储值余额</view>
          <view class="balance-value">¥{{ current.wallet.balance || 0 }}</view>
          <view class="muted">累计充值 ¥{{ current.wallet.totalRecharge || 0 }} · 累计消费 ¥{{ current.wallet.totalConsume || 0 }}</view>
        </view>
        <input v-model="amount" class="form-input" type="digit" placeholder="金额，如 100" />
        <view class="op-row">
          <view class="btn-primary op-btn" @click="recharge">充值</view>
          <view class="btn-ghost op-btn" @click="consume">消费</view>
        </view>
        <view class="section-title small">最近流水</view>
        <view v-for="f in current.flows || []" :key="f.id" class="flow-row">
          <text>{{ f.flowType === 'RECHARGE' ? '充值' : '消费' }} · {{ f.flowNo }}</text>
          <text class="price">¥{{ f.amount }}</text>
        </view>
        <view v-if="!(current.flows || []).length" class="empty">暂无流水</view>
      </view>
    </view>
    <view v-if="!members.length" class="card empty">暂无会员，输入手机号后可直接开卡</view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/request'

const keyword = ref('')
const members = ref([])
const current = ref(null)
const amount = ref('')
const showOpen = ref(false)
const form = ref({ name: '', phone: '', babyName: '', babyBirthday: '', remark: '' })

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '-'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

async function search() {
  members.value = (await api.employeeMembers(keyword.value)) || []
}

async function openCard() {
  if (!form.value.name || !form.value.phone) {
    uni.showToast({ title: '请填写姓名和手机号', icon: 'none' })
    return
  }
  try {
    const res = await api.employeeOpenCard(form.value)
    uni.showToast({ title: '开卡成功', icon: 'success' })
    showOpen.value = false
    form.value = { name: '', phone: '', babyName: '', babyBirthday: '', remark: '' }
    current.value = { member: res.member, wallet: res.wallet, flows: [] }
    await search()
  } catch (e) {
    // 已提示
  }
}

async function selectMember(m) {
  current.value = await api.employeeWallet(m.id)
}

async function recharge() {
  if (!current.value || !amount.value) {
    uni.showToast({ title: '请选择会员并输入金额', icon: 'none' })
    return
  }
  try {
    await api.employeeRecharge(current.value.member.id, amount.value)
    uni.showToast({ title: '充值成功', icon: 'success' })
    amount.value = ''
    await selectMember(current.value.member)
    await search()
  } catch (e) {
    // 已提示
  }
}

async function consume() {
  if (!current.value || !amount.value) {
    uni.showToast({ title: '请选择会员并输入金额', icon: 'none' })
    return
  }
  try {
    await api.employeeConsume(current.value.member.id, amount.value)
    uni.showToast({ title: '扣款成功', icon: 'success' })
    amount.value = ''
    await selectMember(current.value.member)
    await search()
  } catch (e) {
    // 已提示
  }
}

onMounted(search)
</script>

<style scoped>
.search-row { display: flex; gap: 16rpx; margin-bottom: 16rpx; }
.search-input {
  flex: 1;
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
}
.search-btn { padding: 18rpx 36rpx; font-size: 26rpx; }
.open-form { margin-top: 20rpx; }
.form-input {
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}
.member-name { font-weight: 700; font-size: 30rpx; }
.wallet-box { margin-top: 20rpx; border-top: 1rpx solid #f2f4f8; padding-top: 20rpx; }
.wallet-balance { text-align: center; margin-bottom: 16rpx; }
.balance-value { font-size: 44rpx; font-weight: 700; color: #ff5c8a; margin: 8rpx 0; }
.op-row { display: flex; gap: 16rpx; }
.op-btn { flex: 1; padding: 20rpx 0; font-size: 28rpx; }
.small { font-size: 26rpx; margin-top: 24rpx; }
.flow-row {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f7f9fc;
  font-size: 26rpx;
}
</style>
