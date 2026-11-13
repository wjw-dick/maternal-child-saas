<template>
  <div class="file-upload">
    <div
      v-if="multiple || items.length === 0"
      class="file-upload__drop"
      :class="{ 'is-dragging': dragging }"
      @click="pick"
      @dragover.prevent="dragging = true"
      @dragenter.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <el-icon class="file-upload__icon"><UploadFilled /></el-icon>
      <div class="file-upload__text">
        将文件拖到此处，或<em>点击选择</em>
      </div>
      <div class="file-upload__tip">{{ tip || defaultTip }}</div>
    </div>
    <input ref="inputRef" type="file" hidden :accept="accept" :multiple="multiple" @change="onChange" />

    <div v-if="items.length" class="file-upload__list">
      <div
        v-for="(item, index) in items"
        :key="item.key"
        class="file-upload__item"
        :class="{ 'is-error': item.status === 'error' }"
        draggable="true"
        @dragstart="dragIndex = index"
        @dragover.prevent
        @drop.prevent="dropTo(index)"
      >
        <div class="file-upload__thumb" @click="preview(item)">
          <img v-if="isImage(item.url)" :src="item.url" :alt="item.name" />
          <el-icon v-else class="file-upload__file-icon"><Document /></el-icon>
          <div v-if="item.status === 'uploading'" class="file-upload__mask">
            <el-progress type="circle" :percentage="item.progress" :width="46" :stroke-width="4" />
          </div>
          <div v-else-if="item.status === 'error'" class="file-upload__mask is-error">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div v-else class="file-upload__zoom">
            <el-icon><ZoomIn /></el-icon>
          </div>
        </div>

        <div class="file-upload__meta">
          <span class="file-upload__name" :title="item.name">{{ item.name || '未命名文件' }}</span>
          <el-progress
            v-if="item.status === 'uploading'"
            :percentage="item.progress"
            :stroke-width="4"
            :show-text="false"
          />
          <span v-else-if="item.status === 'error'" class="file-upload__error">{{ item.error || '上传失败' }}</span>
          <span v-else class="file-upload__size">{{ readableSize(item.size) }}</span>
        </div>

        <div class="file-upload__ops">
          <el-tooltip content="重试" placement="top">
            <el-button v-if="item.status === 'error'" link type="primary" @click="retry(item)">
              <el-icon><RefreshRight /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip v-if="multiple" content="上移" placement="top">
            <el-button link :disabled="index === 0" @click="move(index, -1)">
              <el-icon><Top /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip v-if="multiple" content="下移" placement="top">
            <el-button link :disabled="index === items.length - 1" @click="move(index, 1)">
              <el-icon><Bottom /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="danger" @click="remove(index)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <el-image-viewer
      v-if="viewerVisible"
      :url-list="imageItems"
      :initial-index="viewerIndex"
      teleported
      @close="viewerVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile, isImage } from '../api/upload'

interface Item {
  key: string
  name: string
  url: string
  size: number
  status: 'done' | 'uploading' | 'error'
  progress: number
  error?: string
  file?: File
}

const props = withDefaults(
  defineProps<{
    modelValue?: string | string[]
    multiple?: boolean
    accept?: string
    maxCount?: number
    maxSizeMb?: number
    bizType?: string
    tip?: string
  }>(),
  {
    modelValue: '',
    multiple: false,
    accept: '',
    maxCount: 9,
    maxSizeMb: 20,
    bizType: 'common',
    tip: ''
  }
)

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const inputRef = ref<HTMLInputElement>()
const dragging = ref(false)
const dragIndex = ref(-1)
const viewerVisible = ref(false)
const viewerIndex = ref(0)
const items = ref<Item[]>([])
let syncing = false
let seed = 0

const defaultTip = computed(() => {
  const types = props.accept ? props.accept.replace(/image\/\*/g, '图片') : '文件'
  return `支持 ${types}，单个不超过 ${props.maxSizeMb}MB${props.multiple ? `，最多 ${props.maxCount} 个` : ''}`
})

const imageItems = computed(() => items.value.filter((item) => isImage(item.url)).map((item) => item.url))

