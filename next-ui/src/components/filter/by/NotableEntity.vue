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
import { notableEntitiesNamesQuery, notableEntitiesNamesQueryInfinite } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import * as v from 'valibot'
import { type AnyAll, filterKeys, filterMessages, SchemaNotableEntity } from '@/types/filter'
import type { ItemType } from '@/components/filter/List.vue'
import { refDebounced } from '@vueuse/core'
import { useIntl } from 'vue-intl'
import { NOTABLE_ENTITY_ANYKIND } from '@/types/referential/notableEntity'

type NotableEntity = v.InferOutput<typeof SchemaNotableEntity>

const intl = useIntl()

const model = defineModel<NotableEntity[]>({ default: () => [] })
const modelMode = defineModel<AnyAll>('mode', { default: 'anyOf' })

const search = ref()
const searchDebounced = refDebounced(search, 500)

const { kind } = defineProps<{
  kind?: string
}>()

const filterContext = inject(filterKeys.context, {})

const apiQuery = computed(() => ({
  ...toValue(filterContext),
  ...(kind === NOTABLE_ENTITY_ANYKIND ? {} : { kind: kind }),
}))

const { data: searchItems, isLoading: searchLoading } = useQuery(() => ({
  ...notableEntitiesNamesQuery({
    pageRequest: PageRequest.Unpaged(),
    search: searchDebounced.value,
    ...apiQuery.value,
  }),
  enabled: !!searchDebounced.value,
}))
const searchResults = computed(() => searchItems.value?.content?.map((it) => toItemType(it)))

const { data: infiniteData, loadNextPage } = useInfiniteQuery(() =>
  notableEntitiesNamesQueryInfinite(apiQuery.value),
)
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

function toItemType(notableEntityName: string): ItemType<NotableEntity> {
  return {
    title: notableEntityName,
    value: { i: 'i', v: notableEntityName },
    valueExclude: { i: 'e', v: notableEntityName },
  }
}
</script>

<script lang="ts"></script>

<style scoped></style>
