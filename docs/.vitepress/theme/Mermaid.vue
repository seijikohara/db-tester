<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useData } from 'vitepress'

const props = defineProps<{
  id: string
  // URI-encoded so that Vue does not interpret diagram text as template syntax.
  graph: string
}>()

const { isDark } = useData()
const svg = ref('')

const render = async () => {
  // Import lazily: mermaid needs the DOM, and a static import would also pull it into the main bundle.
  const { default: mermaid } = await import('mermaid')
  mermaid.initialize({
    startOnLoad: false,
    theme: isDark.value ? 'dark' : 'neutral',
    // Mermaid 12 defaults to the ELK layout and the neo look; keep the pre-12 rendering.
    layout: 'dagre',
    look: 'classic',
  })
  const result = await mermaid.render(props.id, decodeURIComponent(props.graph))
  svg.value = result.svg
}

onMounted(render)
watch(isDark, render)
</script>

<template>
  <div class="mermaid" v-html="svg"></div>
</template>
