<template>
  <div>
    <section class="page-hero">
      <div class="container">
        <span class="eyebrow">{{ t('navStores') }}</span>
        <h1>{{ t('sectionStores') }}</h1>
        <p>{{ t('sectionStoresDesc') }}</p>
      </div>
    </section>
    <section class="section">
      <div class="container store-layout">
        <div v-if="loading" class="skeleton-grid"><div v-for="n in 4" :key="n" class="skeleton-card"></div></div>
        <template v-else>
          <div class="store-list">
            <article v-for="store in stores" :key="store.id" class="store-card store-card--large">
              <div class="store-icon">⌖</div>
              <div>
                <h3>{{ store.name }}</h3>
                <p>{{ store.address }}</p>
                <p class="muted">{{ t('hours') }}：{{ store.businessHours }} · {{ t('phoneLabel') }}：{{ store.phone }}</p>
                <div class="store-tags">
                  <span v-for="tag in (store.serviceRange || '').split(',')" :key="tag" class="tag">{{ tag }}</span>
                </div>
                <a
                  v-if="store.lng && store.lat"
                  class="text-link"
                  :href="`https://uri.amap.com/marker?position=${store.lng},${store.lat}&name=${encodeURIComponent(store.name)}`"
                  target="_blank"
                  rel="noopener"
                >{{ t('address') }} →</a>
              </div>
            </article>
          </div>
          <div class="map-card">
            <div class="map-grid"></div>
            <div class="map-pin" v-for="(store, index) in stores" :key="store.id" :style="pinStyle(index)">
              <span>{{ index + 1 }}</span>
            </div>
            <div class="map-label">{{ t('sectionStores') }}</div>
          </div>
        </template>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api'
import { t } from '../i18n'

const stores = ref([])
const loading = ref(true)

const positions = [
  { left: '28%', top: '38%' },
  { left: '62%', top: '58%' },
  { left: '46%', top: '24%' },
  { left: '72%', top: '34%' }
]

function pinStyle(index) {
  const pos = positions[index % positions.length]
  return { left: pos.left, top: pos.top }
}

onMounted(async () => {
  try {
    stores.value = await api.stores()
  } finally {
    loading.value = false
  }
})
</script>
