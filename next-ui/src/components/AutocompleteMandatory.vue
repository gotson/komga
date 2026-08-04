<template>
  <v-autocomplete
    ref="field"
    v-model:search="search"
    v-model="selection"
    variant="outlined"
    density="compact"
    hide-details
    max-width="100"
    :items="items"
  />
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

const emit = defineEmits(['selected'])
defineProps({ items: Array })

const field = ref()
const search = ref('')
const selection = ref(null)
watch(selection, (v) => {
  if (!v) return
  emit('selected', v)
  void nextTick(() => {
    search.value = ''
    selection.value = null
    setTimeout(() => field.value?.blur(), 100)
  })
})
</script>
