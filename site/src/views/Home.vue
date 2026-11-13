<template>
  <div>
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="container hero-grid">
        <div class="hero-copy">
          <span class="pill">{{ t('heroTag') }}</span>
          <h1>{{ t('heroTitle') }}</h1>
          <p>{{ t('heroDesc') }}</p>
          <div class="hero-actions">
            <RouterLink to="/services" class="primary-btn">{{ t('bookNow') }}</RouterLink>
            <RouterLink to="/services" class="ghost-btn">{{ t('viewServices') }}</RouterLink>
          </div>
          <div class="hero-stats">
            <div><strong>{{ home.services.length || 3 }}+</strong><span>{{ t('navServices') }}</span></div>
            <div><strong>{{ home.stores.length || 2 }}</strong><span>{{ t('navStores') }}</span></div>
            <div><strong>7×12h</strong><span>{{ t('contactPhone') }}</span></div>
          </div>
        </div>
        <div class="hero-visual">
          <SmartImage :src="home.banners?.[0]?.image" :alt="home.banners?.[0]?.title || t('brand')" ratio="4 / 5" />
          <div class="floating-card floating-card--one">
            <span class="dot"></span>
            <div><strong>{{ t('aboutPoint1') }}</strong><small>{{ t('slogan') }}</small></div>
          </div>
          <div class="floating-card floating-card--two">
            <span class="icon-bubble">♥</span>
            <div><strong>{{ t('aboutPoint2') }}</strong><small>{{ t('sectionCasesDesc') }}</small></div>
          </div>
        </div>
      </div>
    </section>

    <section class="section section--soft">
      <div class="container">
        <SectionHead :title="t('sectionServices')" :desc="t('sectionServicesDesc')">
          <RouterLink to="/services" class="text-link">{{ t('allServices') }} →</RouterLink>
        </SectionHead>
        <div class="card-grid card-grid--3">
          <article v-for="item in home.services" :key="item.id" class="service-card">
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
          <div v-if="!home.services?.length" class="empty">{{ t('empty') }}</div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container about-grid">
        <div class="about-visual">
          <SmartImage :src="home.cases?.[0]?.cover" :alt="home.cases?.[0]?.title || t('navAbout')" ratio="1 / 1" />
          <div class="about-badge">
            <strong>2018</strong>
            <span>{{ t('navAbout') }}</span>
          </div>
        </div>
        <div class="about-copy">
          <span class="eyebrow">{{ t('navAbout') }}</span>
          <h2>{{ t('aboutTitle') }}</h2>
          <p>{{ t('aboutDesc') }}</p>
          <ul class="check-list">
            <li>{{ t('aboutPoint1') }}</li>
            <li>{{ t('aboutPoint2') }}</li>
            <li>{{ t('aboutPoint3') }}</li>
          </ul>
          <RouterLink to="/about" class="ghost-btn">{{ t('more') }}</RouterLink>
        </div>
      </div>
    </section>

    <section class="section section--soft">
      <div class="container">
        <SectionHead :title="t('sectionProducts')" :desc="t('sectionProductsDesc')">
          <RouterLink to="/products" class="text-link">{{ t('allProducts') }} →</RouterLink>
        </SectionHead>
        <div class="card-grid card-grid--3">
          <article v-for="item in home.products" :key="item.id" class="product-card">
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
      </div>
    </section>

    <section class="section">
      <div class="container">
        <SectionHead :title="t('sectionStores')" :desc="t('sectionStoresDesc')">
          <RouterLink to="/stores" class="text-link">{{ t('navStores') }} →</RouterLink>
        </SectionHead>
        <div class="store-grid">
          <article v-for="store in home.stores" :key="store.id" class="store-card">
            <div class="store-icon">⌖</div>
            <div>
              <h3>{{ store.name }}</h3>
              <p>{{ store.address }}</p>
              <p class="muted">{{ store.businessHours }} · {{ store.phone }}</p>
              <div class="store-tags">
                <span v-for="tag in (store.serviceRange || '').split(',')" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="section section--soft">
      <div class="container">
        <SectionHead :title="t('sectionCases')" :desc="t('sectionCasesDesc')">
          <RouterLink to="/cases" class="text-link">{{ t('more') }} →</RouterLink>
        </SectionHead>
        <div class="card-grid card-grid--2">
          <article v-for="item in home.cases" :key="item.id" class="case-card">
            <SmartImage :src="item.cover" :alt="pick(item, 'title', 'titleEn')" ratio="16 / 9" />
            <div class="card-body">
              <h3>{{ pick(item, 'title', 'titleEn') }}</h3>
              <p>{{ stripHtml(pick(item, 'content', 'content')) }}</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <SectionHead :title="t('sectionNews')" :desc="t('sectionNewsDesc')">
          <RouterLink to="/news" class="text-link">{{ t('allNews') }} →</RouterLink>
        </SectionHead>
        <div class="news-list">
          <article v-for="item in home.articles" :key="item.id" class="news-row">
            <SmartImage :src="item.cover" :alt="pick(item, 'title', 'titleEn')" ratio="3 / 2" />
            <div>
              <span class="tag">{{ item.category }}</span>
              <h3><RouterLink :to="`/news/${item.id}`">{{ pick(item, 'title', 'titleEn') }}</RouterLink></h3>
              <p>{{ item.seoDesc || stripHtml(pick(item, 'content', 'contentEn')).slice(0, 80) }}</p>
              <span class="muted">{{ formatDate(item.publishedAt) }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="cta-band">
      <div class="container cta-inner">
        <div>
          <h2>{{ t('contactTitle') }}</h2>
          <p>{{ t('contactDesc') }}</p>
        </div>
        <RouterLink to="/contact" class="primary-btn primary-btn--light">{{ t('bookNow') }}</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import SmartImage from '../components/SmartImage.vue'
import SectionHead from '../components/SectionHead.vue'
import { api } from '../api'
import { t, pick } from '../i18n'

const home = ref({ banners: [], services: [], products: [], stores: [], articles: [], cases: [] })

onMounted(async () => {
  try {
    home.value = await api.home('PC')
  } catch {
    home.value = { banners: [], services: [], products: [], stores: [], articles: [], cases: [] }
  }
})

function stripHtml(value) {
  return (value || '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

function formatDate(value) {
  return value ? String(value).slice(0, 10) : ''
}
</script>
