<template>
  <slot :details="data" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { collectionDetailQuery } from '@/colada/collections'

const { collectionId } = defineProps<{
  collectionId?: string
}>()

const { data } = useQuery(() => ({
  ...collectionDetailQuery({ collectionId: collectionId || 'none' }),
  enabled: !!collectionId,
}))
</script>
