<template>
  <FilterTriState
    :model-value="effectiveModel"
    :label="label"
    @change="(newValue) => handleChange(newValue)"
  />
</template>

<script setup lang="ts">
import { type FilterIncludeExclude } from '@/types/filter'
import type { IncludeExclude } from '@/components/filter/TriState.vue'

const model = defineModel<FilterIncludeExclude>({ required: true })

const { label } = defineProps<{
  label: string
}>()

const effectiveModel = computed<IncludeExclude>(() => {
  switch (model.value.i) {
    case 'i':
      return 'include'
    case 'e':
      return 'exclude'
    default:
      return undefined
  }
})

function handleChange(newVal: IncludeExclude) {
  switch (newVal) {
    case 'include':
      model.value = { i: 'i' }
      break
    case 'exclude':
      model.value = { i: 'e' }
      break
    default:
      model.value = { i: undefined }
      break
  }
}
</script>

<script lang="ts"></script>

<style scoped></style>
