export const API_BASE = ''

export function getToken() {
  return uni.getStorageSync('token') || ''
}

export function getTenantCode() {
  return uni.getStorageSync('tenantCode') || 'demo'
}

export function getShareSource() {
  return uni.getStorageSync('shareSource') || ''
}

export function setSession(data) {
  uni.setStorageSync('token', data.token || '')
  uni.setStorageSync('user', data.user || data.member || null)
  uni.setStorageSync('userType', data.user?.userType || (data.member ? 'MEMBER' : ''))
}

export function clearSession() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
  uni.removeStorageSync('userType')
}

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.request({
      url: `${API_BASE}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'X-Tenant-Code': getTenantCode(),
        ...(getShareSource() ? { 'X-Source': getShareSource() } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      success: (res) => {
        const body = res.data || {}
        if (body && typeof body.code === 'number') {
          if (body.code === 0) {
            resolve(body.data)
          } else {
            if (body.code === 401) {
              clearSession()
            }
            uni.showToast({ title: body.msg || '请求失败', icon: 'none' })
            reject(body)
          }
        } else {
          resolve(body)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const api = {
  home: () => request({ url: '/api/public/home?terminal=MINI' }),
  services: () => request({ url: '/api/public/services' }),
  stores: () => request({ url: '/api/public/stores' }),
  articles: () => request({ url: '/api/public/articles' }),
  article: (id) => request({ url: `/api/public/articles/${id}` }),
  products: () => request({ url: '/api/public/products' }),
  activities: () => request({ url: '/api/public/activities' }),
  menu: (terminal) => request({ url: `/api/public/menus/${terminal}` }),
  miniLogin: (payload) => request({ url: '/api/mini/auth/login', method: 'POST', data: payload }),
  staffLogin: (payload) => request({ url: '/api/tenant/auth/login', method: 'POST', data: payload }),
  me: () => request({ url: '/api/auth/me' }),
  myAppointments: () => request({ url: '/api/mini/appointments/mine' }),
  createAppointment: (data) => request({ url: '/api/mini/appointments', method: 'POST', data }),
  myOrders: () => request({ url: '/api/mini/orders/mine' }),
  payOrder: (id) => request({ url: `/api/mini/orders/${id}/pay?channel=MOCK`, method: 'POST' }),
  center: () => request({ url: '/api/mini/member/center' }),
  babies: () => request({ url: '/api/mini/babies' }),
  addBaby: (data) => request({ url: '/api/mini/babies', method: 'POST', data }),
  signup: (id) => request({ url: `/api/mini/activities/${id}/signup`, method: 'POST', data: {} }),
  workbench: () => request({ url: '/api/staff/workbench' }),
  acceptAppointment: (id) => request({ url: `/api/staff/appointments/${id}/accept`, method: 'POST' }),
  completeAppointment: (id) => request({ url: `/api/staff/appointments/${id}/complete`, method: 'POST', data: {} }),
  staffIncome: () => request({ url: '/api/staff/income' }),
  staffIncomeDetail: () => request({ url: '/api/staff/income/detail' }),
  staffAppointment: (id) => request({ url: `/api/staff/appointments/${id}` }),
  staffCheckin: (id, data) => request({ url: `/api/staff/appointments/${id}/checkin`, method: 'POST', data }),
  staffRecord: (id, data) => request({ url: `/api/staff/appointments/${id}/records`, method: 'POST', data }),
  staffComplete: (id, signUrl) => request({ url: `/api/staff/appointments/${id}/complete`, method: 'POST', data: { signUrl } }),
  staffSchedules: () => request({ url: '/api/staff/schedules' }),
  staffProfile: () => request({ url: '/api/staff/profile' }),
  staffNotices: () => request({ url: '/api/staff/notices' }),
  supplyOrders: () => request({ url: '/api/supplier/supply-orders' }),
  signSupply: (id) => request({ url: `/api/supplier/supply-orders/${id}/sign`, method: 'POST' }),
  statements: () => request({ url: '/api/supplier/statements' }),
  supplierProfile: () => request({ url: '/api/supplier/profile' }),
  supplierQualifications: () => request({ url: '/api/supplier/qualifications' }),
  supplierAddQualification: (data) => request({ url: '/api/supplier/qualifications', method: 'POST', data }),
  supplierNotices: () => request({ url: '/api/supplier/notices' }),
  distributorReports: () => request({ url: '/api/distributor/reports' }),
  distributorProfile: () => request({ url: '/api/distributor/profile' }),
  distributorSettlements: () => request({ url: '/api/distributor/settlements' }),
  distributorOrders: () => request({ url: '/api/distributor/orders' }),
  distributorSubordinates: () => request({ url: '/api/distributor/subordinates' }),
  distributorNotices: () => request({ url: '/api/distributor/notices' }),
  commissions: () => request({ url: '/api/distributor/commissions' }),
  credit: () => request({ url: '/api/distributor/credit' }),
  changePassword: (oldPassword, newPassword) =>
    request({ url: '/api/auth/change-password', method: 'POST', data: { oldPassword, newPassword } }),
  employeeOverview: () => request({ url: '/api/employee/overview' }),
  employeeAppointments: (date) => request({ url: `/api/employee/appointments${date ? `?date=${date}` : ''}` }),
  employeeVerify: (code) => request({ url: '/api/employee/appointments/verify', method: 'POST', data: { code } }),
  employeeMembers: (keyword) => request({ url: `/api/employee/members${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}` }),
  employeeOpenCard: (data) => request({ url: '/api/employee/members', method: 'POST', data }),
  employeeWallet: (id) => request({ url: `/api/employee/members/${id}/wallet` }),
  employeeRecharge: (id, amount, remark) =>
    request({ url: `/api/employee/members/${id}/recharge`, method: 'POST', data: { amount, remark } }),
  employeeConsume: (id, amount, remark) =>
    request({ url: `/api/employee/members/${id}/consume`, method: 'POST', data: { amount, remark } }),
  employeeSkus: (keyword) => request({ url: `/api/employee/skus${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}` }),
  employeeStockAdjust: (data) => request({ url: '/api/employee/stock/adjust', method: 'POST', data }),
  employeeOrders: (date) => request({ url: `/api/employee/orders${date ? `?date=${date}` : ''}` }),
  employeeCompleteOrder: (id) => request({ url: `/api/employee/orders/${id}/complete`, method: 'POST' }),
  employeeSchedules: () => request({ url: '/api/employee/schedules' }),
  employeeBrief: () => request({ url: '/api/employee/brief' })
}
