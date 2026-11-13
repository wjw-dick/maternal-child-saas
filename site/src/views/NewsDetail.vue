<template>
  <section class="section detail-section">
    <div class="container article-container">
      <RouterLink to="/news" class="back-link">← {{ t('back') }}</RouterLink>
      <article v-if="article" class="article">
        <span class="tag">{{ article.category }}</span>
        <h1>{{ pick(article, 'title', 'titleEn') }}</h1>
        <div class="article-meta">
          <span>{{ t('publishedAt') }} {{ formatDate(article.publishedAt) }}</span>
          <span>{{ article.seoKeywords }}</span>
        </div>
        <SmartImage v-if="article.cover" :src="article.cover" :alt="pick(article, 'title', 'titleEn')" ratio="16 / 8" />
        <div class="article-content" v-html="pick(article, 'content', 'contentEn')"></div>
      </article>
      <div v-else class="empty">{{ loading ? '…' : t('empty') }}</div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import SmartImage from '../components/SmartImage.vue'
import { api } from '../api'
import { pick, t } from '../i18n'

const route = useRoute()
const article = ref(null)
const loading = ref(true)

function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

async function load() {
  loading.value = true
  try {
    article.value = await api.article(route.params.id)
  } catch {
    article.value = null
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>
