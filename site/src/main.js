import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'
import { setLocale, locale } from './i18n'

const routes = [
  { path: '/', name: 'home', component: () => import('./views/Home.vue'), meta: { titleKey: 'navHome' } },
  { path: '/services', name: 'services', component: () => import('./views/ServiceList.vue'), meta: { titleKey: 'navServices' } },
  { path: '/services/:id', name: 'service-detail', component: () => import('./views/ServiceDetail.vue'), meta: { titleKey: 'serviceDetail' } },
  { path: '/products', name: 'products', component: () => import('./views/ProductList.vue'), meta: { titleKey: 'navProducts' } },
  { path: '/products/:id', name: 'product-detail', component: () => import('./views/ProductDetail.vue'), meta: { titleKey: 'productDetail' } },
  { path: '/stores', name: 'stores', component: () => import('./views/Stores.vue'), meta: { titleKey: 'navStores' } },
  { path: '/cases', name: 'cases', component: () => import('./views/Cases.vue'), meta: { titleKey: 'navCases' } },
  { path: '/news', name: 'news', component: () => import('./views/News.vue'), meta: { titleKey: 'navNews' } },
  { path: '/news/:id', name: 'news-detail', component: () => import('./views/NewsDetail.vue'), meta: { titleKey: 'navNews' } },
  { path: '/about', name: 'about', component: () => import('./views/About.vue'), meta: { titleKey: 'navAbout' } },
  { path: '/contact', name: 'contact', component: () => import('./views/Contact.vue'), meta: { titleKey: 'navContact' } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  }
})

const queryLang = new URLSearchParams(window.location.search).get('lang')
if (queryLang === 'en' || queryLang === 'zh') {
  setLocale(queryLang)
} else {
  setLocale(locale.value)
}

createApp(App).use(router).mount('#app')
