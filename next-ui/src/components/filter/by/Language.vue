<template>
  <FilterSearchList
    v-model="model"
    v-model:mode="modelMode"
    v-model:search="search"
    :items="allItems"
    :search-items="searchResults"
    :hide-search="allItems.length < 10"
    show-mode-selector
  >
  </FilterSearchList>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { languagesQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import * as v from 'valibot'
import { type AnyAll, filterKeys, filterMessages, SchemaString } from '@/types/filter'
import type { ItemType } from '@/components/filter/List.vue'
import { useIntl } from 'vue-intl'
import { languageDisplayNames } from '@/utils/i18n/locale-helper'

type SchString = v.InferOutput<typeof SchemaString>

const intl = useIntl()

const model = defineModel<SchString[]>({ default: [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })

const search = ref()

const filterContext = inject(filterKeys.context, {})

const apiQuery = {
  ...filterContext,
}

const { data: items } = useQuery(() => ({
  ...languagesQuery({
    pageRequest: PageRequest.Unpaged(),
    ...apiQuery,
  }),
}))
const searchResults = computed(() =>
  items.value?.content
    ?.map((it) => toItemType(it))
    .filter((it) => it.title.toLocaleLowerCase().includes(search.value?.toLocaleLowerCase() || '')),
)

const allItems = computed(() => {
  const itemTypes = (items.value?.content ?? []).map((it) => toItemType(it))
  return [
    {
      title: intl.formatMessage(filterMessages.any!),
      value: { a: 'any' },
      valueExclude: { a: 'none' },
    },
    ...itemTypes,
  ]
})

function toItemType(value: string): ItemType<SchString> {
  return {
    title: `${languageDisplayNames.of(value)} (${value})`,
    value: { i: 'i', v: value },
    valueExclude: { i: 'e', v: value },
  }
}
</script>

<script lang="ts"></script>

<style scoped></style>
