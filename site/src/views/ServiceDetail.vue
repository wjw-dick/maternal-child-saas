<template>
  <section class="section detail-section">
    <div class="container">
      <RouterLink to="/services" class="back-link">← {{ t('back') }}</RouterLink>
      <div v-if="service" class="detail-grid">
        <SmartImage :src="service.cover" :alt="service.name" ratio="4 / 3" />
        <div class="detail-copy">
          <span class="tag">{{ service.category }}</span>
          <h1>{{ service.name }}</h1>
          <p class="detail-desc">{{ service.description }}</p>
          <div class="detail-facts">
            <div><strong>¥{{ Number(service.price || 0).toFixed(0) }}</strong><span>{{ t('price') }}</span></div>
            <div><strong>{{ service.duration }}</strong><span>{{ t('duration') }}</span></div>
            <div><strong>{{ service.ageRange }}</strong><span>Age</span></div>
          </div>
          <RouterLink :to="{ path: '/contact', query: { service: service.name } }" class="primary-btn">{{ t('bookService') }}</RouterLink>
        </div>
      </div>
      <div v-else class="empty">{{ loading ? '…' : t('empty') }}</div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import SmartImage from '../components/SmartImage.vue'
import { api } from '../api'
import { t } from '../i18n'

const route = useRoute()
const service = ref(null)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    service.value = await api.service(route.params.id)
  } catch {
    service.value = null
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>
