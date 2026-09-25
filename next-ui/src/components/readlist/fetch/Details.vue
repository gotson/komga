<template>
  <slot v-bind="reactiveData" />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { readListDetailQuery } from '@/colada/readlists'

const { readListId } = defineProps<{
  readListId?: string
}>()

const data = useQuery(() => ({
  ...readListDetailQuery({ readListId: readListId || 'none' }),
  enabled: !!readListId,
}))
// reactive() unwraps all computed refs, so we can v-bind them in the slot
const reactiveData = reactive(data)
</script>
