import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api', timeout: 30000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  const tenantCode = localStorage.getItem('tenantCode')
  if (token) config.headers.Authorization = `Bearer ${token}`
  if (tenantCode) config.headers['X-Tenant-Code'] = tenantCode
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number') {
      if (body.code !== 0) {
        ElMessage.error(body.msg || '请求失败')
        if (body.code === 401) {
          localStorage.removeItem('token')
          location.href = '/login'
        }
        return Promise.reject(body)
      }
      return body.data
    }
    return body
  },
  (err) => {
    const msg = err?.response?.data?.msg || err.message || '网络异常'
    ElMessage.error(msg)
    if (err?.response?.status === 401) {
      localStorage.removeItem('token')
      location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default http
