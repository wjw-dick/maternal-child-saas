<template>
  <div>
    <section class="page-hero">
      <div class="container">
        <span class="eyebrow">{{ t('navProducts') }}</span>
        <h1>{{ t('sectionProducts') }}</h1>
        <p>{{ t('sectionProductsDesc') }}</p>
      </div>
    </section>
    <section class="section">
      <div class="container">
        <div v-if="loading" class="skeleton-grid">
          <div v-for="n in 6" :key="n" class="skeleton-card"></div>
        </div>
        <div v-else class="card-grid card-grid--3">
          <article v-for="item in products" :key="item.id" class="product-card">
            <RouterLink :to="`/products/${item.id}`">
              <SmartImage :src="item.cover" :alt="item.name" ratio="1 / 1" />
              <div class="card-body">
                <span class="tag">{{ item.category }}</span>
                <h3>{{ item.name }}</h3>
                <p>{{ item.detail }}</p>
                <div class="card-meta"><strong>{{ item.brand }}</strong><span>{{ item.ageRange }}</span></div>
              </div>
            </RouterLink>
          </article>
        </div>
        <div v-if="!loading && !products.length" class="empty">{{ t('empty') }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import SmartImage from '../components/SmartImage.vue'
import { api } from '../api'
import { t } from '../i18n'

const products = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    products.value = await api.products()
  } finally {
    loading.value = false
  }
})
</script>
