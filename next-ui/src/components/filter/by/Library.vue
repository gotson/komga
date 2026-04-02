<template>
  <FilterSearchList
    v-model="model"
    v-model:mode="modelMode"
    :items="items"
    hide-search
    show-mode-selector
  >
  </FilterSearchList>
</template>

<script setup lang="ts">
import * as v from 'valibot'
import { type AnyAll, SchemaString } from '@/types/filter'
import type { ItemType } from '@/components/filter/List.vue'
import { useLibraries } from '@/colada/libraries'
import type { components } from '@/generated/openapi/komga'

type SchString = v.InferOutput<typeof SchemaString>

const model = defineModel<SchString[]>({ default: [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })

const { ordered } = useLibraries()

const items = computed(() => ordered.value?.map((it) => toItemType(it)))

function toItemType(value: components['schemas']['LibraryDto']): ItemType<SchString> {
  return {
    title: value.name,
    value: { i: 'i', v: value.id },
    valueExclude: { i: 'e', v: value.id },
  }
}
</script>

<script lang="ts"></script>

<style scoped></style>
