<template>
  <div class="page">
    <div class="card">
      <div class="card-title">对象存储配置</div>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        本地磁盘为默认驱动；切换云端驱动需二次确认。当前版本仅 LOCAL 驱动完成写入实现，S3/OSS/COS/MinIO 需装配 SDK 后启用。
      </el-alert>

      <el-radio-group v-model="driver" class="driver-group" @change="onDriverChange">
        <el-radio-button v-for="item in drivers" :key="item.value" :label="item.value">
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>

      <el-form label-width="120px" style="margin-top: 20px; max-width: 720px">
        <el-form-item label="存储驱动">
          <el-tag effect="plain">{{ driver }}</el-tag>
          <span class="muted" style="margin-left: 10px">{{ driverHint }}</span>
        </el-form-item>
        <el-form-item label="配置 JSON">
          <el-input v-model="configJson" type="textarea" :rows="10" spellcheck="false" />
        </el-form-item>
        <el-form-item>
          <el-button @click="test">连接测试</el-button>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="testResult"
        :title="testResult.message"
        :type="testResult.ok ? 'success' : 'warning'"
        show-icon
        :closable="false"
        style="max-width: 720px"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

const drivers = [
  { label: '本地磁盘', value: 'LOCAL' },
  { label: 'AWS S3', value: 'S3' },
  { label: '阿里云 OSS', value: 'OSS' },
  { label: '腾讯云 COS', value: 'COS' },
  { label: 'MinIO', value: 'MINIO' }
]
const samples: Record<string, any> = {
  LOCAL: { root: './data/storage', publicPrefix: '/files' },
  S3: { bucket: '', region: 'ap-southeast-1', accessKey: '', secretKey: '', endpoint: '' },
  OSS: { bucket: '', region: 'oss-cn-shenzhen', accessKeyId: '', accessKeySecret: '', endpoint: '' },
  COS: { bucket: '', region: 'ap-guangzhou', secretId: '', secretKey: '', endpoint: '' },
  MINIO: { endpoint: 'http://127.0.0.1:9000', bucket: '', accessKey: '', secretKey: '' }
}

const driver = ref('LOCAL')
const savedDriver = ref('LOCAL')
const configJson = ref(JSON.stringify(samples.LOCAL, null, 2))
const saving = ref(false)
const testResult = ref<any>(null)

const driverHint = computed(() => {
  const map: Record<string, string> = {
    LOCAL: '文件写入服务器本地目录，适合单机宝塔部署',
    S3: '需安装 AWS SDK 并配置 AK/SK',
    OSS: '需安装阿里云 SDK 并配置 AK/SK',
    COS: '需安装腾讯云 SDK 并配置密钥',
    MINIO: '需安装 MinIO SDK 并配置地址与密钥'
  }
  return map[driver.value]
})

function onDriverChange(value: any) {
  configJson.value = JSON.stringify(samples[value] || {}, null, 2)
  testResult.value = null
}

async function test() {
  let config: any = {}
  try {
    config = JSON.parse(configJson.value || '{}')
  } catch {
    ElMessage.error('配置 JSON 格式非法')
    return
  }
  testResult.value = await http.post('/tenant/storage-config/test', {
    driver: driver.value,
    root: config.root || './data/storage'
  })
}

async function save() {
  const payload = { driver: driver.value, configJson: configJson.value }
  let switchConfirmed = false
  if (driver.value !== savedDriver.value) {
    await ElMessageBox.confirm('切换存储驱动后，历史文件仍保留在原驱动，确认切换？', '二次确认', { type: 'warning' })
    switchConfirmed = true
  }
  saving.value = true
  try {
    await http.put('/tenant/storage-config', payload, { params: { confirmSwitch: switchConfirmed } })
    savedDriver.value = driver.value
    ElMessage.success('存储配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const data: any = await http.get('/tenant/storage-config')
  if (data?.driver) {
    driver.value = data.driver
    savedDriver.value = data.driver
    configJson.value = data.configJson || JSON.stringify(samples[data.driver] || {}, null, 2)
  }
})
</script>

<style scoped>
.driver-group :deep(.el-radio-button__inner) {
  padding: 10px 20px;
}
</style>
