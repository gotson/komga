<template>
  <v-app-bar>
    <LibraryHeader
      class="ms-4"
      :library-id="series?.libraryId"
      link
    />

    <ChipCount
      class="ms-2"
      :count="totalElements"
    />

    <v-spacer />

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

    <PagingSelector
      v-model="appStore.browsingPaging"
      class="px-2"
    />

    <FilterButton
      :count="filterCount"
      @click="filterDrawer = true"
    />
  </v-app-bar>

  <TempDrawer v-model="filterDrawer">
    <v-list>
      <v-list-subheader>
        <div class="d-flex ga-2 align-center mb-1">
          <span>{{ $formatMessage(commonMessages.filterPanelHeader) }}</span>
          <FilterChipCount
            :count="filterCount"
            @clear="clearFilters()"
          />
        </div>
      </v-list-subheader>

      <FilterByReadStatus
        v-model="filters.read.filter.v"
        class="py-0"
      />

      <v-list class="py-0">
        <FilterByOneShot v-model="filters.oneshot.filter" />
        <FilterByUnavailable v-model="filters.unavailable.filter" />
      </v-list>

      <v-expansion-panels
        v-model="filterExpansionPanels"
        variant="accordion"
        class="no-padding"
        flat
        tile
      >
        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelMediaProfile)"
          :count="filters.profile.count"
          @clear="filters.profile.clear()"
        >
          <FilterByMediaProfile v-model="filters.profile.filter.v" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelMediaStatus)"
          :count="filters.mediaStatus.count"
          @clear="filters.mediaStatus.clear()"
        >
          <FilterByMediaStatus v-model="filters.mediaStatus.filter.v" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelTag)"
          :count="filters.tag.count"
          @clear="filters.tag.clear()"
        >
          <FilterByTag
            v-model="filters.tag.filter.v"
            v-model:mode="filters.tag.filter.m"
            include="BOOK"
          />
        </FilterExpansionPanel>
      </v-expansion-panels>

      <v-divider
        ><span class="text-body-medium text-medium-emphasis">{{
          $formatMessage(commonMessages.filterPanelContributors)
        }}</span></v-divider
      >

      <v-expansion-panels
        v-model="filterExpansionPanels"
        variant="accordion"
        class="no-padding"
        flat
        tile
      >
        <FilterExpansionPanel
          v-for="(filterAuthor, role) in filterContributors"
          :key="role"
          :title="
            contributorsRolesMessages?.[role]
              ? $formatMessage(contributorsRolesMessages?.[role])
              : role
          "
          :count="countFilter(filterAuthor)"
          @clear="filterContributorsClear(role)"
        >
          <FilterByAuthor
            v-model="filterAuthor.v"
            v-model:mode="filterAuthor.m"
            :role="role"
          />
        </FilterExpansionPanel>
      </v-expansion-panels>

      <v-divider />

      <v-list-subheader>{{ $formatMessage(commonMessages.filterPanelSort) }}</v-list-subheader>

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
    <div v-if="isPending">
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

      <v-divider />

      <EmptyStateFilterNoResults
        v-if="totalElements === 0 && filterCount > 0"
        @reset="clearFilters()"
      />

      <ItemBrowser
        v-else
        v-model:page1="page1"
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
            @selection="(_val, event) => toggleSelect(event as MouseEvent)"
          />
        </template>
      </ItemBrowser>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { seriesDetailQuery } from '@/colada/series'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import { useSeriesBooks } from '@/composables/series/useSeriesBooks'
import { filterKeys } from '@/types/filter'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { PageRequest } from '@/types/PageRequest'
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePresentationMode } from '@/composables/presentationMode'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortBooks } from '@/types/sort'
import { useFilterContributors, useFilters } from '@/composables/filter'
import {
  countFilter,
  schemaFilterAuthorsToConditions,
  schemaFilterIncludeExcludeToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterStringToConditions,
  valuesToConditions,
} from '@/functions/filter'

import { bookListQuery, bookListQueryInfinite } from '@/colada/books'
import FilterButton from '@/components/filter/FilterButton.vue'
import ChipCount from '@/components/ChipCount.vue'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import { contributorsRolesMessages } from '@/types/referential'
import { useSelectionContextualActions } from '@/composables/selection'
import { logger } from '@/services/logtape'
import type { SearchConditionBook } from '@/generated/openapi'

// oneshot redirection
definePage({
  beforeEnter: async (to) => {
    logger.debug('navigation guard: check if series is oneshot')
    const params = to.params as { id: string }

    const { data, refresh } = useQuery(seriesDetailQuery({ seriesId: params.id }))
    await refresh()

    if (data.value?.oneshot) {
      logger.debug('navigation guard: series is oneshot, fetch book for redirection')
      const { getFirstBookInSeries } = useSeriesBooks(params.id)
      const book = await getFirstBookInSeries(false)
      if (book) {
        logger.debug('navigation guard: book found, redirect to book page')
        return {
          name: '/book/[id]',
          params: { id: book.id },
          query: to.query,
          replace: true,
        }
      }
    }
  },
})

const route = useRoute('/series/[id]')
const seriesId = computed(() => route.params.id)

provide(
  filterKeys.context,
  computed(() => ({ series_id: [seriesId.value] })),
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

function clearFilters() {
  filterContributorsClearAll()
  filtersClearAll()
}

const filterCount = computed(() => filterContributorsCount.value + filtersCountAll.value)

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortActive = appStore.getSortActive(viewName.value, [
  { key: 'metadata.numberSort', order: 'asc' },
])
const sortOptions = sortBooks
  .filter((it) => it.key !== 'series')
  .map((it) => convertSortOptionDescriptor(it))

const {
  filter: filterContributors,
  countAll: filterContributorsCount,
  clearAll: filterContributorsClearAll,
  clear: filterContributorsClear,
} = useFilterContributors()
const {
  filters,
  clearAll: filtersClearAll,
  countAll: filtersCountAll,
} = useFilters(['mediaStatus', 'profile', 'read', 'tag', 'unavailable', 'oneshot'])

const conds = computed(() => ({
  allOf: [
    valuesToConditions([seriesId.value], 'seriesId'),
    schemaFilterIncludeExcludeToConditions(filters.value.unavailable.filter, 'deleted'),
    schemaFilterIncludeExcludeToConditions(filters.value.oneshot.filter, 'oneShot'),
    schemaFilterReadStatusToConditions(filters.value.read.filter),
    valuesToConditions(filters.value.mediaStatus.filter.v, 'mediaStatus'),
    valuesToConditions(filters.value.profile.filter.v, 'mediaProfile'),
    schemaFilterStringToConditions(filters.value.tag.filter, 'tag', true),
    ...Object.entries(filterContributors.value).map(([role, filter]) =>
      schemaFilterAuthorsToConditions(filter, role),
    ),
  ].filter(Boolean) as SearchConditionBook[],
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

// shared model for all the expansion-panels, so only 1 is opened at the same time
const filterExpansionPanels = ref()
</script>

<style lang="scss">
.no-padding .v-expansion-panel-text__wrapper {
  padding: 0 4px;
}
</style>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
