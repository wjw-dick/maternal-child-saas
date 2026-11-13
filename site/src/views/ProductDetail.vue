<template>
  <section class="section detail-section">
    <div class="container">
      <RouterLink to="/products" class="back-link">← {{ t('back') }}</RouterLink>
      <div v-if="product" class="detail-grid">
        <SmartImage :src="product.cover" :alt="product.name" ratio="1 / 1" />
        <div class="detail-copy">
          <span class="tag">{{ product.category }}</span>
          <h1>{{ product.name }}</h1>
          <p class="detail-desc">{{ product.detail }}</p>
          <div class="detail-facts">
            <div><strong>{{ product.brand }}</strong><span>Brand</span></div>
            <div><strong>{{ product.ageRange }}</strong><span>Age</span></div>
            <div><strong>{{ skus.length }}</strong><span>SKU</span></div>
          </div>
          <div v-if="skus.length" class="sku-table">
            <div v-for="sku in skus" :key="sku.id" class="sku-row">
              <span>{{ sku.skuCode }} · {{ sku.specJson }}</span>
              <strong>¥{{ Number(sku.price || 0).toFixed(2) }}</strong>
              <span class="muted">{{ t('stock') }} {{ sku.stock }}</span>
            </div>
          </div>
          <RouterLink :to="{ path: '/contact', query: { product: product.name } }" class="primary-btn">{{ t('consult') }}</RouterLink>
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
const product = ref(null)
const skus = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const data = await api.product(route.params.id)
    product.value = data.product
    skus.value = data.skus || []
  } catch {
    product.value = null
    skus.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>
