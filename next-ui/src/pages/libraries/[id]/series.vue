<template>
  <v-app-bar>
    <ChipCount
      class="ms-4"
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
        <FilterByComplete v-model="filters.complete.filter" />
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
          :title="$formatMessage(commonMessages.filterPanelSeriesStatus)"
          :count="filters.seriesStatus.count"
          @clear="filters.seriesStatus.clear()"
        >
          <FilterBySeriesStatus v-model="filters.seriesStatus.filter.v" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelGenre)"
          :count="filters.genre.count"
          @clear="filters.genre.clear()"
        >
          <FilterByGenre
            v-model="filters.genre.filter.v"
            v-model:mode="filters.genre.filter.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelTag)"
          :count="filters.tag.count"
          @clear="filters.tag.clear()"
        >
          <FilterByTag
            v-model="filters.tag.filter.v"
            v-model:mode="filters.tag.filter.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelPublisher)"
          :count="filters.publisher.count"
          @clear="filters.publisher.clear()"
        >
          <FilterByPublisher
            v-model="filters.publisher.filter.v"
            v-model:mode="filters.publisher.filter.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelReleaseYear)"
          :count="filters.year.count"
          @clear="filters.year.clear()"
        >
          <FilterByReleaseYear v-model="filters.year.filter" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelAgeRating)"
          :count="filters.age.count"
          @clear="filters.age.clear()"
        >
          <FilterByAgeRating v-model="filters.age.filter" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelLanguage)"
          :count="filters.language.count"
          @clear="filters.language.clear()"
        >
          <FilterByLanguage
            v-model="filters.language.filter.v"
            v-model:mode="filters.language.filter.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelSharingLabel)"
          :count="filters.sharingLabel.count"
          @clear="filters.sharingLabel.clear()"
        >
          <FilterBySharingLabel
            v-model="filters.sharingLabel.filter.v"
            v-model:mode="filters.sharingLabel.filter.m"
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
      <SeriesCard
        v-if="presentationModeEffective === 'grid'"
        stretch-poster
        :series="item"
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />

      <SeriesCardWide
        v-if="presentationModeEffective === 'list'"
        stretch-poster
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

import { PageRequest } from '@/types/PageRequest'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useDisplay } from 'vuetify'
import {
  schemaFilterAuthorsToConditions,
  schemaFilterStringToConditions,
  schemaFilterReleaseYearToConditions,
  schemaFilterAgeRatingToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterIncludeExcludeToConditions,
  valuesToConditions,
  countFilter,
} from '@/functions/filter'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortSeries } from '@/types/sort'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { usePresentationMode } from '@/composables/presentationMode'
import { storeToRefs } from 'pinia'
import { useFilterContributors, useFilters } from '@/composables/filter'
import ChipCount from '@/components/ChipCount.vue'
import { contributorsRolesMessages } from '@/types/referential'
import { useSelectionContextualActions } from '@/composables/selection'
import type { SearchConditionSeries } from '@/generated/openapi'

const route = useRoute('/libraries/[id]/series')
const libraryViewId = route.params.id
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `${libraryViewId}_series`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

function clearFilters() {
  filterContributorsClearAll()
  filtersClearAll()
}

const filterCount = computed(() => filterContributorsCount.value + filtersCountAll.value)

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
} = useFilters([
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
])

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortActive = appStore.getSortActive(viewName.value, [
  { key: 'metadata.titleSort', order: 'asc' },
])
const sortOptions = sortSeries.map((it) => convertSortOptionDescriptor(it))

const conds = computed(() => ({
  allOf: [
    valuesToConditions(libraryIds.value, 'libraryId'),
    schemaFilterIncludeExcludeToConditions(filters.value.complete.filter, 'complete'),
    schemaFilterIncludeExcludeToConditions(filters.value.unavailable.filter, 'deleted'),
    schemaFilterIncludeExcludeToConditions(filters.value.oneshot.filter, 'oneShot'),
    valuesToConditions(filters.value.seriesStatus.filter.v, 'seriesStatus'),
    schemaFilterReadStatusToConditions(filters.value.read.filter),
    schemaFilterStringToConditions(filters.value.genre.filter, 'genre', true),
    schemaFilterStringToConditions(filters.value.tag.filter, 'tag', true),
    schemaFilterStringToConditions(filters.value.publisher.filter, 'publisher', false),
    schemaFilterStringToConditions(filters.value.sharingLabel.filter, 'sharingLabel', true),
    schemaFilterStringToConditions(filters.value.language.filter, 'language', false),
    schemaFilterReleaseYearToConditions(filters.value.year.filter),
    schemaFilterAgeRatingToConditions(filters.value.age.filter),
    ...Object.entries(filterContributors.value).map(([role, filter]) =>
      schemaFilterAuthorsToConditions(filter, role),
    ),
  ].filter(Boolean) as SearchConditionSeries[],
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
