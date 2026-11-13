import { createRouter, createWebHistory } from 'vue-router'

const Layout = () => import('../layout/Layout.vue')
const CrudPage = () => import('../views/CrudPage.vue')
const Login = () => import('../views/Login.vue')

const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  {
    path: '/platform',
    component: Layout,
    meta: { portal: 'platform' },
    children: [
      { path: 'dashboard', component: () => import('../views/platform/Dashboard.vue'), meta: { title: '平台看板', group: '概览' } },
      { path: 'tenants', component: () => import('../views/platform/Tenants.vue'), meta: { title: '租户管理', group: '租户运营' } },
      { path: 'plans', component: CrudPage, meta: { title: '套餐与配额', group: '租户运营', scope: 'platform', resource: 'plans' } },
      { path: 'bills', component: () => import('../views/platform/Bills.vue'), meta: { title: '账单与计费', group: '租户运营' } },
      { path: 'notices', component: CrudPage, meta: { title: '平台公告', group: '租户运营', scope: 'platform', resource: 'notices' } },
      { path: 'audit', component: CrudPage, meta: { title: '平台审计', group: '安全', scope: 'platform', resource: 'platformAudit' } }
    ]
  },
  {
    path: '/tenant',
    component: Layout,
    meta: { portal: 'tenant' },
    children: [
      { path: 'dashboard', component: () => import('../views/tenant/Dashboard.vue'), meta: { title: '经营工作台', group: '概览' } },
      { path: 'members', component: CrudPage, meta: { title: '家庭会员', group: '会员', scope: 'tenant', resource: 'members' } },
      { path: 'babies', component: CrudPage, meta: { title: '宝宝档案', group: '会员', scope: 'tenant', resource: 'babies' } },
      { path: 'appointments', component: CrudPage, meta: { title: '预约管理', group: '预约', scope: 'tenant', resource: 'appointments' } },
      { path: 'orders', component: CrudPage, meta: { title: '订单管理', group: '交易', scope: 'tenant', resource: 'orders' } },
      { path: 'products', component: CrudPage, meta: { title: '商品管理', group: '交易', scope: 'tenant', resource: 'products' } },
      { path: 'skus', component: CrudPage, meta: { title: 'SKU 库存', group: '交易', scope: 'tenant', resource: 'skus' } },
      { path: 'stores', component: CrudPage, meta: { title: '门店档案', group: '门店服务', scope: 'tenant', resource: 'stores' } },
      { path: 'services', component: CrudPage, meta: { title: '服务项目', group: '门店服务', scope: 'tenant', resource: 'services' } },
      { path: 'schedules', component: CrudPage, meta: { title: '护理排班', group: '门店服务', scope: 'tenant', resource: 'schedules' } },
      { path: 'coupons', component: CrudPage, meta: { title: '卡券管理', group: '营销', scope: 'tenant', resource: 'coupons' } },
      { path: 'points', component: CrudPage, meta: { title: '积分规则', group: '营销', scope: 'tenant', resource: 'pointsRules' } },
      { path: 'points-flows', component: CrudPage, meta: { title: '积分流水', group: '营销', scope: 'tenant', resource: 'pointsFlows' } },
      { path: 'activities', component: CrudPage, meta: { title: '亲子活动', group: '营销', scope: 'tenant', resource: 'activities' } },
      { path: 'suppliers', component: CrudPage, meta: { title: '供应商', group: '供应链', scope: 'tenant', resource: 'suppliers' } },
      { path: 'supply-orders', component: CrudPage, meta: { title: '供货单', group: '供应链', scope: 'tenant', resource: 'supplyOrders' } },
      { path: 'distributors', component: CrudPage, meta: { title: '渠道商', group: '渠道', scope: 'tenant', resource: 'distributors' } },
      { path: 'distributor-reports', component: CrudPage, meta: { title: '渠道报单', group: '渠道', scope: 'tenant', resource: 'distributorReports' } },
      { path: 'staff', component: CrudPage, meta: { title: '员工档案', group: '员工', scope: 'tenant', resource: 'staff' } },
      { path: 'licenses', component: CrudPage, meta: { title: '资质证照', group: '员工', scope: 'tenant', resource: 'licenses' } },
      { path: 'wechat', component: () => import('../views/tenant/Wechat.vue'), meta: { title: '公众号运营', group: '公众号' } },
      { path: 'articles', component: CrudPage, meta: { title: '资讯 CMS', group: '官网', scope: 'tenant', resource: 'articles' } },
      { path: 'banners', component: CrudPage, meta: { title: 'Banner', group: '官网', scope: 'tenant', resource: 'banners' } },
      { path: 'cases', component: CrudPage, meta: { title: '客户案例', group: '官网', scope: 'tenant', resource: 'cases' } },
      { path: 'config', component: () => import('../views/tenant/ConfigPage.vue'), meta: { title: '全局参数', group: '系统' } },
      { path: 'menu-designer', component: () => import('../views/tenant/MenuDesigner.vue'), meta: { title: '菜单可视化', group: '系统' } },
      { path: 'storage', component: () => import('../views/tenant/StorageConfig.vue'), meta: { title: '对象存储', group: '系统' } },
      { path: 'message', component: () => import('../views/tenant/MessageConfig.vue'), meta: { title: '消息配置', group: '系统' } },
      { path: 'sso', component: () => import('../views/tenant/SsoConfig.vue'), meta: { title: 'SSO 配置', group: '系统' } },
      { path: 'users', component: CrudPage, meta: { title: '账号管理', group: '系统', scope: 'tenant', resource: 'users' } },
      { path: 'roles', component: CrudPage, meta: { title: '角色权限', group: '系统', scope: 'tenant', resource: 'roles' } },
      { path: 'audit', component: CrudPage, meta: { title: '审计日志', group: '系统', scope: 'tenant', resource: 'auditLogs' } },
      { path: 'report', component: () => import('../views/tenant/Report.vue'), meta: { title: '经营报表', group: '报表' } }
    ]
  },
  { path: '/', redirect: '/login' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const portal = localStorage.getItem('portal') || 'tenant'
  if (to.meta.public) {
    if (token) return portal === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'
    return true
  }
  if (!token) return '/login'
  if (to.meta.portal && to.meta.portal !== portal) {
    return portal === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'
  }
  return true
})

export default router
