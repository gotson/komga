<template>
  <ComboboxInfinite
    v-model="selected"
    v-model:search="search"
    :items="displayedItems"
    :has-more-data="hasNextPage"
    :label="
      notableEntitiesKindsMessages?.[kind]
        ? $formatMessage(notableEntitiesKindsMessages[kind])
        : kind
    "
    @load-more="loadNextPage()"
  >
  </ComboboxInfinite>
</template>

<script setup lang="ts">
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { notableEntitiesNamesQuery, notableEntitiesNamesQueryInfinite } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { type FilterContext } from '@/types/filter'
import { refDebounced } from '@vueuse/core'
import { notableEntitiesKindsMessages } from '@/types/referential/notableEntity'

const selected = defineModel<string[]>({ default: () => [] })

const search = ref()
const searchDebounced = refDebounced(search, 500)
const isSearching = computed(() => searchDebounced.value?.trim()?.length > 0)

const { kind, filterContext } = defineProps<{
  kind: string
  filterContext?: FilterContext
}>()

const apiQuery = computed(() => ({
  ...filterContext,
  kind: kind,
}))

const { data: searchItems } = useQuery(() => ({
  ...notableEntitiesNamesQuery({
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
} = useInfiniteQuery(() => notableEntitiesNamesQueryInfinite(apiQuery.value))
const infiniteItems = computed(() => {
  return infiniteData.value?.pages.flatMap((it) => it?.content ?? []) ?? []
})

const displayedItems = computed(() =>
  isSearching.value ? searchResults.value : infiniteItems.value,
)
</script>

<script lang="ts"></script>

<style scoped></style>
