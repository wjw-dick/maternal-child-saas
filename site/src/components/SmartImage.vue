<template>
  <div class="smart-image" :class="{ 'is-loaded': loaded, 'is-failed': failed }" :style="{ aspectRatio: ratio }">
    <img
      v-if="imageSrc && !failed"
      :src="imageSrc"
      :alt="alt"
      loading="lazy"
      @load="loaded = true"
      @error="failed = true"
    />
    <div v-if="!loaded || failed" class="smart-image__fallback" aria-hidden="true">
      <span>{{ initial }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { assetUrl } from '../api'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  ratio: { type: String, default: '4 / 3' }
})

const loaded = ref(false)
const failed = ref(false)
const resolved = computed(() => assetUrl(props.src))
const initial = computed(() => (props.alt || '悦').trim().slice(0, 1))

watch(resolved, () => {
  loaded.value = false
  failed.value = false
})

// 模板中使用解析后的地址
const imageSrc = computed(() => resolved.value)
</script>
