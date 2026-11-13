<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '232px'" class="layout-aside">
      <div class="logo" @click="router.push(home)">
        <span class="logo-mark">悦</span>
        <span v-if="!collapsed" class="logo-text">
          <b>悦宝 SaaS</b>
          <small>{{ portal === 'platform' ? '平台运营端' : '租户业务端' }}</small>
        </span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#c6d1e3"
          active-text-color="#ffffff"
          router
        >
          <template v-for="group in groups" :key="group.name">
            <div v-if="!collapsed" class="menu-group">{{ group.name }}</div>
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              <el-icon><component :is="iconOf(item)" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <div class="aside-footer">
        <el-button link class="collapse-btn" @click="collapsed = !collapsed">
          {{ collapsed ? '»' : '« 收起菜单' }}
        </el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ portal === 'platform' ? '平台运营' : '租户后台' }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.group }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag v-if="portal === 'tenant'" size="small" effect="plain">{{ auth.tenantCode }}</el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-chip">
              <el-avatar :size="30">{{ (displayName || 'U').slice(0, 1) }}</el-avatar>
              <span class="user-name">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog v-model="passwordVisible" title="修改密码" width="420px">
      <el-form label-width="90px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="changePassword">保存</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const collapsed = ref(false)
const passwordVisible = ref(false)
const passwordSaving = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '' })

const portal = computed(() => (String(route.path).startsWith('/platform') ? 'platform' : 'tenant'))
const home = computed(() => (portal.value === 'platform' ? '/platform/dashboard' : '/tenant/dashboard'))
const displayName = computed(() => auth.user?.user?.nickname || auth.user?.username || '管理员')

const iconMap: Record<string, string> = {
  概览: 'DataBoard',
  租户运营: 'OfficeBuilding',
  安全: 'Lock',
  会员: 'User',
  预约: 'Calendar',
  交易: 'ShoppingCart',
  门店服务: 'Shop',
  营销: 'Present',
  供应链: 'Van',
  渠道: 'Share',
  员工: 'Avatar',
  公众号: 'ChatDotRound',
  官网: 'Monitor',
  系统: 'Setting',
  报表: 'TrendCharts'
}

const groups = computed(() => {
  const root = router.options.routes.find((item: any) => item.path === `/${portal.value}`) as any
  const children = (root?.children || []).filter((item: any) => item.meta?.title)
  const map = new Map<string, Array<{ path: string; title: string; group: string }>>()
  children.forEach((item: any) => {
    const group = item.meta.group || '其他'
    if (!map.has(group)) map.set(group, [])
    map.get(group)!.push({
      path: `/${portal.value}/${item.path}`,
      title: item.meta.title,
      group
    })
  })
  return Array.from(map.entries()).map(([name, items]) => ({ name, items }))
})

function iconOf(item: { group: string }) {
  return iconMap[item.group] || 'Menu'
}

async function onCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    auth.logout()
  } else if (command === 'password') {
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordVisible.value = true
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  passwordSaving.value = true
  try {
    await auth.changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码已修改，请重新登录')
    passwordVisible.value = false
    auth.logout()
  } finally {
    passwordSaving.value = false
  }
}

onMounted(async () => {
  try {
    await auth.me()
  } catch {
    // 未登录跳转由路由守卫与拦截器处理
  }
})
</script>

<style scoped>
.layout {
  height: 100%;
}
.layout-aside {
  background: linear-gradient(180deg, #1d2a3f 0%, #141d2b 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.logo-mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff8a65, #ff5c8a);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.logo-text {
  display: flex;
  flex-direction: column;
  color: #fff;
  line-height: 1.3;
  white-space: nowrap;
}
.logo-text b {
  font-size: 15px;
}
.logo-text small {
  color: #8fa2bd;
  font-size: 11px;
}
.menu-scroll {
  flex: 1;
}
.menu-group {
  padding: 14px 20px 6px;
  font-size: 11px;
  color: #6d7f99;
  letter-spacing: 1px;
}
.el-menu {
  border-right: none;
}
.el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(255, 92, 138, 0.28), rgba(255, 92, 138, 0.05)) !important;
  border-right: 3px solid #ff5c8a;
}
.aside-footer {
  padding: 8px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}
.collapse-btn {
  color: #8fa2bd;
  width: 100%;
}
.layout-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #edf0f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #33415c;
}
.user-name {
  font-size: 13px;
}
.layout-main {
  background: #f5f7fa;
  padding: 0;
}
</style>
