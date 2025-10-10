<template>
  <FragmentHistoryExpandTable :rows="rows" />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'

const { event } = defineProps<{
  event: components['schemas']['HistoricalEventDto']
}>()

const rows = computed(() => [
  { header: 'Before', value: event.properties['former file'] },
  { header: 'After', value: event.properties.name },
])
</script>
