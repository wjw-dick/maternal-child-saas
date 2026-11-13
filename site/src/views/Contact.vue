<template>
  <div>
    <section class="page-hero">
      <div class="container">
        <span class="eyebrow">{{ t('navContact') }}</span>
        <h1>{{ t('contactTitle') }}</h1>
        <p>{{ t('contactDesc') }}</p>
      </div>
    </section>
    <section class="section">
      <div class="container contact-grid">
        <form class="contact-form" @submit.prevent="submit">
          <h2>{{ t('contactTitle') }}</h2>
          <label>
            <span>{{ t('name') }} *</span>
            <input v-model.trim="form.name" type="text" maxlength="40" :placeholder="t('name')" />
          </label>
          <label>
            <span>{{ t('phone') }} *</span>
            <input v-model.trim="form.phone" type="tel" maxlength="20" :placeholder="t('phone')" />
          </label>
          <label>
            <span>{{ t('message') }}</span>
            <textarea v-model.trim="form.content" rows="5" :placeholder="interest || t('message')"></textarea>
          </label>
          <p v-if="error" class="form-message form-message--error">{{ error }}</p>
          <p v-if="success" class="form-message form-message--ok">{{ success }}</p>
          <button class="primary-btn" type="submit" :disabled="submitting">
            {{ submitting ? t('submitting') : t('submit') }}
          </button>
        </form>

        <aside class="contact-aside">
          <div class="contact-card">
            <h3>{{ t('contactPhone') }}</h3>
            <p class="contact-phone">{{ configValue('service_phone', '400-800-2026') }}</p>
            <p class="muted">{{ t('businessHours') }} 09:00 - 21:00</p>
          </div>
          <div class="contact-card" v-for="store in stores" :key="store.id">
            <h3>{{ store.name }}</h3>
            <p>{{ store.address }}</p>
            <p class="muted">{{ store.phone }} · {{ store.businessHours }}</p>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'
import { t } from '../i18n'
import { configValue } from '../store'

const route = useRoute()
const stores = ref([])
const submitting = ref(false)
const success = ref('')
const error = ref('')
const form = reactive({ name: '', phone: '', content: '' })

const interest = computed(() => {
  if (route.query.service) return `${t('navServices')}：${route.query.service}`
  if (route.query.product) return `${t('navProducts')}：${route.query.product}`
  return ''
})

onMounted(async () => {
  if (interest.value) form.content = interest.value
  try {
    stores.value = await api.stores()
  } catch {
    stores.value = []
  }
})

async function submit() {
  error.value = ''
  success.value = ''
  if (!form.name || !form.phone) {
    error.value = t('required')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone) && !/^\+\d{6,15}$/.test(form.phone)) {
    error.value = '请输入有效的手机号码'
    return
  }
  submitting.value = true
  try {
    await api.submitForm({
      formType: route.query.service ? 'SERVICE_CONSULT' : route.query.product ? 'PRODUCT_CONSULT' : 'CONTACT',
      name: form.name,
      phone: form.phone,
      content: form.content,
      source: 'PC_SITE'
    })
    success.value = t('submitted')
    form.name = ''
    form.phone = ''
    form.content = interest.value || ''
  } catch (e) {
    error.value = `${t('formError')} (${e.message})`
  } finally {
    submitting.value = false
  }
}
</script>
