<template>
  <div>
    <section class="page-hero">
      <div class="container">
        <span class="eyebrow">{{ t('navNews') }}</span>
        <h1>{{ t('sectionNews') }}</h1>
        <p>{{ t('sectionNewsDesc') }}</p>
      </div>
    </section>
    <section class="section">
      <div class="container">
        <div v-if="loading" class="skeleton-grid"><div v-for="n in 4" :key="n" class="skeleton-card"></div></div>
        <div v-else class="news-list news-list--page">
          <article v-for="item in articles" :key="item.id" class="news-row">
            <SmartImage :src="item.cover" :alt="pick(item, 'title', 'titleEn')" ratio="3 / 2" />
            <div>
              <span class="tag">{{ item.category }}</span>
              <h3><RouterLink :to="`/news/${item.id}`">{{ pick(item, 'title', 'titleEn') }}</RouterLink></h3>
              <p>{{ item.seoDesc || stripHtml(pick(item, 'content', 'contentEn')).slice(0, 120) }}</p>
              <span class="muted">{{ t('publishedAt') }} {{ formatDate(item.publishedAt) }}</span>
            </div>
          </article>
        </div>
        <div v-if="!loading && !articles.length" class="empty">{{ t('empty') }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import SmartImage from '../components/SmartImage.vue'
import { api } from '../api'
import { pick, t } from '../i18n'

const articles = ref([])
const loading = ref(true)

function stripHtml(value) {
  return (value || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}
function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

onMounted(async () => {
  try {
    articles.value = await api.articles()
  } finally {
    loading.value = false
  }
})
</script>
