<template>
  <div class="page">
    <div class="card">
      <div class="card-title">菜单可视化配置</div>
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 14px">
        菜单为多端唯一出处，保存后实时生效；公众号菜单发布前会校验一级 ≤ 3、二级 ≤ 5。
      </el-alert>
      <el-tabs v-model="terminal" @tab-change="switchTerminal">
        <el-tab-pane v-for="item in terminals" :key="item.value" :label="item.label" :name="item.value" />
      </el-tabs>

      <div class="grid-2">
        <div>
          <div class="sub-title">JSON 配置</div>
          <el-input v-model="treeJson" type="textarea" :rows="18" spellcheck="false" />
          <div class="toolbar" style="margin-top: 12px">
            <el-button @click="format">格式化</el-button>
            <el-button @click="loadTemplate">载入模板</el-button>
            <el-button type="primary" :loading="saving" @click="save">保存并生效</el-button>
            <el-button type="danger" plain @click="reset">清空菜单</el-button>
          </div>
        </div>
        <div>
          <div class="sub-title">实时预览</div>
          <div class="phone-frame">
            <div class="phone-title">{{ currentLabel }}</div>
            <div class="menu-preview">
              <div v-for="(node, index) in preview" :key="index" class="menu-node">
                <div class="menu-node-title">{{ node.name || node.title || '未命名' }}</div>
                <div v-if="node.sub_button?.length || node.children?.length" class="menu-children">
                  <span v-for="(child, ci) in node.sub_button || node.children" :key="ci">
                    {{ child.name || child.title || '子菜单' }}
                  </span>
                </div>
              </div>
              <el-empty v-if="!preview.length" description="暂无菜单" :image-size="70" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

const terminals = [
  { label: '微信公众号', value: 'WECHAT' },
  { label: 'PC 官网', value: 'PC_SITE' },
  { label: 'PC 后台', value: 'PC_ADMIN' },
  { label: '小程序 tabBar', value: 'MINI_TABBAR' },
  { label: 'H5 底部导航', value: 'H5_FOOTER' },
  { label: 'App 底部导航', value: 'APP_TABBAR' }
]
const templates: Record<string, any> = {
  WECHAT: {
    button: [
      { type: 'view', name: '预约服务', url: 'https://demo.quantaida.cloud/h5/appointment' },
      { name: '会员中心', sub_button: [{ type: 'view', name: '我的订单', url: 'https://demo.quantaida.cloud/h5/orders' }, { type: 'view', name: '宝宝档案', url: 'https://demo.quantaida.cloud/h5/babies' }] },
      { type: 'click', name: '联系客服', key: 'CONTACT' }
    ]
  },
  MINI_TABBAR: [
    { pagePath: '/pages/index/index', text: '首页' },
    { pagePath: '/pages/service/index', text: '服务' },
    { pagePath: '/pages/order/index', text: '订单' },
    { pagePath: '/pages/mine/index', text: '我的' }
  ]
}
const defaultTemplate = [
  { name: '首页', path: '/' },
  { name: '服务', path: '/services' },
  { name: '会员', path: '/member' }
]

const terminal = ref('WECHAT')
const treeJson = ref('[]')
const saving = ref(false)
const configs = ref<Record<string, any>>({})

const currentLabel = computed(() => terminals.find((item) => item.value === terminal.value)?.label || '')
const preview = computed(() => {
  try {
    const data = JSON.parse(treeJson.value || '[]')
    if (Array.isArray(data)) return data
    return data.button || data.menus || []
  } catch {
    return []
  }
})

function switchTerminal() {
  const exists = configs.value[terminal.value]
  treeJson.value = exists?.treeJson || JSON.stringify(templates[terminal.value] || defaultTemplate, null, 2)
}

function format() {
  try {
    treeJson.value = JSON.stringify(JSON.parse(treeJson.value), null, 2)
    ElMessage.success('已格式化')
  } catch {
    ElMessage.error('JSON 格式非法')
  }
}

function loadTemplate() {
  treeJson.value = JSON.stringify(templates[terminal.value] || defaultTemplate, null, 2)
}

async function save() {
  try {
    JSON.parse(treeJson.value)
  } catch {
    ElMessage.error('JSON 格式非法，无法保存')
    return
  }
  saving.value = true
  try {
    await http.put(`/tenant/menu-config/${terminal.value}`, { treeJson: treeJson.value })
    ElMessage.success('菜单已保存并实时生效')
    await refresh()
  } finally {
    saving.value = false
  }
}

async function reset() {
  await ElMessageBox.confirm('清空后该端将使用系统默认菜单，确认继续？', '二次确认', { type: 'warning' })
  await http.delete(`/tenant/menu-config/${terminal.value}`, { params: { confirm: true } })
  ElMessage.success('已清空')
  treeJson.value = '[]'
  await refresh()
}

async function refresh() {
  const list: any = await http.get('/tenant/menu-config')
  configs.value = {}
  ;(list || []).forEach((item: any) => {
    configs.value[item.terminal] = item
  })
  switchTerminal()
}

onMounted(refresh)
</script>

<style scoped>
.sub-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #33415c;
}
.phone-frame {
  border: 1px solid #e1e7f0;
  border-radius: 18px;
  padding: 14px;
  min-height: 420px;
  background: #fbfcfe;
}
.phone-title {
  text-align: center;
  color: #8a94a6;
  font-size: 12px;
  margin-bottom: 12px;
}
.menu-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.menu-node {
  background: #fff;
  border: 1px dashed #dfe6f1;
  border-radius: 10px;
  padding: 10px 12px;
}
.menu-node-title {
  font-weight: 600;
  color: #33415c;
}
.menu-children {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.menu-children span {
  background: #f0f4fb;
  color: #5a6a85;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
}
</style>