function parse(value?: string | string[]): string[] {
  if (Array.isArray(value)) return value.filter(Boolean)
  if (!value) return []
  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function toItem(url: string): Item {
  const name = decodeURIComponent(url.split('?')[0].split('/').pop() || '文件')
  return { key: `f_${++seed}`, name, url, size: 0, status: 'done', progress: 100 }
}

function syncFromModel(value?: string | string[]) {
  const urls = parse(value)
  const current = items.value.filter((item) => item.status === 'done').map((item) => item.url)
  if (urls.join('|') === current.join('|') && items.value.every((item) => item.status === 'done')) {
    return
  }
  syncing = true
  items.value = urls.map(toItem)
  syncing = false
}

watch(() => props.modelValue, syncFromModel, { immediate: true })

function emitModel() {
  if (syncing) return
  const urls = items.value.filter((item) => item.status === 'done').map((item) => item.url)
  emit('update:modelValue', urls.join(','))
}

function pick() {
  inputRef.value?.click()
}

function onChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  addFiles(files)
}

function onDrop(event: DragEvent) {
  dragging.value = false
  const files = Array.from(event.dataTransfer?.files || [])
  addFiles(files)
}

function addFiles(files: File[]) {
  if (!files.length) return
  if (!props.multiple) {
    items.value = []
  } else if (items.value.length + files.length > props.maxCount) {
    ElMessage.warning(`最多上传 ${props.maxCount} 个文件`)
    files = files.slice(0, Math.max(0, props.maxCount - items.value.length))
  }
  files.forEach((file) => {
    if (file.size > props.maxSizeMb * 1024 * 1024) {
      ElMessage.error(`${file.name} 超过 ${props.maxSizeMb}MB`)
      return
    }
    const item: Item = {
      key: `f_${++seed}`,
      name: file.name,
      url: '',
      size: file.size,
      status: 'uploading',
      progress: 0,
      file
    }
    if (props.multiple) {
      items.value.push(item)
    } else {
      items.value = [item]
    }
    doUpload(item)
  })
}

async function doUpload(item: Item) {
  if (!item.file) return
  item.status = 'uploading'
  item.progress = 0
  item.error = undefined
  try {
    const result = await uploadFile(item.file, props.bizType, (percent) => {
      item.progress = percent
    })
    item.url = result.url
    item.name = result.name || item.name
    item.size = result.size || item.size
    item.status = 'done'
    item.progress = 100
    emitModel()
  } catch (error: any) {
    item.status = 'error'
    item.error = error?.msg || '上传失败'
  }
}

function retry(item: Item) {
  if (!item.file) {
    ElMessage.warning('源文件已丢失，请重新选择')
    return
  }
  doUpload(item)
}

function remove(index: number) {
  items.value.splice(index, 1)
  emitModel()
}

function move(index: number, offset: number) {
  const target = index + offset
  if (target < 0 || target >= items.value.length) return
  const list = items.value
  ;[list[index], list[target]] = [list[target], list[index]]
  emitModel()
}

function dropTo(index: number) {
  const from = dragIndex.value
  dragIndex.value = -1
  if (from < 0 || from === index) return
  const list = items.value
  const [moved] = list.splice(from, 1)
  list.splice(index, 0, moved)
  emitModel()
}

function preview(item: Item) {
  if (!isImage(item.url) || item.status !== 'done') return
  viewerIndex.value = Math.max(0, imageItems.value.indexOf(item.url))
  viewerVisible.value = true
}

function readableSize(size: number) {
  if (!size) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}
.file-upload__drop {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  padding: 18px 12px;
  text-align: center;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-lighter);
  transition: all 0.2s;
}
.file-upload__drop:hover,
.file-upload__drop.is-dragging {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.file-upload__icon {
  font-size: 30px;
}
.file-upload__text {
  margin-top: 6px;
  font-size: 14px;
}
.file-upload__text em {
  color: var(--el-color-primary);
  font-style: normal;
}
.file-upload__tip {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.75;
}
.file-upload__list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}
.file-upload__item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  width: 236px;
  padding: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: #fff;
}
.file-upload__item.is-error {
  border-color: var(--el-color-danger-light-5);
}
.file-upload__thumb {
  position: relative;
  flex: 0 0 56px;
  width: 56px;
  height: 56px;
  border-radius: 4px;
  overflow: hidden;
  background: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.file-upload__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.file-upload__file-icon {
  font-size: 26px;
  color: var(--el-text-color-secondary);
}
.file-upload__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.82);
}
.file-upload__mask.is-error {
  color: var(--el-color-danger);
  font-size: 22px;
}
.file-upload__zoom {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(0, 0, 0, 0.35);
  opacity: 0;
  transition: opacity 0.2s;
}
.file-upload__thumb:hover .file-upload__zoom {
  opacity: 1;
}
.file-upload__meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.file-upload__name {
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.file-upload__size,
.file-upload__error {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.file-upload__error {
  color: var(--el-color-danger);
}
.file-upload__ops {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.file-upload__ops .el-button {
  height: 18px;
  padding: 0;
}
</style>
