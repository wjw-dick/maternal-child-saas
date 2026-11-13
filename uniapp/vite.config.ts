import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5174,
    proxy: {
      '/api': { target: process.env.VITE_API_TARGET || 'http://127.0.0.1:18080', changeOrigin: true },
      '/files': { target: process.env.VITE_API_TARGET || 'http://127.0.0.1:18080', changeOrigin: true }
    }
  }
})
