<template>
  <HistoryExpandTable :rows="rows" />
</template>

<script setup lang="ts">
import type { HistoricalEventDto } from '@/generated/openapi'

const { event } = defineProps<{
  event: HistoricalEventDto
}>()

const rows = computed(() => [
  { header: 'File', value: event.properties.name },
  { header: 'Reason', value: event.properties.reason },
])
</script>
