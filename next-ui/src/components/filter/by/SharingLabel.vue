<template>
  <FilterSearchList
    v-model="model"
    v-model:mode="modelMode"
    v-model:search="search"
    :items="infiniteItems"
    :search-items="searchResults"
    :search-loading="searchLoading"
    :hide-search="hideSearch"
    show-mode-selector
    @load-more="loadNextPage()"
  >
  </FilterSearchList>
</template>

<script setup lang="ts">
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { sharingLabelsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { komgaClient } from '@/api/komga-client'
import * as v from 'valibot'
import { type AnyAll, filterKeys, filterMessages, SchemaString } from '@/types/filter'
import type { ItemType } from '@/components/filter/List.vue'
import { refDebounced } from '@vueuse/core'
import { useIntl } from 'vue-intl'

type SchString = v.InferOutput<typeof SchemaString>

const intl = useIntl()

const model = defineModel<SchString[]>({ default: [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })

const search = ref()
const searchDebounced = refDebounced(search, 500)

const filterContext = inject(filterKeys.context, {})

const apiQuery = {
  ...filterContext,
}

const { data: searchItems, isLoading: searchLoading } = useQuery(() => ({
  ...sharingLabelsQuery({
    pageRequest: PageRequest.Unpaged(),
    search: searchDebounced.value,
    ...apiQuery,
  }),
  enabled: !!searchDebounced.value,
}))
const searchResults = computed(() => searchItems.value?.content?.map((it) => toItemType(it)))

const { data: infiniteData, loadNextPage } = useInfiniteQuery({
  key: () => ['infinite_sharing-labels', apiQuery],
  initialPageParam: new PageRequest(0, 50),
  query: ({ pageParam }) =>
    komgaClient
      .GET('/api/v2/sharing-labels', {
        params: {
          query: {
            ...apiQuery,
            ...pageParam,
          },
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
  getNextPageParam: (lastPage) =>
    !lastPage?.last ? new PageRequest((lastPage?.number ?? 0) + 1, lastPage?.size) : null,
})
const infiniteItems = computed(() => {
  const itemTypes = (infiniteData.value?.pages.flatMap((it) => it?.content ?? []) ?? []).map((it) =>
    toItemType(it),
  )
  return [
    {
      title: intl.formatMessage(filterMessages.any!),
      value: { a: 'any' },
      valueExclude: { a: 'none' },
    },
    ...itemTypes,
  ]
})

const hideSearch = computed(() => (infiniteData.value?.pages?.[0]?.totalElements || 0) < 10)

function toItemType(value: string): ItemType<SchString> {
  return {
    title: value,
    value: { i: 'i', v: value },
    valueExclude: { i: 'e', v: value },
  }
}
</script>

<script lang="ts"></script>

<style scoped></style>
