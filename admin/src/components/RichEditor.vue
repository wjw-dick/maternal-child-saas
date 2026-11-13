<template>
  <div class="rich-editor">
    <Toolbar
      class="rich-editor__toolbar"
      :editor="editorRef"
      :default-config="toolbarConfig"
      :mode="mode"
    />
    <Editor
      v-model="html"
      class="rich-editor__body"
      :style="{ height: height }"
      :default-config="editorConfig"
      :mode="mode"
      @on-created="handleCreated"
    />
  </div>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { uploadFile } from '../api/upload'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    height?: string
    placeholder?: string
    lang?: 'zh-CN' | 'en'
    maxImageSizeMb?: number
  }>(),
  {
    modelValue: '',
    height: '360px',
    placeholder: '请输入内容…',
    lang: 'zh-CN',
    maxImageSizeMb: 10
  }
)

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const editorRef = shallowRef<IDomEditor>()
const html = ref(props.modelValue || '')
const mode = 'default'

const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: ['group-video', 'fullScreen']
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  scroll: true,
  MENU_CONF: {
    uploadImage: {
      // 图片统一走租户存储，编辑器内粘贴/拖拽/选择图片都会触发
      async customUpload(file: File, insertFn: (url: string, alt: string, href: string) => void) {
        if (file.size > props.maxImageSizeMb * 1024 * 1024) {
          throw new Error(`图片不能超过 ${props.maxImageSizeMb}MB`)
        }
        const result = await uploadFile(file, 'richtext')
        insertFn(result.url, result.name, result.url)
      }
    }
  }
}

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
  if (props.modelValue) editor.setHtml(props.modelValue)
}

watch(
  () => props.modelValue,
  (value) => {
    if (value !== html.value) {
      html.value = value || ''
      if (editorRef.value && editorRef.value.getHtml() !== html.value) {
        editorRef.value.setHtml(html.value)
      }
    }
  }
)

watch(html, (value) => {
  const next = value || ''
  if (next !== (props.modelValue || '')) emit('update:modelValue', next)
})

onBeforeUnmount(() => {
  editorRef.value?.destroy()
  editorRef.value = undefined
})
</script>

<style scoped>
.rich-editor {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
  z-index: 1;
}
.rich-editor__toolbar {
  border-bottom: 1px solid var(--el-border-color);
}
.rich-editor__body {
  overflow-y: auto;
}
</style>
