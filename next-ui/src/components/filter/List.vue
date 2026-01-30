<template>
  <v-checkbox
    :model-value="anyAll === 'allOf'"
    label="All of"
    @update:model-value="(v) => (anyAll = v ? 'allOf' : 'anyOf')"
  />
  <v-list>
    <FilterTriState
      v-for="(item, i) in items"
      :key="i"
      v-model="internalModel[i]"
      :label="item.title"
      :tri-state="!!item.valueExclude"
      :color="color"
    />
  </v-list>
</template>

<script setup lang="ts">
import { syncRef } from '@vueuse/core'
import type { AnyAll } from '@/types/filter'

const model = defineModel<unknown[]>({ default: [] })
const anyAll = defineModel<AnyAll>('mode', { default: 'anyOf' })

//TODO: handle mode better
//TODO: add clear all?

const { items = [], color } = defineProps<{
  items?: {
    title: string
    value: unknown
    valueExclude?: unknown
  }[]
  color?: string
}>()

function initializeInternalModel() {
  const c: Record<number, string | undefined> = {}
  items.forEach((_it, i) => (c[i] = undefined))
  return c
}

const internalModel = ref<Record<number, string | undefined>>(initializeInternalModel())

syncRef(model, internalModel, {
  direction: 'both',
  deep: true,
  transform: {
    ltr: (left) =>
      left.reduce((acc, item) => {
        const indexInclude = items.findIndex(
          (it) => JSON.stringify(it.value) === JSON.stringify(item),
        )
        if (indexInclude >= 0)
          return {
            ...(acc as Record<number, string | undefined>),
            [indexInclude]: 'include',
          }

        const indexExclude = items.findIndex(
          (it) => JSON.stringify(it.valueExclude) === JSON.stringify(item),
        )
        if (indexExclude >= 0)
          return {
            ...(acc as Record<number, string | undefined>),
            [indexExclude]: 'exclude',
          }

        return acc
      }, {}) as Record<number, string | undefined>,
    rtl: (right) =>
      items
        .map((it, i) =>
          right[i] === 'include' ? it.value : right[i] === 'exclude' ? it.valueExclude : undefined,
        )
        .filter((it) => it != undefined),
  },
})
</script>

<script lang="ts"></script>

<style scoped></style>
