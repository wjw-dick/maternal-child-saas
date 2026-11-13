import { ref } from 'vue'
import { api } from './api'

export const siteConfig = ref({})
export const configLoaded = ref(false)

export async function loadSiteConfig() {
  if (configLoaded.value) return
  try {
    siteConfig.value = (await api.config()) || {}
  } catch {
    siteConfig.value = {}
  } finally {
    configLoaded.value = true
  }
}

export function configValue(key, fallback = '') {
  const value = siteConfig.value[key]
  return value === undefined || value === null || value === '' ? fallback : value
}
