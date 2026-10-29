<template>
  <ComboboxInfiniteBulk
    v-model="selected"
    v-model:search="search"
    :items="displayedItems"
    :has-more-data="hasNextPage"
    :label="
      contributorsRolesMessages?.[role] ? $formatMessage(contributorsRolesMessages?.[role]) : role
    "
    :initial-counts="initialCounts"
    :total-count="totalCount"
    @load-more="loadNextPage()"
  >
  </ComboboxInfiniteBulk>
</template>

<script setup lang="ts">
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { authorsNamesQuery, authorsNamesQueryInfinite } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { type FilterContext } from '@/types/filter'
import { refDebounced } from '@vueuse/core'
import { contributorsRolesMessages } from '@/types/referential/contributor'
import type { BulkInputProps, BulkItemMultiField } from '@/types/bulk'

const selected = defineModel<BulkItemMultiField<string>[]>({ default: () => [] })

const search = ref()
const searchDebounced = refDebounced(search, 500)
const isSearching = computed(() => searchDebounced.value?.trim()?.length > 0)

const { role, filterContext } = defineProps<
  {
    role: string
    filterContext?: FilterContext
  } & BulkInputProps<string>
>()

const apiQuery = computed(() => ({
  ...filterContext,
  role: role,
}))

const { data: searchItems } = useQuery(() => ({
  ...authorsNamesQuery({
    pageRequest: PageRequest.Unpaged(),
    search: searchDebounced.value,
    ...apiQuery.value,
  }),
  enabled: isSearching.value,
}))
const searchResults = computed(() => searchItems.value?.content ?? [])

const {
  data: infiniteData,
  loadNextPage,
  hasNextPage,
} = useInfiniteQuery(() => authorsNamesQueryInfinite(apiQuery.value))
const infiniteItems = computed(() => {
  return infiniteData.value?.pages.flatMap((it) => it?.content ?? []) ?? []
})

const displayedItems = computed(() =>
  isSearching.value ? searchResults.value : infiniteItems.value,
)
</script>
