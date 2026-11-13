<template>
  <div class="page">
    <div class="card">
      <div class="card-title">SSO 单点登录</div>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        支持 CAS / OAuth2 / OIDC / Sa-Token 四种协议；账号映射使用 JSON 描述外部字段与本地账号的对应关系。
      </el-alert>
      <el-form label-width="130px" style="max-width: 720px">
        <el-form-item label="启用 SSO">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="协议">
          <el-select v-model="form.protocol" style="width: 240px">
            <el-option v-for="item in protocols" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务地址">
          <el-input v-model="form.baseUrl" placeholder="https://sso.example.com" />
        </el-form-item>
        <el-form-item label="Client ID">
          <el-input v-model="form.clientId" />
        </el-form-item>
        <el-form-item label="Client Secret">
          <el-input v-model="form.clientSecret" show-password placeholder="留空表示不修改" />
        </el-form-item>
        <el-form-item label="账号映射">
          <el-input v-model="form.accountMapping" type="textarea" :rows="5" spellcheck="false" placeholder='{"email":"username","name":"nickname"}' />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
          <el-button @click="checkAuthorize">生成授权链接</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="authorizeUrl" :title="authorizeUrl" type="success" :closable="false" show-icon />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const protocols = ref<string[]>(['CAS', 'OAUTH2', 'OIDC', 'SA_TOKEN'])
const saving = ref(false)
const authorizeUrl = ref('')
const form = reactive<any>({
  enabled: 0,
  protocol: 'OIDC',
  baseUrl: '',
  clientId: '',
  clientSecret: '',
  accountMapping: '{"email":"username","name":"nickname"}'
})

async function save() {
  saving.value = true
  try {
    await http.post('/tenant/sso-config', form)
    ElMessage.success('SSO 配置已保存')
    await load()
  } finally {
    saving.value = false
  }
}

async function checkAuthorize() {
  const data: any = await http.post('/tenant/sso-config/authorize-url', { service: location.origin + '/login' })
  if (data.enabled === 'true') {
    authorizeUrl.value = data.url
    ElMessage.success('授权链接已生成')
  } else {
    authorizeUrl.value = ''
    ElMessage.warning('SSO 尚未启用')
  }
}

async function load() {
  const data: any = await http.get('/tenant/sso-config')
  Object.assign(form, data || {})
  const list: any = await http.get('/tenant/sso-config/protocols')
  protocols.value = list || protocols.value
}

onMounted(load)
</script>
