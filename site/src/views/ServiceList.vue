<template>
  <div>
    <section class="page-hero">
      <div class="container">
        <span class="eyebrow">{{ t('navServices') }}</span>
        <h1>{{ t('sectionServices') }}</h1>
        <p>{{ t('sectionServicesDesc') }}</p>
      </div>
    </section>
    <section class="section">
      <div class="container">
        <div class="filter-row">
          <button
            v-for="cat in categories"
            :key="cat.value"
            type="button"
            class="filter-chip"
            :class="{ active: activeCategory === cat.value }"
            @click="activeCategory = cat.value"
          >
            {{ cat.label }}
          </button>
        </div>
        <div v-if="loading" class="skeleton-grid">
          <div v-for="n in 6" :key="n" class="skeleton-card"></div>
        </div>
        <div v-else class="card-grid card-grid--3">
          <article v-for="item in filtered" :key="item.id" class="service-card">
            <RouterLink :to="`/services/${item.id}`">
              <SmartImage :src="item.cover" :alt="item.name" ratio="16 / 10" />
              <div class="card-body">
                <span class="tag">{{ item.category }}</span>
                <h3>{{ item.name }}</h3>
                <p>{{ item.description }}</p>
                <div class="card-meta">
                  <strong>¥{{ Number(item.price || 0).toFixed(0) }}</strong>
                  <span>{{ item.duration }}{{ t('duration') }}</span>
                </div>
              </div>
            </RouterLink>
          </article>
        </div>
        <div v-if="!loading && !filtered.length" class="empty">{{ t('empty') }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import SmartImage from '../components/SmartImage.vue'
import { api } from '../api'
import { t } from '../i18n'

const services = ref([])
const loading = ref(true)
const activeCategory = ref('ALL')

const categoryLabels = {
  CARE: '新生儿护理',
  REHAB: '产后康复',
  NANNY: '育儿照护',
  FOOD: '营养辅食',
  DAILY: '日常护理',
  TOY: '益智玩具'
}

const categories = computed(() => {
  const set = [...new Set(services.value.map((s) => s.category).filter(Boolean))]
  return [{ value: 'ALL', label: t('allServices') }, ...set.map((c) => ({ value: c, label: categoryLabels[c] || c }))]
})

const filtered = computed(() =>
  activeCategory.value === 'ALL' ? services.value : services.value.filter((s) => s.category === activeCategory.value)
)

onMounted(async () => {
  try {
    services.value = await api.services()
  } finally {
    loading.value = false
  }
})
</script>
