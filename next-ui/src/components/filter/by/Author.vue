<template>
  <FilterSearchList
    v-model="model"
    v-model:mode="modelMode"
    v-model:search="search"
    :items="infiniteItems"
    :search-items="searchResults"
    :search-loading="searchLoading"
    show-mode-selector
    @load-more="loadNextPage()"
  >
  </FilterSearchList>
</template>

<script setup lang="ts">
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { authorsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { komgaClient } from '@/api/komga-client'
import * as v from 'valibot'
import { type AnyAll, filterKeys, filterMessages, SchemaAuthor } from '@/types/filter'
import type { ItemType } from '@/components/filter/List.vue'
import { refDebounced } from '@vueuse/core'
import { useIntl } from 'vue-intl'

type Author = v.InferOutput<typeof SchemaAuthor>

const intl = useIntl()

const model = defineModel<Author[]>({ default: [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })

const search = ref()
const searchDebounced = refDebounced(search, 500)

const { role } = defineProps<{
  role?: string
}>()

const filterContext = inject(filterKeys.context, {})

const apiQuery = {
  ...filterContext,
  role: role,
}

const { data: searchItems, isLoading: searchLoading } = useQuery(authorsQuery, () => ({
  pageRequest: PageRequest.Unpaged(),
  search: searchDebounced.value,
  pause: !searchDebounced.value,
  placeholder: false,
  ...apiQuery,
}))
const searchResults = computed(() => searchItems.value?.content?.map((it) => toItemType(it.name)))

const { data: infiniteData, loadNextPage } = useInfiniteQuery({
  key: () => ['infinite_authors', apiQuery],
  initialPageParam: new PageRequest(0, 50),
  query: ({ pageParam }) =>
    komgaClient
      .GET('/api/v2/authors', {
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
  const itemTypes = Array.from(
    new Set(
      infiniteData.value?.pages.flatMap((it) => it?.content?.map((it) => it.name) ?? []) ?? [],
    ),
  ).map((it) => toItemType(it))
  return [
    {
      title: intl.formatMessage(filterMessages.any!),
      value: { a: 'any' },
      valueExclude: { a: 'none' },
    },
    ...itemTypes,
  ]
})

function toItemType(authorName: string): ItemType<Author> {
  return {
    title: authorName,
    value: { i: 'i', v: authorName },
    valueExclude: { i: 'e', v: authorName },
  }
}
</script>

<script lang="ts"></script>

<style scoped></style>
