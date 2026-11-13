<template>
  <div class="page">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        class="toolbar-search"
        clearable
        placeholder="输入关键词搜索"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button v-if="!resource.readonly" type="success" @click="openCreate">新增</el-button>
      <span class="toolbar-total">共 {{ total }} 条</span>
    </div>

    <div class="card">
      <el-table v-loading="loading" :data="rows" border stripe row-key="id">
        <el-table-column
          v-for="col in resource.columns"
          :key="col.key"
          :prop="col.key"
          :label="col.label"
          :width="col.width"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-tag v-if="isStatus(col)" :type="statusType(row[col.key])" size="small">
              {{ row[col.key] || '-' }}
            </el-tag>
            <span v-else>{{ display(row[col.key], col) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!resource.readonly || resource.actions?.length" label="操作" :width="actionWidth" fixed="right">
          <template #default="{ row }">
            <template v-if="!resource.readonly">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
            <el-button
              v-for="action in resource.actions || []"
              :key="action.label"
              link
              :type="action.type || 'primary'"
              @click="runAction(action, row)"
            >
              {{ action.label }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑' : '新增'"
      :width="resource.form?.some((item) => item.type === 'richtext') ? '900px' : '680px'"
      destroy-on-close
    >
      <el-form ref="formRef" :model="model" label-width="120px">
        <el-form-item
          v-for="field in resource.form || []"
          :key="field.key"
          :label="field.label"
          :required="field.required"
          :prop="field.key"
        >
          <el-input-number
            v-if="field.type === 'number'"
            v-model="model[field.key]"
            :controls="false"
            style="width: 100%"
          />
          <el-select
            v-else-if="field.type === 'select'"
            v-model="model[field.key]"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="option in field.options || []"
              :key="String(option.value)"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="model[field.key]"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
          <el-date-picker
            v-else-if="field.type === 'datetime'"
            v-model="model[field.key]"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
          <el-input
            v-else-if="field.type === 'textarea'"
            v-model="model[field.key]"
            type="textarea"
            :rows="4"
          />
          <RichEditor
            v-else-if="field.type === 'richtext'"
            v-model="model[field.key]"
            :height="'380px'"
            :placeholder="field.placeholder || `请输入${field.label}`"
          />
          <FileUpload
            v-else-if="field.type === 'upload' || field.type === 'attachments'"
            v-model="model[field.key]"
            :multiple="field.type === 'attachments' || field.multiple"
            :accept="field.accept || ''"
            :biz-type="field.bizType || field.key"
          />
          <el-input v-else v-model="model[field.key]" :placeholder="field.placeholder" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import RichEditor from '../components/RichEditor.vue'
import FileUpload from '../components/FileUpload.vue'
import { resources, type Field, type Resource, type RowAction } from '../resources'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const model = ref<Record<string, any>>({})
const formRef = ref()

const resource = computed<Resource>(() => {
  const key = String(route.meta.resource || '')
  return resources[key] || { api: '', columns: [], readonly: true }
})

const actionWidth = computed(() => {
  const count = (resource.value.readonly ? 0 : 2) + (resource.value.actions?.length || 0)
  return Math.max(110, 70 + count * 68)
})

function display(value: any, col: Field) {
  if (value === null || value === undefined || value === '') return '-'
  if (col.type === 'datetime') return String(value).replace('T', ' ').slice(0, 19)
  return value
}

function isStatus(col: Field) {
  return ['status', 'payStatus'].includes(col.key)
}

function statusType(value: string) {
  if (['ACTIVE', 'PAID', 'COMPLETED', 'PUBLISHED', 'SUCCESS', 'CONFIRMED', 'SETTLED'].includes(value)) return 'success'
  if (['SUSPENDED', 'CANCELLED', 'REFUNDED', 'DISABLED', 'FAILED'].includes(value)) return 'danger'
  if (['PENDING', 'TRIAL', 'DRAFT', 'PROCESSING'].includes(value)) return 'warning'
  return 'info'
}

async function load() {
  if (!resource.value.api) return
  loading.value = true
  try {
    const data: any = await http.get(resource.value.api, {
      params: { page: page.value, size: size.value, keyword: keyword.value || undefined }
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  const initial: Record<string, any> = {}
  ;(resource.value.form || []).forEach((field) => {
    if (field.type === 'number') initial[field.key] = undefined
    else initial[field.key] = ''
  })
  model.value = initial
  dialogVisible.value = true
}

function openEdit(row: any) {
  editingId.value = row.id
  model.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  const missing = (resource.value.form || []).find(
    (field) => field.required && (model.value[field.key] === undefined || model.value[field.key] === '')
  )
  if (missing) {
    ElMessage.warning(`请填写${missing.label}`)
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await http.put(`${resource.value.api}/${editingId.value}`, model.value)
    } else {
      await http.post(resource.value.api, model.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm('删除后不可恢复，确认继续？', '删除确认', { type: 'warning' })
  await http.delete(`${resource.value.api}/${row.id}`)
  ElMessage.success('已删除')
  load()
}

async function runAction(action: RowAction, row: any) {
  let input: string | undefined
  if (action.ask) {
    const { value } = await ElMessageBox.prompt(action.ask, action.label, {
      inputValue: action.askDefault,
      inputPattern: /\S+/,
      inputErrorMessage: '请输入有效内容'
    })
    input = value
  } else if (action.confirm) {
    await ElMessageBox.confirm(action.confirm, action.label, { type: 'warning' })
  }
  const path = action.path(row, input)
  const method = action.method || 'post'
  await (http as any)[method](path, action.body ? action.body(row, input) : undefined)
  ElMessage.success(`${action.label}成功`)
  load()
}

watch(
  () => route.meta.resource,
  () => {
    keyword.value = ''
    page.value = 1
    load()
  }
)

onMounted(load)
</script>
