import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as Icons from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
Object.entries(Icons).forEach(([key, value]) => app.component(key, value as any))
app.use(createPinia()).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
