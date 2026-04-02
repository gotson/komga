<template>
  <v-app-bar>
    <ChipCount :count="totalElements" />

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
      :sizes="[1, 10, 20]"
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
        v-model="filterReadStatus.v"
        class="py-0"
      />

      <v-list class="py-0">
        <FilterByOneShot v-model="filterOneShot" />
        <FilterByComplete v-model="filterComplete" />
        <FilterByUnavailable v-model="filterUnavailable" />
      </v-list>

      <v-expansion-panels
        v-model="filterExpansionPanels"
        variant="accordion"
        class="no-padding"
        flat
        tile
      >
        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelLibrary)"
          :count="filterLibrary.v.length"
          @clear="clearFilter(filterLibrary)"
        >
          <FilterByLibrary v-model="filterLibrary.v" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelSeriesStatus)"
          :count="filterSeriesStatus.v.length"
          @clear="clearFilter(filterSeriesStatus)"
        >
          <FilterBySeriesStatus v-model="filterSeriesStatus.v" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelGenre)"
          :count="filterGenre.v.length"
          @clear="clearFilter(filterGenre)"
        >
          <FilterByGenre
            v-model="filterGenre.v"
            v-model:mode="filterGenre.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelTag)"
          :count="filterTag.v.length"
          @clear="clearFilter(filterTag)"
        >
          <FilterByTag
            v-model="filterTag.v"
            v-model:mode="filterTag.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelPublisher)"
          :count="filterPublisher.v.length"
          @clear="clearFilter(filterPublisher)"
        >
          <FilterByPublisher
            v-model="filterPublisher.v"
            v-model:mode="filterPublisher.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelReleaseYear)"
          :count="!!filterReleaseYear.is ? 1 : !!filterReleaseYear.min ? 1 : 0"
          @clear="clearFilter(filterReleaseYear)"
        >
          <FilterByReleaseYear v-model="filterReleaseYear" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelAgeRating)"
          :count="!!filterAgeRating.is ? 1 : !!filterAgeRating.min ? 1 : 0"
          @clear="clearFilter(filterAgeRating)"
        >
          <FilterByAgeRating v-model="filterAgeRating" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelLanguage)"
          :count="filterLanguage.v.length"
          @clear="clearFilter(filterLanguage)"
        >
          <FilterByLanguage
            v-model="filterLanguage.v"
            v-model:mode="filterLanguage.m"
          />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelSharingLabel)"
          :count="filterSharingLabel.v.length"
          @clear="clearFilter(filterSharingLabel)"
        >
          <FilterBySharingLabel
            v-model="filterSharingLabel.v"
            v-model:mode="filterSharingLabel.m"
          />
        </FilterExpansionPanel>
      </v-expansion-panels>

      <v-divider
        ><span class="text-body-medium text-medium-emphasis">{{
          $formatMessage(commonMessages.filterPanelCreators)
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
          v-for="(filterAuthor, role) in filterAuthors"
          :key="role"
          :title="filterAuthor.text"
          :count="filterAuthor.filter.v.length"
          @clear="clearFilter(filterAuthor.filter)"
        >
          <FilterByAuthor
            v-model="filterAuthor.filter.v"
            v-model:mode="filterAuthor.filter.m"
            :role="filterAuthor.role"
          />
        </FilterExpansionPanel>
      </v-expansion-panels>
    </v-list>
  </TempDrawer>

  <ItemBrowser
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
import ChipCount from '@/components/ChipCount.vue'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import {
  clearFilter,
  schemaFilterAgeRatingToConditions,
  schemaFilterAuthorsToConditions,
  schemaFilterIncludeExcludeToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterReleaseYearToConditions,
  schemaFilterSeriesStatusToConditions,
  schemaFilterStringToConditions,
  valuesToConditions,
} from '@/functions/filter'
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePresentationMode } from '@/composables/presentationMode'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useFilterAuthors } from '@/composables/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import {
  SchemaFilterReadStatus,
  SchemaFilterSeriesStatus,
  SchemaFilterStrings,
  SchemaIncludeExclude,
  SchemaSeriesAgeRatings,
  SchemaSeriesReleaseYears,
} from '@/types/filter'
import type { components } from '@/generated/openapi/komga'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { seriesListQuery } from '@/colada/series'
import { PageRequest, sortToString, type Sort } from '@/types/PageRequest'
import { komgaClient } from '@/api/komga-client'
import { collectionDetailQuery } from '@/colada/collections'

const route = useRoute('/collection/[id]')
const collectionId = computed(() => route.params.id)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `collection_${collectionId.value}`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const { data: collection } = useQuery(() => ({
  ...collectionDetailQuery({
    collectionId: collectionId.value,
  }),
}))

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

function clearFilters() {
  clearFilter(filterSeriesStatus.value)
  clearFilter(filterLibrary.value)
  clearFilter(filterReadStatus.value)
  clearFilter(filterGenre.value)
  clearFilter(filterTag.value)
  clearFilter(filterPublisher.value)
  clearFilter(filterSharingLabel.value)
  clearFilter(filterLanguage.value)
  clearFilter(filterComplete.value)
  clearFilter(filterUnavailable.value)
  clearFilter(filterOneShot.value)
  clearFilter(filterReleaseYear.value)
  clearFilter(filterAgeRating.value)
  Object.entries(filterAuthors).map(([, filter]) => clearFilter(filter.filter))
}

