<template>
  <header class="site-header" :class="{ 'is-open': menuOpen }">
    <div class="container header-inner">
      <RouterLink to="/" class="brand" @click="menuOpen = false">
        <span class="brand-mark">悦</span>
        <span class="brand-text">
          <strong>{{ configValue('site_name', t('brand')) }}</strong>
          <small>{{ t('slogan') }}</small>
        </span>
      </RouterLink>

      <nav class="nav" :class="{ 'nav--open': menuOpen }">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" @click="menuOpen = false">
          {{ t(item.key) }}
        </RouterLink>
      </nav>

      <div class="header-actions">
        <button class="ghost-btn" type="button" @click="toggleLocale">{{ t('otherLang') }}</button>
        <a class="primary-btn header-login" :href="appUrl">{{ t('login') }}</a>
        <button class="menu-btn" type="button" :aria-expanded="menuOpen" aria-label="Menu" @click="menuOpen = !menuOpen">
          <span></span><span></span><span></span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { t, toggleLocale } from '../i18n'
import { configValue } from '../store'

const menuOpen = ref(false)
const appUrl = import.meta.env.VITE_APP_URL || '/app/'

const navItems = [
  { to: '/', key: 'navHome' },
  { to: '/services', key: 'navServices' },
  { to: '/products', key: 'navProducts' },
  { to: '/stores', key: 'navStores' },
  { to: '/cases', key: 'navCases' },
  { to: '/news', key: 'navNews' },
  { to: '/about', key: 'navAbout' },
  { to: '/contact', key: 'navContact' }
]
</script>
