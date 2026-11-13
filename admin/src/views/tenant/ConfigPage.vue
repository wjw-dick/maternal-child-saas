<template>
  <div class="page">
    <div class="card">
      <div class="card-title">全局参数</div>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        参数按租户隔离保存，保存后立即生效；敏感参数建议使用加密分组。
      </el-alert>
      <el-table :data="rows" border size="small">
        <el-table-column prop="configKey" label="参数键" width="280" />
        <el-table-column label="参数值">
          <template #default="{ row }">
            <el-input v-model="row.configValue" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="groupCode" label="分组" width="130" />
        <el-table-column prop="remark" label="说明" width="220" />
      </el-table>
      <div class="toolbar" style="margin-top: 14px">
        <el-input v-model="newKey" placeholder="新增参数键" style="width: 220px" />
        <el-input v-model="newValue" placeholder="参数值" style="width: 220px" />
        <el-button @click="addRow">添加参数</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存全部</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

const rows = ref<Array<{ configKey: string; configValue: string; groupCode: string; remark: string }>>([])
const newKey = ref('')
const newValue = ref('')
const saving = ref(false)

function addRow() {
  if (!newKey.value) {
    ElMessage.warning('请输入参数键')
    return
  }
  rows.value.push({ configKey: newKey.value, configValue: newValue.value, groupCode: 'custom', remark: '自定义参数' })
  newKey.value = ''
  newValue.value = ''
}

async function save() {
  const payload: Record<string, string> = {}
  rows.value.forEach((row) => {
    payload[row.configKey] = row.configValue ?? ''
  })
  saving.value = true
  try {
    await http.put('/tenant/config', payload)
    ElMessage.success('参数已保存并生效')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const data: any = await http.get('/tenant/config')
  rows.value = Object.entries(data || {}).map(([key, value]) => ({
    configKey: key,
    configValue: value as string,
    groupCode: key.split('_')[0],
    remark: ''
  }))
})
</script>
