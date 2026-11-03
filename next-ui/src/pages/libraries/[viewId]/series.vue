<template>
  <v-app-bar class="bg-background px-2">
    <template #prepend>
      <ChipCount :count="totalElements" />
    </template>

    <template #append>
      <div class="d-flex ga-2">
        <PosterSizeSlider />

        <PresentationSelector
          v-if="display.smAndUp.value"
          v-model="presentationMode"
          :modes="['grid', 'list']"
          toggle
        />

        <PageSizeSelector
          v-if="isBrowsingPaged"
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
    :presentation-mode="presentationModeEffective"
    :has-next-page="hasNextPage"
    :page-count="pageCount"
    @load-next-page="loadNextPage()"
  >
    <template #default="{ item, isSelected, preSelect, toggleSelect }">
      <SeriesCard
        v-if="presentationModeEffective === 'grid'"
        :stretch-poster="appStore.stretchPoster"
        :series="item"
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        :sort-active="sortActive"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />

      <SeriesCardWide
        v-if="presentationModeEffective === 'list'"
        :stretch-poster="appStore.stretchPoster"
        :series="item"
        :selected="isSelected"
        :pre-select="preSelect"
        :width="appStore.gridCardWidth"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />
    </template>
  </ItemBrowser>
</template>

<script lang="ts" setup>
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { seriesListQuery, seriesListQueryInfinite } from '@/colada/series'

import { PageRequest, type Sort } from '@/types/PageRequest'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useDisplay } from 'vuetify'
import { valuesToConditions, filtersToConditions } from '@/functions/filter'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortSeries } from '@/types/sort'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { usePresentationMode } from '@/composables/presentationMode'
import { storeToRefs } from 'pinia'
import { useFilters } from '@/composables/filter'
import ChipCount from '@/components/ChipCount.vue'
import { useSelectionContextualActions } from '@/composables/selection'
import type { SearchConditionSeries } from '@/generated/openapi'
import { BrowsingContextKey } from '@/functions/browsing-context'
import { useSort } from '@/composables/sort'
import { useLibraryViewStore } from '@/stores/library-view'

const route = useRoute('/libraries/[viewId]/series')
const libraryViewId = route.params.viewId
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)

provide(
  BrowsingContextKey,
  computed(() => ({ type: 'libraryView', id: libraryViewId, subType: 'series' })),
)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const libraryViewStore = useLibraryViewStore()
libraryViewStore.saveState(libraryViewId, 'series')

const viewName = computed(() => `${libraryViewId}_series`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

const {
  filters,
  clearAll: filtersClearAll,
  countAll: filtersCountAll,
} = useFilters(
  [
    'seriesStatus',
    'read',
    'genre',
    'tag',
    'publisher',
    'sharingLabel',
    'language',
    'year',
    'age',
    'complete',
    'unavailable',
    'oneshot',
    'contributors',
    'notableEntities',
  ],
  {
    viewName: viewName.value,
    persist: true,
  },
)

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortDefault: Sort[] = [{ key: 'metadata.titleSort', order: 'asc' }]
const sortOptions = sortSeries.map((it) => convertSortOptionDescriptor(it))
const {
  sortActive,
  isDefault: sortIsDefault,
  restoreDefault: sortRestore,
} = useSort({
  defaultSort: sortDefault,
  options: sortOptions,
  persist: true,
  viewName: viewName.value,
})

const conds = computed(() => ({
  allOf: [
    valuesToConditions(libraryIds.value, 'libraryId'),
    ...filtersToConditions(filters),
  ] as SearchConditionSeries[],
}))

// clear selection if filter or paging changes
watch([() => JSON.stringify(conds.value), () => appStore.browsingPaging], () =>
  selectionStore.clear(),
)

const apiQuery = computed(() => ({
  condition: conds.value,
}))

const { data: dataPaged } = useQuery(() => ({
  ...seriesListQuery({
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
  ...seriesListQueryInfinite({ search: { ...apiQuery.value }, sort: sortActive.value }),
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

useSelectionContextualActions(dataItems)

const filterDrawer = ref(false)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
