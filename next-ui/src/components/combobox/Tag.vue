<template>
  <ComboboxInfinite
    v-model="selected"
    v-model:search="search"
    :items="displayedItems"
    :has-more-data="hasNextPage"
    :label="
      $formatMessage({
        description: 'Combobox tags: label',
        defaultMessage: 'Tags',
        id: '8KsYYJ',
      })
    "
    @load-more="loadNextPage()"
  >
    <template #prepend>
      <slot name="prepend" />
    </template>
  </ComboboxInfinite>
</template>

<script setup lang="ts">
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { tagsQuery, tagsQueryInfinite } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { type FilterContext } from '@/types/filter'
import { refDebounced } from '@vueuse/core'

const selected = defineModel<string[]>({ default: () => [] })

const search = ref()
const searchDebounced = refDebounced(search, 500)
const isSearching = computed(() => searchDebounced.value?.trim()?.length > 0)

const { include = 'BOTH', filterContext } = defineProps<{
  include?: 'SERIES' | 'BOOK' | 'BOTH'
  filterContext?: FilterContext
}>()

const apiQuery = computed(() => ({
  ...filterContext,
  include: include,
}))

const { data: searchItems } = useQuery(() => ({
  ...tagsQuery({
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
} = useInfiniteQuery(() => tagsQueryInfinite(apiQuery.value))
const infiniteItems = computed(() => {
  return infiniteData.value?.pages.flatMap((it) => it?.content ?? []) ?? []
})

const displayedItems = computed(() =>
  isSearching.value ? searchResults.value : infiniteItems.value,
)
</script>

<script lang="ts"></script>

<style scoped></style>
