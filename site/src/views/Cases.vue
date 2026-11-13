<template>
  <div>
    <section class="page-hero">
      <div class="container">
        <span class="eyebrow">{{ t('navCases') }}</span>
        <h1>{{ t('sectionCases') }}</h1>
        <p>{{ t('sectionCasesDesc') }}</p>
      </div>
    </section>
    <section class="section">
      <div class="container">
        <div v-if="loading" class="skeleton-grid"><div v-for="n in 4" :key="n" class="skeleton-card"></div></div>
        <div v-else class="card-grid card-grid--2">
          <article v-for="item in cases" :key="item.id" class="case-card">
            <SmartImage :src="item.cover" :alt="pick(item, 'title', 'titleEn')" ratio="16 / 9" />
            <div class="card-body">
              <h3>{{ pick(item, 'title', 'titleEn') }}</h3>
              <p>{{ stripHtml(pick(item, 'content', 'contentEn')) }}</p>
            </div>
          </article>
        </div>
        <div v-if="!loading && !cases.length" class="empty">{{ t('empty') }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import SmartImage from '../components/SmartImage.vue'
import { api } from '../api'
import { pick, t } from '../i18n'

const cases = ref([])
const loading = ref(true)

function stripHtml(value) {
  return (value || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

onMounted(async () => {
  try {
    cases.value = await api.cases()
  } finally {
    loading.value = false
  }
})
</script>
