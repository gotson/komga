<template>
  <slot :details="data" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { bookDetailQuery } from '@/colada/books'

const { bookId } = defineProps<{
  bookId?: string
}>()

const { data } = useQuery(() => ({
  ...bookDetailQuery({ bookId: bookId || 'none' }),
  enabled: !!bookId,
}))
</script>
