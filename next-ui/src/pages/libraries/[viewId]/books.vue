<template>
  <v-app-bar class="bg-background px-2">
    <template #prepend>
      <ChipCount :count="totalElements" />
    </template>

    <template #append>
      <div class="d-flex ga-2">
        <PosterSizeSlider />

        <PageSizeSelector
          v-if="appStore.isBrowsingPaged"
          v-model="appStore.browsingPageSize"
          allow-unpaged
        />

        <PagingSelector v-model="appStore.browsingPaging" />

        <FilterButton
          :count="filtersCountAll"
          :dot="!sortIsDefault"
          @click="filterDrawer = true"
        />
      </div>
    </template>
  </v-app-bar>

  <TempDrawer v-model="filterDrawer">
    <FilterPanel
      v-model="filters"
      :count="filtersCountAll"
      tags-include="BOOK"
      @clear="filtersClearAll()"
    />

    <v-divider />

    <v-list>
      <v-list-subheader>
        <span>{{ $formatMessage(commonMessages.filterPanelSort) }}</span>
        <SortRestore
          v-if="!sortIsDefault"
          class="position-absolute right-0 me-6"
          @restore="sortRestore()"
        />
      </v-list-subheader>

      <SortList
        v-model="sortActive"
        :items="sortOptions"
        mandatory
        multi-sort
      />
    </v-list>
  </TempDrawer>

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
        show-series
        :stretch-poster="appStore.stretchPoster"
        :book="item"
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        :sort-active="sortActive"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />
    </template>
  </ItemBrowser>
</template>

<script lang="ts" setup>
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { useDisplay } from 'vuetify'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortBooks } from '@/types/sort'
import { filtersToConditions, valuesToConditions } from '@/functions/filter'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { bookListQuery, bookListQueryInfinite } from '@/colada/books'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useFilters } from '@/composables/filter'
import { useSelectionContextualActions } from '@/composables/selection'
import type { SearchConditionBook } from '@/generated/openapi'
import { BrowsingContextKey } from '@/functions/browsing-context'
import { useSort } from '@/composables/sort'
import { useLibraryViewStore } from '@/stores/library-view'

const route = useRoute('/libraries/[viewId]/books')
const libraryViewId = route.params.viewId
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)

provide(
  BrowsingContextKey,
  computed(() => ({ type: 'libraryView', id: libraryViewId, subType: 'books' })),
)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const libraryViewStore = useLibraryViewStore()
libraryViewStore.saveState(libraryViewId, 'books')

const viewName = computed(() => `${libraryViewId}_books`)

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

const {
  filters,
  clearAll: filtersClearAll,
  countAll: filtersCountAll,
} = useFilters(
  [
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
  {
    viewName: viewName.value,
    persist: true,
  },
)

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortDefault: Sort[] = [
  { key: 'series', order: 'asc' },
  { key: 'metadata.numberSort', order: 'asc' },
]
const sortOptions = sortBooks.map((it) => convertSortOptionDescriptor(it))
const {
  sortActive,
  isDefault: sortIsDefault,
  restoreDefault: sortRestore,
} = useSort({
  defaultSort: sortDefault,
  options: sortOptions,
  multiSort: true,
  persist: true,
  viewName: viewName.value,
})

const conds = computed(() => ({
  allOf: [
    valuesToConditions(libraryIds.value, 'libraryId'),
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
