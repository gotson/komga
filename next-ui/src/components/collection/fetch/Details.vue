<template>
  <slot v-bind="reactiveData" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { collectionDetailQuery } from '@/colada/collections'

const { collectionId } = defineProps<{
  collectionId?: string
}>()

const data = useQuery(() => ({
  ...collectionDetailQuery({ collectionId: collectionId || 'none' }),
  enabled: !!collectionId,
}))
// reactive() unwraps all computed refs, so we can v-bind them in the slot
const reactiveData = reactive(data)
</script>
