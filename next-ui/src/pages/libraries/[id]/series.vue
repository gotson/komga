<template>
  <v-app-bar>
    <v-spacer />

    <PosterSizeSlider />

    <PresentationSelector
      v-if="display.smAndUp.value"
      v-model="presentationMode"
      :modes="['grid', 'list']"
      toggle
    />

    <PageSizeSelector
      v-if="appStore.isBrowsingPaged"
      v-model="appStore.browsingPageSize"
      allow-unpaged
      :sizes="[1, 10, 20]"
    />

    <PagingSelector
      v-model="appStore.browsingPaging"
      class="px-2"
    />

    <!-- We use padding end so that the badge is displayed properly, else it goes off screen -->
    <v-badge
      location="top right"
      color="primary"
      :content="filterCount"
      :model-value="filterCount > 0"
      class="pe-4"
      offset-x="7"
      offset-y="7"
    >
      <v-icon-btn
        icon="i-mdi:filter-variant"
        @click="filterDrawer = true"
      />
    </v-badge>
  </v-app-bar>

  <TempDrawer v-model="filterDrawer">
    <v-list>
      <v-list-subheader>
        <div class="d-flex ga-2 align-center mb-1">
          <span>{{ $formatMessage(commonMessages.filterPanelHeader) }}</span>
          <v-chip
            v-if="filterCount > 0"
            color="primary"
            rounded
            closable
            variant="elevated"
            size="small"
            @click:close="clearFilters()"
          >
            {{ filterCount }}
          </v-chip>
        </div>
      </v-list-subheader>

      <v-expansion-panels
        v-model="filterExpansionPanels"
        variant="accordion"
        class="no-padding"
        flat
        tile
      >
        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelReadStatus)"
          :count="filterReadStatus.v.length"
          @clear="clearFilter(filterReadStatus)"
        >
          <FilterByReadStatus v-model="filterReadStatus.v" />
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
          @clear="clearFilterSelectRange(filterReleaseYear)"
        >
          <FilterByReleaseYear v-model="filterReleaseYear" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelAgeRating)"
          :count="!!filterAgeRating.is ? 1 : !!filterAgeRating.min ? 1 : 0"
          @clear="clearFilterSelectRange(filterAgeRating)"
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

      <v-divider />

      <v-list-subheader>{{ $formatMessage(commonMessages.filterPanelSort) }}</v-list-subheader>

      <SortList
        v-model="sortActive"
        :items="sortOptions"
        color="primary"
        mandatory
      />
    </v-list>
  </TempDrawer>

  <v-data-iterator
    v-model="selectedItems"
    return-object
    :items="seriesItems"
    :items-per-page="appStore.browsingPaging === 'paged' ? itemsPerPage : -1"
    :page="appStore.browsingPaging === 'paged' ? page1 : 1"
    show-select
  >
    <template #default="{ items, toggleSelect, isSelected }">
      <v-container
        v-if="presentationModeEffective === 'grid'"
        fluid
      >
        <v-row>
          <v-col
            v-for="(item, idx) in items"
            :key="item.raw.id"
            cols="auto"
          >
            <SeriesCard
              stretch-poster
              :series="item.raw"
              :selected="isSelected(item)"
              :pre-select="preSelect"
              :width="display.xs.value ? undefined : appStore.gridCardWidth"
              @selection="(_val, event) => toggleSelect(item, idx, event as MouseEvent)"
            />
          </v-col>
        </v-row>
      </v-container>

      <v-container
        v-if="presentationModeEffective === 'list'"
        fluid
      >
        <v-row
          v-for="(item, idx) in items"
          :key="item.raw.id"
        >
          <v-col>
            <SeriesCardWide
              stretch-poster
              :series="item.raw"
              :selected="isSelected(item)"
              :pre-select="preSelect"
              :width="appStore.gridCardWidth"
              @selection="(_val, event) => toggleSelect(item, idx, event as MouseEvent)"
            />
          </v-col>
        </v-row>
      </v-container>
    </template>
  </v-data-iterator>

  <v-pagination
    v-if="appStore.isBrowsingPaged"
    v-model="page1"
    :length="pageCount"
  />

  <div
    v-if="appStore.isBrowsingScroll && hasNextPage"
    v-intersect="(isIntersecting: boolean) => (isIntersecting ? loadMore() : undefined)"
    style="min-height: 40px"
  ></div>
</template>

<script lang="ts" setup>
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { seriesListQuery } from '@/colada/series'
import type { components } from '@/generated/openapi/komga'
import { PageRequest } from '@/types/PageRequest'
import { useGetLibrariesById } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { useItemsPerPage, usePagination } from '@/composables/pagination'
import { useSearchConditionLibraries } from '@/composables/search'
import { storeToRefs } from 'pinia'
import { useSelectionStore } from '@/stores/selection'
import { useDisplay } from 'vuetify'
import {
  schemaFilterAuthorsToConditions,
  schemaFilterStringToConditions,
  schemaFilterSeriesStatusToConditions,
  schemaFilterReleaseYearToConditions,
  schemaFilterAgeRatingToConditions,
  schemaFilterReadStatusToConditions,
} from '@/functions/filter'
import * as v from 'valibot'
import {
  type FilterType,
  type FilterTypeSelectRange,
  SchemaFilterAuthors,
  SchemaFilterReadStatus,
  SchemaFilterSeriesStatus,
  SchemaFilterStrings,
  SchemaSeriesAgeRatings,
  SchemaSeriesReleaseYears,
} from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { authorRoles } from '@/types/referential'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortSeries } from '@/types/sort'
import { komgaClient } from '@/api/komga-client'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'

