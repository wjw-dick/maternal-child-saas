<template>
  <div class="page">
    <div class="card">
      <el-tabs v-model="tab" @tab-change="onTabChange">
        <!-- 账号管理 -->
        <el-tab-pane label="账号管理" name="account">
          <div class="toolbar">
            <el-button type="primary" @click="openAccount()">新增公众号</el-button>
            <el-button @click="loadAccounts">刷新</el-button>
            <span class="muted">凭证唯一入口；真实微信 API 需运营方提供已认证主体与 AppSecret。</span>
          </div>
          <el-table :data="accounts" border size="small">
            <el-table-column prop="name" label="公众号名称" />
            <el-table-column prop="appId" label="AppID" width="200" />
            <el-table-column prop="wxId" label="原始 ID" width="180" />
            <el-table-column label="认证" width="80">
              <template #default="{ row }">
                <el-tag :type="row.verified === 1 ? 'success' : 'info'" size="small">{{ row.verified === 1 ? '已认证' : '未认证' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="API 用量" width="140">
              <template #default="{ row }">{{ row.apiUsed || 0 }} / {{ row.apiQuota || 0 }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAccount(row)">编辑</el-button>
                <el-button link type="warning" @click="resetQuota(row)">重置配额</el-button>
                <el-button link type="danger" @click="removeAccount(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="card-title" style="margin-top: 20px">小程序配置</div>
          <el-form :model="mini" label-width="120px" style="max-width: 640px">
            <el-form-item label="AppID"><el-input v-model="mini.appId" /></el-form-item>
            <el-form-item label="AppSecret"><el-input v-model="mini.appSecret" show-password placeholder="留空表示不修改" /></el-form-item>
            <el-form-item label="合法域名"><el-input v-model="mini.legalDomain" placeholder="https://demo.quantaida.cloud" /></el-form-item>
            <el-form-item><el-button type="primary" @click="saveMini">保存小程序配置</el-button></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 数据统计 -->
        <el-tab-pane label="数据统计" name="stats">
          <div class="toolbar">
            <el-button-group>
              <el-button v-for="d in [7, 15, 30]" :key="d" :type="statDays === d ? 'primary' : ''" @click="changeDays(d)">
                近 {{ d }} 天
              </el-button>
            </el-button-group>
            <span class="muted" style="margin-left: 12px">统计区间：{{ statDays }} 天</span>
          </div>
          <div class="stat-grid">
            <div class="stat-card"><div class="label">粉丝总数</div><div class="value">{{ stats.total || 0 }}</div></div>
            <div class="stat-card"><div class="label">已关注</div><div class="value success">{{ stats.subscribed || 0 }}</div></div>
            <div class="stat-card"><div class="label">新增粉丝</div><div class="value">{{ stats.newUsers || 0 }}</div></div>
            <div class="stat-card"><div class="label">消息（收/发）</div><div class="value">{{ stats.inMsg || 0 }} / {{ stats.outMsg || 0 }}</div></div>
            <div class="stat-card"><div class="label">API 调用</div><div class="value">{{ stats.apiUsed || 0 }} / {{ stats.apiQuota || 0 }}</div></div>
          </div>

          <div class="chart-grid">
            <div class="chart-card">
              <div class="chart-title">图 1 · 粉丝增减</div>
              <div v-if="!trendRows.length" class="chart-empty">暂无数据</div>
              <div v-else class="bars">
                <div v-for="p in trendRows" :key="p.date" class="bar-col" :title="`${p.date} 新增 ${p.value}`">
                  <div class="bar" :style="{ height: barHeight(p.value, trendMax) }"></div>
                  <div class="bar-label">{{ shortDate(p.date) }}</div>
                </div>
              </div>
            </div>

            <div class="chart-card">
              <div class="chart-title">图 2 · 累计用户</div>
              <div v-if="!trendRows.length" class="chart-empty">暂无数据</div>
              <div v-else class="bars">
                <div v-for="p in cumulativeRows" :key="p.date" class="bar-col" :title="`${p.date} 累计 ${p.value}`">
                  <div class="bar alt" :style="{ height: barHeight(p.value, cumulativeMax) }"></div>
                  <div class="bar-label">{{ shortDate(p.date) }}</div>
                </div>
              </div>
            </div>

            <div class="chart-card">
              <div class="chart-title">图 3 · 消息概况</div>
              <div v-if="!stats.inMsg && !stats.outMsg" class="chart-empty">暂无数据</div>
              <div v-else class="hbars">
                <div class="hrow"><span>接收</span><div class="htrack"><div class="hfill" :style="{ width: pct(stats.inMsg, msgMax) }"></div></div><b>{{ stats.inMsg || 0 }}</b></div>
                <div class="hrow"><span>发送</span><div class="htrack"><div class="hfill alt" :style="{ width: pct(stats.outMsg, msgMax) }"></div></div><b>{{ stats.outMsg || 0 }}</b></div>
              </div>
            </div>

            <div class="chart-card">
              <div class="chart-title">图 4 · 接口分析</div>
              <div v-if="!stats.apiUsed && !stats.apiQuota" class="chart-empty">暂无数据</div>
              <div v-else class="hbars">
                <div class="hrow"><span>已用</span><div class="htrack"><div class="hfill" :style="{ width: pct(stats.apiUsed, stats.apiQuota) }"></div></div><b>{{ stats.apiUsed || 0 }}</b></div>
                <div class="hrow"><span>剩余</span><div class="htrack"><div class="hfill alt" :style="{ width: pct((stats.apiQuota || 0) - (stats.apiUsed || 0), stats.apiQuota) }"></div></div><b>{{ (stats.apiQuota || 0) - (stats.apiUsed || 0) }}</b></div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 标签 -->
        <el-tab-pane label="标签管理" name="tags">
          <div class="toolbar">
            <el-input v-model="newTag" placeholder="新标签名称" style="width: 220px" />
            <el-button type="primary" @click="createTag">创建标签</el-button>
            <el-button @click="syncTags">同步标签</el-button>
          </div>
          <el-table :data="tags" border size="small">
            <el-table-column prop="wxTagId" label="微信标签 ID" width="150" />
            <el-table-column prop="name" label="标签名称" />
            <el-table-column prop="count" label="人数" width="100" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="danger" @click="removeTag(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 粉丝 -->
        <el-tab-pane label="粉丝管理" name="followers">
          <div class="toolbar">
            <el-input v-model="followerKeyword" placeholder="昵称 / OpenID" style="width: 240px" @keyup.enter="loadFollowers" />
            <el-button @click="loadFollowers">查询</el-button>
            <el-button type="primary" plain @click="syncFollowers">同步粉丝</el-button>
          </div>
          <el-table :data="followers" border size="small">
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="openid" label="OpenID" width="240" />
            <el-table-column prop="city" label="城市" width="100" />
            <el-table-column prop="tags" label="标签" width="160" />
            <el-table-column label="关注" width="90">
              <template #default="{ row }">
                <el-tag :type="row.subscribed === 1 ? 'success' : 'info'" size="small">{{ row.subscribed === 1 ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="tagFollower(row)">打标签</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            v-model:current-page="followerPage"
            :page-size="10"
            :total="followerTotal"
            layout="total, prev, pager, next"
            @current-change="loadFollowers"
          />
        </el-tab-pane>

        <!-- 消息 -->
        <el-tab-pane label="消息记录" name="messages">
          <div class="toolbar">
            <el-select v-model="msgType" clearable placeholder="消息类型" style="width: 180px" @change="loadMessages">
              <el-option label="文本" value="text" />
              <el-option label="图片" value="image" />
              <el-option label="事件" value="event" />
              <el-option label="菜单发布" value="MENU_PUBLISH" />
            </el-select>
            <el-button @click="loadMessages">刷新</el-button>
          </div>
          <el-table :data="messages" border size="small">
            <el-table-column prop="direction" label="方向" width="90" />
            <el-table-column prop="msgType" label="类型" width="130" />
            <el-table-column prop="openid" label="OpenID" width="220" />
            <el-table-column prop="content" label="内容" />
            <el-table-column prop="errCode" label="错误码" width="90" />
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            v-model:current-page="messagePage"
            :page-size="10"
            :total="messageTotal"
            layout="total, prev, pager, next"
            @current-change="loadMessages"
          />
        </el-tab-pane>

        <!-- 模板消息 -->
        <el-tab-pane label="模板消息" name="templates">
          <div class="toolbar">
            <el-button type="primary" @click="syncTemplates">同步模板</el-button>
            <span class="muted">仅同步/查看，模板 ID 需在微信后台申请。</span>
          </div>
          <el-table :data="templates" border size="small">
            <el-table-column prop="wxTemplateId" label="模板 ID" width="240" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="industry" label="行业" width="140" />
            <el-table-column prop="content" label="字段示例" />
          </el-table>
        </el-tab-pane>

        <!-- 菜单发布 -->
        <el-tab-pane label="菜单发布" name="menu">
          <div class="toolbar">
            <el-button type="primary" @click="publishMenu">发布菜单</el-button>
            <el-button type="danger" plain @click="clearMenu">清空菜单</el-button>
            <el-button @click="router.push('/tenant/menu-designer')">前往菜单设计器</el-button>
          </div>
          <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
            发布前校验一级菜单 ≤ 3、二级菜单 ≤ 5；发布后重新进入公众号可见。
          </el-alert>
          <el-table :data="menuRecords" border size="small">
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="wxResp" label="说明" />
            <el-table-column label="发布时间" width="180">
              <template #default="{ row }">{{ fmt(row.publishedAt) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 自动回复 -->
        <el-tab-pane label="自动回复" name="autoreply">
          <div class="toolbar">
            <el-button type="primary" @click="openReply()">新增规则</el-button>
          </div>
          <el-table :data="autoreplies" border size="small">
            <el-table-column prop="replyType" label="触发方式" width="120" />
            <el-table-column prop="keyword" label="关键词" width="160" />
            <el-table-column prop="msgType" label="回复类型" width="110" />
            <el-table-column prop="content" label="回复内容" />
            <el-table-column label="启用" width="90">
              <template #default="{ row }">{{ row.enabled === 1 ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="openReply(row)">编辑</el-button>
                <el-button link type="danger" @click="removeReply(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 素材 -->
        <el-tab-pane label="素材管理" name="material">
          <div class="toolbar">
            <el-button type="primary" @click="openMaterial()">新增素材</el-button>
            <span class="muted">视频素材必须填写封面 thumb 地址。</span>
          </div>
          <el-table :data="materials" border size="small">
            <el-table-column prop="materialType" label="类型" width="100" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="url" label="地址" />
            <el-table-column prop="mediaId" label="Media ID" width="200" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="danger" @click="removeMaterial(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 图文草稿 -->
        <el-tab-pane label="图文草稿" name="draft">
          <div class="toolbar">
            <el-button type="primary" @click="openDraft()">新建草稿</el-button>
          </div>
          <el-table :data="drafts" border size="small">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="创建时间" width="170">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDraft(row)">编辑</el-button>
                <el-button link type="success" @click="publishDraft(row)">发表</el-button>
              </template>
            </el-table-column>
          </el-table>

        </el-tab-pane>

        <el-tab-pane label="发表记录" name="publish">
          <el-table :data="publishRecords" border size="small">
            <el-table-column prop="draftId" label="草稿 ID" width="160" />
            <el-table-column prop="mediaId" label="Media ID" width="220" />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="发表时间" width="180">
              <template #default="{ row }">{{ fmt(row.publishedAt) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="accountVisible" title="公众号账号" width="640px">
      <el-form :model="accountForm" label-width="120px">
        <el-form-item label="公众号名称"><el-input v-model="accountForm.name" /></el-form-item>
        <el-form-item label="原始 ID"><el-input v-model="accountForm.wxId" /></el-form-item>
        <el-form-item label="AppID"><el-input v-model="accountForm.appId" /></el-form-item>
        <el-form-item label="AppSecret"><el-input v-model="accountForm.appSecret" show-password placeholder="留空表示不修改" /></el-form-item>
        <el-form-item label="服务器地址"><el-input v-model="accountForm.serverUrl" /></el-form-item>
        <el-form-item label="Token"><el-input v-model="accountForm.token" /></el-form-item>
        <el-form-item label="EncodingAESKey"><el-input v-model="accountForm.aesKey" /></el-form-item>
        <el-form-item label="业务域名"><el-input v-model="accountForm.authDomain" /></el-form-item>
        <el-form-item label="JS 安全域名"><el-input v-model="accountForm.jsDomain" /></el-form-item>
        <el-form-item label="是否认证">
          <el-switch v-model="accountForm.verified" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="accountForm.status" style="width: 100%">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAccount">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyVisible" title="自动回复规则" width="560px">
      <el-form :model="replyForm" label-width="110px">
        <el-form-item label="触发方式">
          <el-select v-model="replyForm.replyType" style="width: 100%">
            <el-option label="关注时" value="SUBSCRIBE" />
            <el-option label="关键词" value="KEYWORD" />
            <el-option label="收到消息" value="MESSAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词"><el-input v-model="replyForm.keyword" /></el-form-item>
        <el-form-item label="回复类型">
          <el-select v-model="replyForm.msgType" style="width: 100%">
            <el-option v-for="item in ['TEXT', 'IMAGE', 'NEWS', 'AUDIO', 'CARD']" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容"><el-input v-model="replyForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="replyForm.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReply">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="materialVisible" title="素材" width="560px">
      <el-form :model="materialForm" label-width="110px">
        <el-form-item label="素材类型">
          <el-select v-model="materialForm.materialType" style="width: 100%">
            <el-option v-for="item in ['IMAGE', 'VOICE', 'VIDEO', 'NEWS']" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="materialForm.name" /></el-form-item>
        <el-form-item label="文件地址"><el-input v-model="materialForm.url" /></el-form-item>
        <el-form-item label="视频封面"><el-input v-model="materialForm.thumbUrl" /></el-form-item>
        <el-form-item label="Media ID"><el-input v-model="materialForm.mediaId" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="materialVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMaterial">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="draftVisible" title="图文草稿" width="680px">
      <el-form :model="draftForm" label-width="100px">
        <el-form-item label="标题"><el-input v-model="draftForm.title" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="draftForm.author" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="draftForm.digest" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="封面"><el-input v-model="draftForm.coverUrl" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="draftForm.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="draftVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDraft">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

const router = useRouter()
const tab = ref('account')

const accounts = ref<any[]>([])
const mini = reactive<any>({ appId: '', appSecret: '', legalDomain: '' })
const stats = ref<any>({})
const statDays = ref(7)
const tags = ref<any[]>([])
const newTag = ref('')
const followers = ref<any[]>([])
const followerPage = ref(1)
const followerTotal = ref(0)
const followerKeyword = ref('')
const messages = ref<any[]>([])
const messagePage = ref(1)
const messageTotal = ref(0)
const msgType = ref('')
const templates = ref<any[]>([])
const menuRecords = ref<any[]>([])
const autoreplies = ref<any[]>([])
const materials = ref<any[]>([])
const drafts = ref<any[]>([])
const publishRecords = ref<any[]>([])

const accountVisible = ref(false)
const accountForm = reactive<any>({})
const replyVisible = ref(false)
const replyForm = reactive<any>({})
const materialVisible = ref(false)
const materialForm = reactive<any>({})
const draftVisible = ref(false)
const draftForm = reactive<any>({})

function fmt(value: any) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

async function loadAccounts() {
  accounts.value = (await http.get('/tenant/wx/accounts')) || []
  const data: any = await http.get('/tenant/wx/mini-config')
  Object.assign(mini, data || {})
}

async function loadStats() {
  stats.value = (await http.get('/tenant/wx/stats', { params: { days: statDays.value } })) || {}
}

function changeDays(days: number) {
  statDays.value = days
  loadStats()
}

const trendRows = computed<any[]>(() => stats.value.trend || [])
const trendMax = computed<number>(() => Math.max(1, ...trendRows.value.map((p: any) => Number(p.value) || 0)))
const cumulativeRows = computed<any[]>(() => {
  let sum = 0
  return trendRows.value.map((p: any) => {
    sum += Number(p.value) || 0
    return { date: p.date, value: sum }
  })
})
const cumulativeMax = computed<number>(() => Math.max(1, ...cumulativeRows.value.map((p: any) => p.value)))
const msgMax = computed<number>(() => Math.max(1, Number(stats.value.inMsg) || 0, Number(stats.value.outMsg) || 0))

function barHeight(value: any, max: number) {
  const ratio = (Number(value) || 0) / (max || 1)
  return Math.max(2, Math.round(ratio * 92)) + 'px'
}

function pct(value: any, total: any) {
  const ratio = (Number(value) || 0) / (Number(total) || 1)
  return Math.round(Math.min(1, ratio) * 100) + '%'
}

function shortDate(value: string) {
  return String(value || '').slice(5)
}

async function loadTags() {
  tags.value = (await http.get('/tenant/wx/tags')) || []
}

async function loadFollowers() {
  const data: any = await http.get('/tenant/wx/followers', {
    params: { page: followerPage.value, size: 10, keyword: followerKeyword.value || undefined }
  })
  followers.value = data.records || []
  followerTotal.value = data.total || 0
}

async function loadMessages() {
  const data: any = await http.get('/tenant/wx/messages', {
    params: { page: messagePage.value, size: 10, msgType: msgType.value || undefined }
  })
  messages.value = data.records || []
  messageTotal.value = data.total || 0
}

async function loadTemplates() {
  templates.value = (await http.get('/tenant/wx/templates')) || []
}

async function loadMenu() {
  const data: any = await http.get('/tenant/wx/menu')
  menuRecords.value = data.records || []
}

async function loadReplies() {
  autoreplies.value = (await http.get('/tenant/wx/autoreplies')) || []
}

async function loadMaterials() {
  materials.value = (await http.get('/tenant/wx/materials')) || []
}

async function loadDrafts() {
  drafts.value = (await http.get('/tenant/wx/drafts')) || []
  publishRecords.value = (await http.get('/tenant/wx/publish-records')) || []
}

const loaders: Record<string, () => Promise<void> | void> = {
  account: loadAccounts,
  stats: loadStats,
  tags: loadTags,
  followers: loadFollowers,
  messages: loadMessages,
  templates: loadTemplates,
  menu: loadMenu,
  autoreply: loadReplies,
  material: loadMaterials,
  draft: loadDrafts,
  publish: loadDrafts
}

function onTabChange(name: any) {
  loaders[name]?.()
}

function openAccount(row?: any) {
  Object.keys(accountForm).forEach((key) => delete accountForm[key])
  Object.assign(accountForm, {
    name: '',
    wxId: '',
    appId: '',
    appSecret: '',
    serverUrl: '',
    token: '',
    aesKey: '',
    authDomain: '',
    jsDomain: '',
    verified: 0,
    status: 'ACTIVE'
  }, row || {})
  if (row) accountForm.appSecret = ''
  accountVisible.value = true
}

async function saveAccount() {
  await http.post('/tenant/wx/accounts', accountForm)
  ElMessage.success('公众号已保存')
  accountVisible.value = false
  loadAccounts()
}

async function removeAccount(row: any) {
  await ElMessageBox.confirm(`确认删除公众号「${row.name}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/tenant/wx/accounts/${row.id}`)
  ElMessage.success('已删除')
  loadAccounts()
}

async function resetQuota(row: any) {
  await ElMessageBox.confirm('确认清空该公众号 API 配额用量？', '二次确认', { type: 'warning' })
  await http.post(`/tenant/wx/accounts/${row.id}/reset-quota`, null, { params: { confirm: true } })
  ElMessage.success('配额已重置')
  loadAccounts()
}

async function saveMini() {
  await http.post('/tenant/wx/mini-config', mini)
  ElMessage.success('小程序配置已保存')
  loadAccounts()
}

async function createTag() {
  if (!newTag.value) {
    ElMessage.warning('请输入标签名称')
    return
  }
  await http.post('/tenant/wx/tags', { name: newTag.value })
  newTag.value = ''
  ElMessage.success('标签已创建')
  loadTags()
}

async function removeTag(row: any) {
  await http.delete(`/tenant/wx/tags/${row.id}`)
  ElMessage.success('标签已删除')
  loadTags()
}

async function syncTags() {
  await http.post('/tenant/wx/tags/sync')
  ElMessage.success('标签已同步（本地台账）')
  loadTags()
}

async function syncFollowers() {
  const data: any = await http.post('/tenant/wx/followers/sync')
  ElMessage.success(data.message || '同步完成')
  loadFollowers()
}

async function tagFollower(row: any) {
  const { value } = await ElMessageBox.prompt('请输入标签（逗号分隔）', '粉丝打标签', {
    inputValue: row.tags || ''
  })
  await http.post(`/tenant/wx/followers/${row.id}/tags`, { tags: value })
  ElMessage.success('标签已更新')
  loadFollowers()
}

async function syncTemplates() {
  templates.value = (await http.post('/tenant/wx/templates/sync')) || []
  ElMessage.success('模板已同步（本地台账）')
}

async function publishMenu() {
  await http.post('/tenant/wx/menu/publish')
  ElMessage.success('菜单发布成功')
  loadMenu()
}

async function clearMenu() {
  await ElMessageBox.confirm('确认清空公众号菜单？', '二次确认', { type: 'warning' })
  await http.delete('/tenant/wx/menu', { params: { confirm: true } })
  ElMessage.success('已清空')
  loadMenu()
}

function openReply(row?: any) {
  Object.keys(replyForm).forEach((key) => delete replyForm[key])
  Object.assign(replyForm, { replyType: 'KEYWORD', keyword: '', msgType: 'TEXT', content: '', enabled: 1 }, row || {})
  replyVisible.value = true
}

async function saveReply() {
  await http.post('/tenant/wx/autoreplies', replyForm)
  ElMessage.success('规则已保存')
  replyVisible.value = false
  loadReplies()
}

async function removeReply(row: any) {
  await http.delete(`/tenant/wx/autoreplies/${row.id}`)
  ElMessage.success('已删除')
  loadReplies()
}

function openMaterial() {
  Object.keys(materialForm).forEach((key) => delete materialForm[key])
  Object.assign(materialForm, { materialType: 'IMAGE', name: '', url: '', thumbUrl: '', mediaId: '' })
  materialVisible.value = true
}

async function saveMaterial() {
  await http.post('/tenant/wx/materials', materialForm)
  ElMessage.success('素材已保存')
  materialVisible.value = false
  loadMaterials()
}

async function removeMaterial(row: any) {
  await http.delete(`/tenant/wx/materials/${row.id}`)
  ElMessage.success('已删除')
  loadMaterials()
}

function openDraft(row?: any) {
  Object.keys(draftForm).forEach((key) => delete draftForm[key])
  Object.assign(draftForm, { title: '', author: '', digest: '', coverUrl: '', content: '', status: 'DRAFT' }, row || {})
  draftVisible.value = true
}

async function saveDraft() {
  await http.post('/tenant/wx/drafts', draftForm)
  ElMessage.success('草稿已保存')
  draftVisible.value = false
  loadDrafts()
}

async function publishDraft(row: any) {
  await ElMessageBox.confirm('确认发表该草稿？', '发表确认', { type: 'warning' })
  await http.post(`/tenant/wx/drafts/${row.id}/publish`)
  ElMessage.success('已发表（本地台账）')
  loadDrafts()
}

onMounted(loadAccounts)
</script>

<style scoped>
.muted { color: #909399; font-size: 13px; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-top: 16px; }
.chart-card { border: 1px solid var(--el-border-color-lighter); border-radius: 8px; padding: 12px 16px; background: #fff; }
.chart-title { font-weight: 600; margin-bottom: 12px; }
.chart-empty { color: var(--el-text-color-secondary); text-align: center; padding: 40px 0; }
.bars { display: flex; align-items: flex-end; gap: 6px; height: 128px; overflow-x: auto; }
.bar-col { display: flex; flex-direction: column; align-items: center; justify-content: flex-end; min-width: 18px; flex: 0 0 auto; height: 100%; }
.bar { width: 14px; background: var(--el-color-primary); border-radius: 3px 3px 0 0; }
.bar.alt { background: #7cc6a1; }
.bar-label { font-size: 11px; color: #909399; margin-top: 4px; white-space: nowrap; }
.hbars { display: flex; flex-direction: column; gap: 14px; padding: 10px 0; }
.hrow { display: flex; align-items: center; gap: 10px; }
.hrow span { width: 36px; color: #606266; font-size: 13px; }
.htrack { flex: 1; height: 12px; background: #f0f2f5; border-radius: 6px; overflow: hidden; }
.hfill { height: 100%; background: var(--el-color-primary); }
.hfill.alt { background: #7cc6a1; }
.hrow b { width: 52px; text-align: right; font-size: 13px; }
@media (max-width: 900px) { .chart-grid { grid-template-columns: 1fr; } }
</style>
