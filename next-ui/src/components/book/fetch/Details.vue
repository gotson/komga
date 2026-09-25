<template>
  <slot v-bind="reactiveData" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { bookDetailQuery } from '@/colada/books'

const { bookId } = defineProps<{
  bookId?: string
}>()

const data = useQuery(() => ({
  ...bookDetailQuery({ bookId: bookId || 'none' }),
  enabled: !!bookId,
}))
// reactive() unwraps all computed refs, so we can v-bind them in the slot
const reactiveData = reactive(data)
</script>
