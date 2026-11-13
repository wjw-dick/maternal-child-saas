<template>
  <div class="login-wrap">
    <div class="login-brand">
      <div class="brand-badge">悦宝</div>
      <h1>母婴亲子数字化平台</h1>
      <p>多租户 SaaS · 会员 / 预约 / 交易 / 公众号 / 开放 API</p>
      <ul>
        <li>平台运营端与租户业务端物理隔离</li>
        <li>RBAC 权限与全量操作审计</li>
        <li>UnionID 多端会员互通</li>
      </ul>
    </div>
    <div class="login-card">
      <h2>登录控制台</h2>
      <el-tabs v-model="portal" stretch>
        <el-tab-pane label="租户后台" name="tenant" />
        <el-tab-pane label="平台运营" name="platform" />
      </el-tabs>
      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item v-if="portal === 'tenant'" label="租户编码">
          <el-input v-model="form.tenantCode" placeholder="演示租户：demo" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="login-hint">
        演示账号：<code>admin / admin123</code>（平台与租户各一套）
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '../stores/auth'

const router = useRouter()
const auth = useAuth()
const loading = ref(false)
const portal = ref<'tenant' | 'platform'>((localStorage.getItem('portal') as any) || 'tenant')
const form = reactive({ username: 'admin', password: 'admin123', tenantCode: 'demo' })

watch(portal, (value) => {
  form.username = 'admin'
  form.password = 'admin123'
  localStorage.setItem('portal', value)
})

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login({
      username: form.username,
      password: form.password,
      tenantCode: portal.value === 'tenant' ? form.tenantCode || 'demo' : undefined,
      portal: portal.value
    })
    ElMessage.success('登录成功')
    router.replace(portal.value === 'platform' ? '/platform/dashboard' : '/tenant/dashboard')
  } catch {
    // 错误提示由 http 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 64px;
  padding: 40px;
  background: radial-gradient(circle at 15% 20%, #ffe9e3 0, transparent 45%),
    radial-gradient(circle at 85% 75%, #dff1ff 0, transparent 45%), #f7f9fc;
}
.login-brand {
  max-width: 460px;
}
.brand-badge {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ff8a65, #ff5c8a);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(255, 92, 138, 0.3);
}
.login-brand h1 {
  font-size: 34px;
  margin: 22px 0 10px;
  color: #1f2d3d;
}
.login-brand p {
  color: #6b7a90;
  line-height: 1.7;
}
.login-brand ul {
  margin: 22px 0 0;
  padding-left: 18px;
  color: #56637a;
  line-height: 2;
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 28px 30px 24px;
  box-shadow: 0 24px 60px rgba(31, 45, 61, 0.12);
}
.login-card h2 {
  margin: 0 0 12px;
  font-size: 20px;
}
.login-btn {
  width: 100%;
  height: 42px;
  margin-top: 6px;
  font-size: 15px;
}
.login-hint {
  margin-top: 18px;
  color: #8a94a6;
  font-size: 12px;
  text-align: center;
}
.login-hint code {
  color: #ff5c8a;
}
@media (max-width: 900px) {
  .login-brand {
    display: none;
  }
  .login-wrap {
    padding: 20px;
  }
  .login-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>
