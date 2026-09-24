<template>
  <slot v-bind="reactiveData" />
</template>

<script setup lang="ts">
import { useGetLibrariesByViewId } from '@/composables/libraries'

const { libraryViewId } = defineProps<{
  libraryViewId?: string
}>()

const data = useGetLibrariesByViewId(libraryViewId || 'none')
// reactive() unwraps all computed refs, so we can v-bind them in the slot
const reactiveData = reactive(data)
</script>
