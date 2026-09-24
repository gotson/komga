<template>
  <slot :details="data" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { readListDetailQuery } from '@/colada/readlists'

const { readListId } = defineProps<{
  readListId?: string
}>()

const { data } = useQuery(() => ({
  ...readListDetailQuery({ readListId: readListId || 'none' }),
  enabled: !!readListId,
}))
</script>
