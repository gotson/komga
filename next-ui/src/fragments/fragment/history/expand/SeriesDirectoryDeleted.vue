<template>
  <fragment-history-expand-table :rows="rows" />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'

const { event } = defineProps<{
  event: components['schemas']['HistoricalEventDto']
}>()

const rows = computed(() => [
  { header: 'Directory', value: event.properties.name },
  { header: 'Reason', value: event.properties.reason },
])
</script>