const filterCount = computed(
  () =>
    (!!filterComplete.value.i ? 1 : 0) +
    (!!filterUnavailable.value.i ? 1 : 0) +
    (!!filterOneShot.value.i ? 1 : 0) +
    filterReadStatus.value.v.length +
    filterLibrary.value.v.length +
    filterSeriesStatus.value.v.length +
    filterGenre.value.v.length +
    filterTag.value.v.length +
    filterPublisher.value.v.length +
    (!!filterReleaseYear.value.is ? 1 : !!filterReleaseYear.value.min ? 1 : 0) +
    (!!filterAgeRating.value.is ? 1 : !!filterAgeRating.value.min ? 1 : 0) +
    filterLanguage.value.v.length +
    filterSharingLabel.value.v.length +
    Object.entries(filterAuthors)
      .map(([, filter]) => filter.filter.v.length)
      .reduce((sum, item) => sum + item, 0),
)

const { filterAuthors } = useFilterAuthors()
const { data: filterLibrary } = useRouteQuerySchema('library', SchemaFilterStrings)
const { data: filterSeriesStatus } = useRouteQuerySchema('status', SchemaFilterSeriesStatus)
const { data: filterReadStatus } = useRouteQuerySchema('read', SchemaFilterReadStatus)
const { data: filterGenre } = useRouteQuerySchema('genre', SchemaFilterStrings)
const { data: filterTag } = useRouteQuerySchema('tag', SchemaFilterStrings)
const { data: filterPublisher } = useRouteQuerySchema('publisher', SchemaFilterStrings)
const { data: filterSharingLabel } = useRouteQuerySchema('sharingLabel', SchemaFilterStrings)
const { data: filterLanguage } = useRouteQuerySchema('language', SchemaFilterStrings)
const { data: filterReleaseYear } = useRouteQuerySchema('year', SchemaSeriesReleaseYears)
const { data: filterAgeRating } = useRouteQuerySchema('age', SchemaSeriesAgeRatings)
const { data: filterComplete } = useRouteQuerySchema('complete', SchemaIncludeExclude)
const { data: filterUnavailable } = useRouteQuerySchema('unavailable', SchemaIncludeExclude)
const { data: filterOneShot } = useRouteQuerySchema('oneshot', SchemaIncludeExclude)

const sortActive = computed<Sort[]>(() => {
  if (collection.value?.ordered) return [{ key: 'collection.number', order: 'asc' }]
  else return [{ key: 'metadata.titleSort', order: 'asc' }]
})

const conds = computed(() => ({
  allOf: [
    valuesToConditions([collectionId.value], 'collectionId'),
    schemaFilterIncludeExcludeToConditions(filterComplete.value, 'complete'),
    schemaFilterIncludeExcludeToConditions(filterUnavailable.value, 'deleted'),
    schemaFilterIncludeExcludeToConditions(filterOneShot.value, 'oneShot'),
    schemaFilterStringToConditions(filterLibrary.value, 'libraryId', false),
    schemaFilterSeriesStatusToConditions(filterSeriesStatus.value),
    schemaFilterReadStatusToConditions(filterReadStatus.value),
    schemaFilterStringToConditions(filterGenre.value, 'genre', true),
    schemaFilterStringToConditions(filterTag.value, 'tag', true),
    schemaFilterStringToConditions(filterPublisher.value, 'publisher', false),
    schemaFilterStringToConditions(filterSharingLabel.value, 'sharingLabel', true),
    schemaFilterStringToConditions(filterLanguage.value, 'language', false),
    schemaFilterReleaseYearToConditions(filterReleaseYear.value),
    schemaFilterAgeRatingToConditions(filterAgeRating.value),
    ...Object.entries(filterAuthors).map(([, filter]) =>
      schemaFilterAuthorsToConditions(toValue(filter.filter), toValue(filter.role)),
    ),
  ].filter(Boolean),
}))

// clear selection if filter or paging changes
watch([conds, () => appStore.browsingPaging], () => selectionStore.clear())

const apiQuery = computed(() => ({
  condition: conds.value as components['schemas']['AllOfSeries'],
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
} = useInfiniteQuery({
  key: () => ['infinite_series', apiQuery.value, sortActive.value],
  initialPageParam: new PageRequest(0, 50, sortActive.value),
  query: ({ pageParam }) =>
    komgaClient
      .POST('/api/v1/series/list', {
        body: apiQuery.value,
        params: {
          query: {
            page: pageParam.page,
            size: pageParam.size,
            sort: sortActive.value.map((it) => sortToString(it)),
          },
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
  getNextPageParam: (lastPage, _, lastPageParam) => (!lastPage?.last ? lastPageParam.next() : null),
  enabled: isBrowsingScroll,
})
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
