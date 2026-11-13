const BASE = (import.meta.env.VITE_API_BASE || '').replace(/\/$/, '')
const TENANT = import.meta.env.VITE_TENANT_CODE || ''

async function request(path, options = {}) {
  const headers = { Accept: 'application/json', ...(options.headers || {}) }
  if (TENANT) headers['X-Tenant-Code'] = TENANT
  if (options.body) headers['Content-Type'] = 'application/json'
  const response = await fetch(`${BASE}${path}`, { ...options, headers })
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }
  const body = await response.json()
  if (body.code !== 0) {
    throw new Error(body.msg || '请求失败')
  }
  return body.data
}

const get = (path) => request(path)

export const api = {
  home: (terminal = 'PC') => get(`/api/public/home?terminal=${encodeURIComponent(terminal)}`),
  services: () => get('/api/public/services'),
  service: (id) => get(`/api/public/services/${id}`),
  products: () => get('/api/public/products'),
  product: (id) => get(`/api/public/products/${id}`),
  stores: () => get('/api/public/stores'),
  articles: () => get('/api/public/articles'),
  article: (id) => get(`/api/public/articles/${id}`),
  cases: () => get('/api/public/cases'),
  activities: () => get('/api/public/activities'),
  config: () => get('/api/public/config'),
  i18n: (lang) => get(`/api/public/i18n/${lang}`),
  submitForm: (payload) => request('/api/public/forms', { method: 'POST', body: JSON.stringify(payload) })
}

/** 静态资源（/files/...）在后端托管，开发时由 Vite 代理。 */
export function assetUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  return `${BASE}${path.startsWith('/') ? '' : '/'}${path}`
}
