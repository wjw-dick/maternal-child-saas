import { defineStore } from 'pinia'
import http from '../api/http'

export const useAuth = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    portal: localStorage.getItem('portal') || 'tenant',
    tenantCode: localStorage.getItem('tenantCode') || 'demo',
    user: null as any
  }),
  actions: {
    async login(payload: { username: string; password: string; tenantCode?: string; portal: 'platform' | 'tenant' }) {
      const path = payload.portal === 'platform' ? '/platform/auth/login' : '/tenant/auth/login'
      const data: any = await http.post(path, {
        username: payload.username,
        password: payload.password,
        tenantCode: payload.tenantCode
      })
      this.token = data.token
      this.user = data.user
      this.portal = payload.portal
      this.tenantCode = payload.tenantCode || 'demo'
      localStorage.setItem('token', data.token)
      localStorage.setItem('portal', payload.portal)
      localStorage.setItem('tenantCode', this.tenantCode)
    },
    async me() {
      this.user = await http.get('/auth/me')
      return this.user
    },
    async changePassword(oldPassword: string, newPassword: string) {
      await http.post('/auth/change-password', { oldPassword, newPassword })
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('portal')
      location.href = '/login'
    }
  }
})
