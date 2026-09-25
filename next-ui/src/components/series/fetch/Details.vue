<template>
  <slot v-bind="reactiveData" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { seriesDetailQuery } from '@/colada/series'

const { seriesId } = defineProps<{
  seriesId?: string
}>()

const data = useQuery(() => ({
  ...seriesDetailQuery({ seriesId: seriesId || 'none' }),
  enabled: !!seriesId,
}))
// reactive() unwraps all computed refs, so we can v-bind them in the slot
const reactiveData = reactive(data)
</script>
