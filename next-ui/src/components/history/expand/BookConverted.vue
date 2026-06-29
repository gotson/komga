<template>
  <HistoryExpandTable :rows="rows" />
</template>

<script setup lang="ts">
import type { HistoricalEventDto } from '@/generated/openapi'

const { event } = defineProps<{
  event: HistoricalEventDto
}>()

const rows = computed(() => [
  { header: 'Before', value: event.properties['former file'] },
  { header: 'After', value: event.properties.name },
])
</script>
