<template>
  <view class="container">
    <view class="card">
      <view class="section-title">{{ article.title || title }}</view>
      <view class="muted" style="margin-bottom: 20rpx">{{ fmt(article.publishedAt) }}</view>
      <rich-text v-if="article.content" :nodes="article.content"></rich-text>
      <view v-else class="empty">正文内容加载中…</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api/request'

const article = ref({})
const title = ref('')

function fmt(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

onMounted(async () => {
  const pages = getCurrentPages()
  const options = pages[pages.length - 1]?.options || {}
  title.value = decodeURIComponent(options.title || '')
  if (options.id) {
    article.value = (await api.article(options.id)) || {}
  }
})
</script>
