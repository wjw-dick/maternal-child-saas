<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="keyword" class="toolbar-search" clearable placeholder="租户名称 / 编码" @keyup.enter="reload" />
      <el-select v-model="status" clearable placeholder="状态" style="width: 140px" @change="reload">
        <el-option label="试用中" value="TRIAL" />
        <el-option label="正常" value="ACTIVE" />
        <el-option label="已停用" value="SUSPENDED" />
        <el-option label="已到期" value="EXPIRED" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button type="success" @click="openCreate">开通租户</el-button>
      <span class="toolbar-total">共 {{ total }} 个租户</span>
    </div>

    <div class="card">
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="tenantCode" label="租户编码" width="130" />
        <el-table-column prop="name" label="租户名称" min-width="170" />
        <el-table-column prop="industry" label="行业" width="110" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="planId" label="套餐" width="90" />
        <el-table-column label="配额（席位/公众号/存储GB/短信/API）" width="280">
          <template #default="{ row }">
            {{ row.seatLimit }} / {{ row.mpAccountLimit }} / {{ row.storageQuotaGb }} / {{ row.smsQuota }} / {{ row.apiQuota }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" width="170">
          <template #default="{ row }">{{ fmt(row.expireAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 'ACTIVE'" link type="success" @click="changeStatus(row, 'ACTIVE')">启用</el-button>
            <el-button v-else link type="warning" @click="changeStatus(row, 'SUSPENDED')">停用</el-button>
            <el-button link type="primary" @click="renew(row)">续费</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑租户' : '开通租户'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="110px">
        <el-form-item label="租户编码" required>
          <el-input v-model="form.tenantCode" :disabled="!!editingId" placeholder="小写字母/数字，如 yuebao" />
        </el-form-item>
        <el-form-item label="租户名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-input v-model="form.industry" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="form.planId" style="width: 100%">
            <el-option v-for="plan in plans" :key="plan.id" :label="`${plan.name}（${plan.price} 元/${plan.billingCycle}）`" :value="plan.id" />
          </el-select>
        </el-form-item>
        <template v-if="!editingId">
          <el-form-item label="管理员账号">
            <el-input v-model="form.adminUsername" placeholder="默认 admin" />
          </el-form-item>
          <el-form-item label="管理员密码">
            <el-input v-model="form.adminPassword" placeholder="默认 admin123" />
          </el-form-item>
          <el-form-item label="试用天数">
            <el-input-number v-model="form.trialDays" :min="0" :max="365" />
          </el-form-item>
        </template>
        <el-form-item v-else label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="试用中" value="TRIAL" />
            <el-option label="正常" value="ACTIVE" />
            <el-option label="已停用" value="SUSPENDED" />
            <el-option label="已到期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const plans = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<any>({})

function fmt(value: any) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function statusType(value: string) {
  if (value === 'ACTIVE') return 'success'
  if (value === 'TRIAL') return 'warning'
  if (value === 'SUSPENDED') return 'danger'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    const data: any = await http.get('/platform/tenants', {
      params: { page: page.value, size: size.value, keyword: keyword.value || undefined, status: status.value || undefined }
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

function resetForm() {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, {
    tenantCode: '',
    name: '',
    industry: '母婴护理',
    contact: '',
    phone: '',
    planId: undefined,
    adminUsername: 'admin',
    adminPassword: 'admin123',
    trialDays: 14,
    remark: ''
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: any) {
  editingId.value = row.id
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.tenantCode || !form.name) {
    ElMessage.warning('请填写租户编码和名称')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await http.put(`/platform/tenants/${editingId.value}`, form)
    } else {
      await http.post('/platform/tenants', form)
      ElMessage.success('租户已开通，默认角色/菜单/参数已初始化')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row: any, next: string) {
  await ElMessageBox.confirm(`确认将租户「${row.name}」状态调整为 ${next}？`, '状态变更', { type: 'warning' })
  await http.post(`/platform/tenants/${row.id}/status`, null, { params: { status: next } })
  ElMessage.success('状态已更新')
  load()
}

async function renew(row: any) {
  const { value } = await ElMessageBox.prompt('续费月数', `续费 - ${row.name}`, {
    inputValue: '12',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入数字'
  })
  await http.post(`/platform/tenants/${row.id}/renew`, null, { params: { months: Number(value) } })
  ElMessage.success('续费成功')
  load()
}

onMounted(async () => {
  load()
  const data: any = await http.get('/platform/tenants/plans')
  plans.value = data || []
})
</script>
