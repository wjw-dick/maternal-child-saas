<template>
  <div class="site-shell">
    <SiteHeader />
    <main class="site-main">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
    <SiteFooter />
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
import { locale, t } from './i18n'
import { loadSiteConfig, siteConfig } from './store'

const route = useRoute()

function upsertMeta(name, content) {
  let el = document.querySelector(`meta[name="${name}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute('name', name)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function upsertProperty(property, content) {
  let el = document.querySelector(`meta[property="${property}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute('property', property)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function updateHreflang() {
  document.querySelectorAll('link[data-hreflang]').forEach((el) => el.remove())
  const base = (import.meta.env.VITE_SITE_URL || window.location.origin).replace(/\/$/, '')
  const links = [
    { lang: 'zh-CN', href: `${base}/?lang=zh` },
    { lang: 'en', href: `${base}/?lang=en` },
    { lang: 'x-default', href: `${base}/` }
  ]
  links.forEach(({ lang, href }) => {
    const el = document.createElement('link')
    el.rel = 'alternate'
    el.hreflang = lang
    el.href = href
    el.setAttribute('data-hreflang', '1')
    document.head.appendChild(el)
  })
  let canonical = document.querySelector('link[rel="canonical"]')
  if (!canonical) {
    canonical = document.createElement('link')
    canonical.rel = 'canonical'
    document.head.appendChild(canonical)
  }
  canonical.href = `${base}${route.fullPath}`
}

function updateSeo() {
  const titleKey = route.meta.titleKey || 'navHome'
  const brand = siteConfig.value.site_name || t('brand')
  document.title = route.name === 'home' ? `${brand} | ${t('slogan')}` : `${t(titleKey)} | ${brand}`
  const desc = siteConfig.value.seo_description || t('heroDesc')
  upsertMeta('description', desc)
  upsertMeta('keywords', siteConfig.value.seo_keywords || '母婴护理,新生儿护理,产后康复,育儿嫂,亲子活动')
  upsertProperty('og:title', document.title)
  upsertProperty('og:description', desc)
  upsertProperty('og:type', 'website')
  updateHreflang()
}

onMounted(async () => {
  await loadSiteConfig()
  updateSeo()
})

watch([() => route.fullPath, locale], updateSeo)
</script>