const route = useRoute('/libraries/[id]/series')
const libraryId = route.params.id
const { libraries } = useGetLibrariesById(libraryId)
const { librariesCondition } = useSearchConditionLibraries(libraries)

const intl = useIntl()
const display = useDisplay()
const appStore = useAppStore()
const { browsingPageSize } = storeToRefs(appStore)
const viewName = computed(() => `${libraryId}_series`)
const presentationMode = appStore.getPresentationMode(viewName.value, 'grid')
const presentationModeEffective = computed(() =>
  display.xs.value ? 'grid' : presentationMode.value,
)

const { itemsPerPage } = useItemsPerPage(browsingPageSize)
const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()
const { selection: selectedItems } = storeToRefs(selectionStore)
const preSelect = computed(() => selectedItems.value.length > 0)

type AuthorQuery = v.InferOutput<typeof SchemaFilterAuthors>
const filterAuthors = reactive<
  Record<string, { filter: AuthorQuery; text: string; role?: string }>
>({})

filterAuthors['anyrole'] = {
  filter: useRouteQuerySchema('anyrole', SchemaFilterAuthors).data.value,
  text: intl.formatMessage({
    description: 'Author filter: any role',
    defaultMessage: 'All creators',
    id: 'RmNasP',
  }),
}
// TODO: get roles dynamically
Object.entries(authorRoles).forEach(([role, value]) => {
  filterAuthors[role] = {
    filter: useRouteQuerySchema(role, SchemaFilterAuthors).data.value,
    text: intl.formatMessage(value),
    role: role,
  }
})

function clearFilter(filter: FilterType) {
  filter.v = []
  if ('m' in filter) filter.m = 'anyOf'
}

function clearFilterSelectRange(filter: FilterTypeSelectRange) {
  filter.is = undefined
  filter.min = undefined
  filter.max = undefined
}

function clearFilters() {
  clearFilter(filterSeriesStatus.value)
  clearFilter(filterReadStatus.value)
  clearFilter(filterGenre.value)
  clearFilter(filterTag.value)
  clearFilter(filterPublisher.value)
  clearFilter(filterSharingLabel.value)
  clearFilter(filterLanguage.value)
  clearFilterSelectRange(filterReleaseYear.value)
  clearFilterSelectRange(filterAgeRating.value)
  Object.entries(filterAuthors).map(([, filter]) => clearFilter(filter.filter))
}

const filterCount = computed(
  () =>
    filterReadStatus.value.v.length +
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

const { data: filterSeriesStatus } = useRouteQuerySchema('status', SchemaFilterSeriesStatus)
const { data: filterReadStatus } = useRouteQuerySchema('read', SchemaFilterReadStatus)
const { data: filterGenre } = useRouteQuerySchema('genre', SchemaFilterStrings)
const { data: filterTag } = useRouteQuerySchema('tag', SchemaFilterStrings)
const { data: filterPublisher } = useRouteQuerySchema('publisher', SchemaFilterStrings)
const { data: filterSharingLabel } = useRouteQuerySchema('sharingLabel', SchemaFilterStrings)
const { data: filterLanguage } = useRouteQuerySchema('language', SchemaFilterStrings)
const { data: filterReleaseYear } = useRouteQuerySchema('year', SchemaSeriesReleaseYears)
const { data: filterAgeRating } = useRouteQuerySchema('age', SchemaSeriesAgeRatings)

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortActive = appStore.getSortActive(viewName.value, [
  { key: 'metadata.titleSort', order: 'asc' },
])
const sortOptions = sortSeries.map((it) => convertSortOptionDescriptor(it))

const conds = computed(() => ({
  allOf: [
    librariesCondition.value as components['schemas']['AnyOfSeries'],
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

const { data: series } = useQuery(() => ({
  ...seriesListQuery({
    search: { ...apiQuery.value },
    pageRequest: PageRequest.FromPageSize(appStore.browsingPageSize, page0.value, sortActive.value),
  }),
  enabled: appStore.isBrowsingPaged,
}))

watch(series, (newSeries) => {
  if (newSeries) pageCount.value = newSeries.totalPages ?? 0
})

const {
  data: infiniteData,
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
  enabled: appStore.isBrowsingScroll,
})
const infiniteSeries = computed(() => infiniteData.value?.pages.flatMap((it) => it?.content ?? []))

const seriesItems = computed(() =>
  appStore.isBrowsingPaged ? series.value?.content : infiniteSeries.value,
)

function loadMore() {
  void loadNextPage()
}

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
