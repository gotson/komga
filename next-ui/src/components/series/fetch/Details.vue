<template>
  <slot :details="data" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { seriesDetailQuery } from '@/colada/series'

const { seriesId } = defineProps<{
  seriesId?: string
}>()

const { data } = useQuery(() => ({
  ...seriesDetailQuery({ seriesId: seriesId || 'none' }),
  enabled: !!seriesId,
}))
</script>
