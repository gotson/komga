<template>
  <HistoryExpandTable :rows="rows" />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'

const { event } = defineProps<{
  event: components['schemas']['HistoricalEventDto']
}>()

const rows = computed(() => [
  { header: 'File', value: event.properties.name },
  { header: 'Source file', value: event.properties.source },
  { header: 'Upgrade', value: event.properties.upgrade },
])
</script>
