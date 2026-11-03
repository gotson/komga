<template>
  <v-app-bar>
    <template #prepend>
      <NavigationBreadcrumbs />
    </template>

    <template #append>
      <CollectionNavigation :series-id="seriesId" />
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
      />
    </v-list>
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

    <template v-else-if="series">
      <SeriesView :series="series" />

      <v-divider class="mb-1 mx-2" />

      <div class="sticky-bar d-flex align-center pa-2">
        <ChipCount :count="totalElements" />

        <v-spacer />

        <!-- Append -->
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
        :presentation-mode="presentationModeEffective"
        :has-next-page="hasNextPage"
        :page-count="pageCount"
        @load-next-page="loadNextPage()"
      >
        <template #default="{ item, isSelected, preSelect, toggleSelect }">
          <BookCard
            stretch-poster
            :show-series="false"
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
  </v-container>
</template>

<script lang="ts" setup>
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import { filterKeys } from '@/types/filter'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePresentationMode } from '@/composables/presentationMode'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortBooks } from '@/types/sort'
import { useFilters } from '@/composables/filter'
import { filtersToConditions, valuesToConditions } from '@/functions/filter'
import { bookListQuery, bookListQueryInfinite } from '@/colada/books'
import FilterButton from '@/components/filter/FilterButton.vue'
import ChipCount from '@/components/ChipCount.vue'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useSelectionContextualActions } from '@/composables/selection'
import type { SearchConditionBook } from '@/generated/openapi'
import { seriesDetailQuery } from '@/colada/series'
import { BrowsingContextKey, pushBrowsingContext } from '@/functions/browsing-context'
import { useBrowsingContextFromRouteQuery } from '@/composables/browsingContext'
import { useSort } from '@/composables/sort'

const route = useRoute('/series/[id]')
const seriesId = computed(() => route.params.id)

const filterContext = computed(() => ({ series_id: [seriesId.value] }))
provide(filterKeys.context, filterContext)

const { context } = useBrowsingContextFromRouteQuery()
provide(
  BrowsingContextKey,
  computed(() => pushBrowsingContext(context.value, { type: 'series', id: seriesId.value })),
)

const {
  data: series,
  error,
  isPending,
} = useQuery(() => seriesDetailQuery({ seriesId: seriesId.value }))

// books list
const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `series_${seriesId.value}`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)
const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortDefault: Sort[] = [{ key: 'metadata.numberSort', order: 'asc' }]
const sortOptions = sortBooks
  .filter((it) => it.key !== 'series')
  .map((it) => convertSortOptionDescriptor(it))
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

const conds = computed(() => ({
  allOf: [
    valuesToConditions([seriesId.value], 'seriesId'),
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
