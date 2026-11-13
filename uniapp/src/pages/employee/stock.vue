<template>
  <view class="container">
    <view class="card">
      <view class="row">
        <view class="section-title">商品库存</view>
        <view class="muted">{{ skus.length }} 条</view>
      </view>
      <view class="search-row">
        <input v-model="keyword" class="search-input" placeholder="SKU 编码 / 扫码" @confirm="search" />
        <view class="btn-ghost search-btn" @click="scan">扫码</view>
        <view class="btn-primary search-btn" @click="search">查询</view>
      </view>
      <view class="muted">低于安全库存的 SKU 会标红。</view>
    </view>

    <view v-for="sku in skus" :key="sku.id" class="card sku-card" :class="{ low: sku.stock <= sku.safetyStock }">
      <view class="row">
        <text class="sku-code">{{ sku.skuCode }}</text>
        <text class="tag" v-if="sku.stock <= sku.safetyStock">低库存</text>
      </view>
      <view class="muted">库存 {{ sku.stock }} · 安全库存 {{ sku.safetyStock }} · 成本 ¥{{ sku.costPrice }} · 售价 ¥{{ sku.price }}</view>
      <view class="sku-actions">
        <input v-model="qtyMap[sku.id]" class="qty-input" type="number" placeholder="数量" />
        <view class="btn-ghost mini-btn" @click="adjust(sku, 'IN')">入库</view>
        <view class="btn-ghost mini-btn" @click="adjust(sku, 'OUT')">出库</view>
        <view class="btn-primary mini-btn" @click="adjust(sku, 'ADJUST')">盘点</view>
      </view>
    </view>
    <view v-if="!skus.length" class="card empty">未查询到 SKU</view>
  </view>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api/request'

const keyword = ref('')
const skus = ref([])
const qtyMap = reactive({})

async function search() {
  skus.value = (await api.employeeSkus(keyword.value)) || []
}

function scan() {
  uni.scanCode({
    success: (res) => {
      keyword.value = res.result || ''
      search()
    },
    fail: () => uni.showToast({ title: '扫码不可用，请手工输入', icon: 'none' })
  })
}

async function adjust(sku, type) {
  const qty = Number(qtyMap[sku.id])
  if (!Number.isFinite(qty) || qty < 0) {
    uni.showToast({ title: '请输入数量', icon: 'none' })
    return
  }
  const label = { IN: '入库', OUT: '出库', ADJUST: '盘点为' }[type]
  uni.showModal({
    title: `${label}`,
    content: `SKU ${sku.skuCode} ${label}${type === 'ADJUST' ? '实际库存' : ''} ${qty}，确认？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await api.employeeStockAdjust({ skuId: sku.id, qty, recordType: type, remark: `员工端${label}` })
        uni.showToast({ title: '操作成功', icon: 'success' })
        qtyMap[sku.id] = ''
        await search()
      } catch (e) {
        // 已提示
      }
    }
  })
}

onMounted(search)
</script>

<style scoped>
.search-row { display: flex; gap: 12rpx; margin-bottom: 12rpx; }
.search-input {
  flex: 1;
  border: 1rpx solid #e3e8f0;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
}
.search-btn { padding: 18rpx 26rpx; font-size: 24rpx; }
.sku-code { font-weight: 700; font-size: 30rpx; }
.sku-card.low { border-left: 8rpx solid #ff5c8a; }
.sku-actions { display: flex; gap: 12rpx; align-items: center; margin-top: 18rpx; }
.qty-input {
  width: 140rpx;
  border: 1rpx solid #e3e8f0;
  border-radius: 10rpx;
  padding: 12rpx 16rpx;
  font-size: 26rpx;
}
.mini-btn { padding: 10rpx 22rpx; font-size: 24rpx; }
</style>
