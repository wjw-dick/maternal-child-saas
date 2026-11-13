<template>
  <footer class="site-footer">
    <div class="container footer-grid">
      <div>
        <div class="brand footer-brand">
          <span class="brand-mark">悦</span>
          <strong>{{ configValue('site_name', t('brand')) }}</strong>
        </div>
        <p class="footer-desc">{{ t('footerAbout') }}</p>
      </div>
      <div>
        <h4>{{ t('footerQuick') }}</h4>
        <div class="footer-links">
          <RouterLink to="/services">{{ t('navServices') }}</RouterLink>
          <RouterLink to="/products">{{ t('navProducts') }}</RouterLink>
          <RouterLink to="/stores">{{ t('navStores') }}</RouterLink>
          <RouterLink to="/news">{{ t('navNews') }}</RouterLink>
          <RouterLink to="/contact">{{ t('navContact') }}</RouterLink>
        </div>
      </div>
      <div>
        <h4>{{ t('footerContact') }}</h4>
        <p>{{ t('contactPhone') }}：{{ configValue('service_phone', '400-800-2026') }}</p>
        <p>{{ t('contactAddress') }}：{{ configValue('address', '广州市天河区体育西路 1 号') }}</p>
        <p>{{ configValue('icp', t('icp')) }}</p>
      </div>
    </div>
    <div class="container footer-bottom">
      <span>© {{ year }} {{ configValue('site_name', t('brand')) }}</span>
      <span>{{ t('slogan') }}</span>
    </div>
  </footer>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import { t } from '../i18n'
import { configValue } from '../store'

const year = new Date().getFullYear()
</script>
