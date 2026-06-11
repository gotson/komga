<template>
  <v-app-bar>
    <ChipCount
      :count="totalElements"
      class="ms-4"
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
          :title="$formatMessage(commonMessages.filterPanelLibrary)"
          :count="filters.library.count"
          @clear="filters.library.clear()"
        >
          <FilterByLibrary v-model="filters.library.filter.v" />
        </FilterExpansionPanel>

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
    </v-list>
  </TempDrawer>

  <div
    v-if="collection"
    class="d-flex align-center px-4 pt-2 ga-2"
  >
    <div class="text-title-large text-truncate">{{ collection.name }}</div>
    <CollectionMenuButton :collection="collection" />
  </div>
  <v-divider class="mt-4" />

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
import ChipCount from '@/components/ChipCount.vue'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import {
  countFilter,
  schemaFilterAgeRatingToConditions,
  schemaFilterAuthorsToConditions,
  schemaFilterIncludeExcludeToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterReleaseYearToConditions,
  schemaFilterStringToConditions,
  valuesToConditions,
} from '@/functions/filter'
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePresentationMode } from '@/composables/presentationMode'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useFilterContributors, useFilters } from '@/composables/filter'
import { filterKeys } from '@/types/filter'
import type { components } from '@/generated/openapi/komga'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { seriesListQuery, seriesListQueryInfinite } from '@/colada/series'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { collectionDetailQuery } from '@/colada/collections'
import CollectionMenuButton from '@/components/collection/menu/CollectionMenuButton.vue'
import { contributorsRolesMessages } from '@/types/referential'

const route = useRoute('/collection/[id]')
const router = useRouter()
const collectionId = computed(() => route.params.id)

provide(
  filterKeys.context,
  computed(() => ({ collection_id: [collectionId.value] })),
)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `collection_${collectionId.value}`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const { data: collection, error } = useQuery(() => ({
  ...collectionDetailQuery({
    collectionId: collectionId.value,
  }),
}))
// redirect to home if the entity is deleted
watch(error, () => router.push('/'))

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
  'library',
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

const sortActive = computed<Sort[]>(() => {
  if (collection.value?.ordered) return [{ key: 'collection.number', order: 'asc' }]
  else return [{ key: 'metadata.titleSort', order: 'asc' }]
})

const conds = computed(() => ({
  allOf: [
    valuesToConditions([collectionId.value], 'collectionId'),
    schemaFilterStringToConditions(filters.value.library.filter, 'libraryId', false),
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
