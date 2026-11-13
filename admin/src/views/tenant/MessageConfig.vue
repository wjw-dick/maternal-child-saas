<template>
  <div class="page">
    <div class="card">
      <div class="card-title">消息通道</div>
      <el-table :data="channels" border size="small">
        <el-table-column prop="channel" label="通道" width="150" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="(v: boolean) => toggleChannel(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="配置 JSON">
          <template #default="{ row }">
            <el-input v-model="row.configJson" size="small" placeholder='{"appId":"","secret":""}' />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="saveChannel(row)">保存</el-button>
            <el-button link type="success" @click="test('WECHAT_TEMPLATE')">发送测试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" style="margin-top: 16px">
      <div class="card-title">短信渠道（全局互斥，仅可启用一个）</div>
      <el-table :data="sms" border size="small">
        <el-table-column prop="provider" label="服务商" width="150" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-radio :model-value="row.enabled === 1" :label="true" @change="() => enableSms(row)">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="配置 JSON">
          <template #default="{ row }">
            <el-input v-model="row.configJson" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button link type="primary" @click="saveSms(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="toolbar" style="margin-top: 12px">
        <el-input v-model="testTarget" placeholder="测试手机号/OpenID" style="width: 240px" />
        <el-button @click="test('SMS')">短信连通测试</el-button>
      </div>
    </div>

    <div class="card" style="margin-top: 16px">
      <div class="card-title">消息模板</div>
      <el-table :data="templates" border size="small">
        <el-table-column prop="node" label="业务节点" width="160" />
        <el-table-column prop="channel" label="通道" width="140" />
        <el-table-column prop="title" label="模板名称" />
        <el-table-column prop="templateId" label="模板 ID" width="220" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="(v: boolean) => toggleTemplate(row, v)" />
          </template>
        </el-table-column>
      </el-table>
      <div class="muted" style="margin-top: 8px">模板 ID 由运营方在微信/短信服务商后台申请后回填。</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const channels = ref<any[]>([])
const sms = ref<any[]>([])
const templates = ref<any[]>([])
const testTarget = ref('13800000000')

async function load() {
  const data: any = await http.get('/tenant/message-config')
  channels.value = data.channels || []
  sms.value = data.sms || []
  templates.value = data.templates || []
}

async function saveChannel(row: any) {
  await http.put('/tenant/message-config/channel', { channel: row.channel, enabled: row.enabled, configJson: row.configJson })
  ElMessage.success('通道配置已保存')
}

async function toggleChannel(row: any, value: boolean) {
  row.enabled = value ? 1 : 0
  await saveChannel(row)
}

async function saveSms(row: any) {
  await http.put('/tenant/message-config/sms', { provider: row.provider, enabled: row.enabled, configJson: row.configJson })
  ElMessage.success('短信渠道已保存')
  load()
}

async function enableSms(row: any) {
  await http.put('/tenant/message-config/sms', { provider: row.provider, enabled: 1, configJson: row.configJson })
  ElMessage.success(`已启用 ${row.provider}，其他渠道自动停用`)
  load()
}

async function toggleTemplate(row: any, value: boolean) {
  await http.put(`/tenant/message-templates/${row.id}`, { ...row, enabled: value ? 1 : 0 })
  ElMessage.success('模板状态已更新')
}

async function test(type: string) {
  const data: any = await http.post('/tenant/message-config/test', { type, target: testTarget.value })
  if (data.degraded) ElMessage.warning(data.message)
  else ElMessage.success(data.message)
}

onMounted(load)
</script>
