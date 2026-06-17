<template>
  <v-app-bar>
    <ChipCount
      class="ms-4"
      :count="totalElements"
    />

    <v-spacer />

    <PosterSizeSlider />

    <PageSizeSelector
      v-if="appStore.isBrowsingPaged"
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
        multi-sort
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
    presentation-mode="grid"
    :has-next-page="hasNextPage"
    :page-count="pageCount"
    @load-next-page="loadNextPage()"
  >
    <template #default="{ item, isSelected, preSelect, toggleSelect }">
      <BookCard
        show-series
        stretch-poster
        :book="item"
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />
    </template>
  </ItemBrowser>
</template>

<script lang="ts" setup>
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { useDisplay } from 'vuetify'
import { useGetLibrariesById } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortBooks } from '@/types/sort'
import type { components } from '@/generated/openapi/komga'
import {
  countFilter,
  schemaFilterAuthorsToConditions,
  schemaFilterIncludeExcludeToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterStringToConditions,
  valuesToConditions,
} from '@/functions/filter'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { PageRequest } from '@/types/PageRequest'
import { bookListQuery, bookListQueryInfinite } from '@/colada/books'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useFilterContributors, useFilters } from '@/composables/filter'
import ChipCount from '@/components/ChipCount.vue'
import { contributorsRolesMessages } from '@/types/referential'

const route = useRoute('/libraries/[id]/books')
const libraryId = route.params.id
const { libraryIds } = useGetLibrariesById(libraryId)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `${libraryId}_books`)

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
} = useFilters(['mediaStatus', 'profile', 'read', 'tag', 'unavailable', 'oneshot'])

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortActive = appStore.getSortActive(viewName.value, [
  { key: 'series', order: 'asc' },
  { key: 'metadata.numberSort', order: 'asc' },
])
const sortOptions = sortBooks.map((it) => convertSortOptionDescriptor(it))

const conds = computed(() => ({
  allOf: [
    valuesToConditions(libraryIds.value, 'libraryId'),
    schemaFilterIncludeExcludeToConditions(filters.value.unavailable.filter, 'deleted'),
    schemaFilterIncludeExcludeToConditions(filters.value.oneshot.filter, 'oneShot'),
    schemaFilterReadStatusToConditions(filters.value.read.filter),
    valuesToConditions(filters.value.mediaStatus.filter.v, 'mediaStatus'),
    valuesToConditions(filters.value.profile.filter.v, 'mediaProfile'),
    schemaFilterStringToConditions(filters.value.tag.filter, 'tag', true),
    ...Object.entries(filterContributors.value).map(([role, filter]) =>
      schemaFilterAuthorsToConditions(filter, role),
    ),
  ].filter(Boolean),
}))

// clear selection if filter or paging changes
watch([() => JSON.stringify(conds.value), () => appStore.browsingPaging], () =>
  selectionStore.clear(),
)

const apiQuery = computed(() => ({
  condition: conds.value as components['schemas']['AllOfBook'],
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
