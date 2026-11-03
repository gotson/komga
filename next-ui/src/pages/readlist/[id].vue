<template>
  <v-app-bar>
    <template #prepend>
      <NavigationBreadcrumbs />
    </template>
  </v-app-bar>

  <TempDrawer v-model="filterDrawer">
    <FilterPanel
      v-model="filters"
      :count="filtersCountAll"
      tags-include="BOOK"
      @clear="filtersClearAll()"
    />
  </TempDrawer>

  <v-container
    fluid
    class="pa-0 pa-sm-4"
  >
    <div
      v-if="isPending"
      class="pa-4 pa-sm-0"
    >
      <v-row>
        <v-col cols="3">
          <v-skeleton-loader type="image" />
        </v-col>
        <v-col>
          <v-skeleton-loader type="article" />
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-skeleton-loader type="table-heading@5" />
        </v-col>
      </v-row>
    </div>

    <EmptyStateNetworkError v-else-if="error" />

    <template v-else-if="readList">
      <ReadlistView :read-list="readList" />

      <v-divider class="mb-1 mx-2" />

      <div class="sticky-bar d-flex align-center pa-2">
        <ChipCount :count="totalElements" />

        <v-spacer />

        <!-- Append -->
        <div class="d-flex ga-2">
          <PosterSizeSlider />

          <PageSizeSelector
            v-if="isBrowsingPaged"
            v-model="appStore.browsingPageSize"
            allow-unpaged
          />

          <PagingSelector v-model="appStore.browsingPaging" />

          <FilterButton
            :count="filtersCountAll"
            @click="filterDrawer = true"
          />
        </div>
      </div>

      <EmptyStateFilterNoResults
        v-if="totalElements === 0 && filtersCountAll > 0"
        @reset="filtersClearAll()"
      />

      <ItemBrowser
        v-else
        v-model:page1="page1"
        :paging="appStore.browsingPaging"
        :items="dataItems"
        presentation-mode="grid"
        :has-next-page="hasNextPage"
        :page-count="pageCount"
        @load-next-page="loadNextPage()"
      >
        <template #default="{ item, isSelected, preSelect, toggleSelect }">
          <BookCard
            :stretch-poster="appStore.stretchPoster"
            show-series
            :book="item"
            :selected="isSelected"
            :pre-select="preSelect"
            :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
            @selection="(_val, event) => toggleSelect(event as MouseEvent)"
          />
        </template>
      </ItemBrowser>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { usePagination } from '@/composables/pagination'
import { readListDetailQuery } from '@/colada/readlists'
import { useSelectionStore } from '@/stores/selection'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { filtersToConditions, valuesToConditions } from '@/functions/filter'
import { bookListQuery, bookListQueryInfinite } from '@/colada/books'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import ChipCount from '@/components/ChipCount.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { useFilters } from '@/composables/filter'
import { filterKeys } from '@/types/filter'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import { useSelectionContextualActions } from '@/composables/selection'
import type { SearchConditionBook } from '@/generated/openapi'
import { BrowsingContextKey, pushBrowsingContext } from '@/functions/browsing-context'
import { useBrowsingContextFromRouteQuery } from '@/composables/browsingContext'

const route = useRoute('/readlist/[id]')
const router = useRouter()
const readListId = computed(() => route.params.id)

const filterContext = computed(() => ({ readlist_id: [readListId.value] }))
provide(filterKeys.context, filterContext)

const { context } = useBrowsingContextFromRouteQuery()
provide(
  BrowsingContextKey,
  computed(() => pushBrowsingContext(context.value, { type: 'readList', id: readListId.value })),
)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const {
  data: readList,
  error,
  isPending,
} = useQuery(() => ({
  ...readListDetailQuery({
    readListId: readListId.value,
  }),
}))
// redirect to home if the entity is deleted
watch(error, () => router.push('/'))

const viewName = computed(() => `readlist${readListId.value}`)

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

const sortActive = computed<Sort[]>(() => {
  if (readList.value?.ordered) return [{ key: 'readList.number', order: 'asc' }]
  else return [{ key: 'metadata.releaseDate', order: 'asc' }]
})

const {
  filters,
  clearAll: filtersClearAll,
  countAll: filtersCountAll,
} = useFilters(
  [
    'library',
    'mediaStatus',
    'profile',
    'read',
    'tag',
    'unavailable',
    'oneshot',
    'poster',
    'contributors',
    'notableEntities',
  ],
  { context: filterContext, viewName: viewName.value, persist: true },
)

const conds = computed(() => ({
  allOf: [
    valuesToConditions([readListId.value], 'readListId'),
    ...filtersToConditions(filters),
  ] as SearchConditionBook[],
}))

// clear selection if filter or paging changes
watch([() => JSON.stringify(conds.value), () => appStore.browsingPaging], () =>
  selectionStore.clear(),
)

const apiQuery = computed(() => ({
  condition: conds.value,
}))

const { data: dataPaged } = useQuery(() => ({
  ...bookListQuery({
    search: { ...apiQuery.value },
    pageRequest: PageRequest.FromPageSize(appStore.browsingPageSize, page0.value, sortActive.value),
  }),
  enabled: isBrowsingPaged.value,
}))

watch(dataPaged, (newDataPaged) => {
  if (newDataPaged) pageCount.value = newDataPaged.totalPages ?? 0
})

const {
  data: dataInfinite,
  loadNextPage,
  hasNextPage,
} = useInfiniteQuery(() => ({
  ...bookListQueryInfinite({ search: { ...apiQuery.value }, sort: sortActive.value }),
  enabled: isBrowsingScroll.value,
}))
const dataInfiniteFlat = computed(() =>
  dataInfinite.value?.pages.flatMap((it) => it?.content ?? []),
)

const dataItems = computed(() =>
  isBrowsingPaged.value ? dataPaged.value?.content : dataInfiniteFlat.value,
)
const totalElements = computed(() =>
  isBrowsingPaged.value
    ? dataPaged.value?.totalElements
    : dataInfinite.value?.pages?.[0]?.totalElements,
)

const filterDrawer = ref(false)

useSelectionContextualActions(dataItems)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
