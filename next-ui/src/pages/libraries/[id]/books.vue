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
      v-if="appStore.isBrowsingPaged"
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
          :title="$formatMessage(commonMessages.filterPanelTag)"
          :count="filterTag.v.length"
          @clear="clearFilter(filterTag)"
        >
          <FilterByTag
            v-model="filterTag.v"
            v-model:mode="filterTag.m"
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
        mandatory
        multi-sort
      />
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
import { usePresentationMode } from '@/composables/presentationMode'
import { useGetLibrariesById } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { SchemaFilterReadStatus, SchemaFilterStrings, SchemaIncludeExclude } from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { useIntlFormatter } from '@/composables/intlFormatter'
import { sortBooks } from '@/types/sort'
import type { components } from '@/generated/openapi/komga'
import {
  clearFilter,
  schemaFilterAuthorsToConditions,
  schemaFilterIncludeExcludeToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterStringToConditions,
  valuesToConditions,
} from '@/functions/filter'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { PageRequest, sortToString } from '@/types/PageRequest'
import { komgaClient } from '@/api/komga-client'
import { bookListQuery } from '@/colada/books'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useFilterAuthors } from '@/composables/filter'
import ChipCount from '@/components/ChipCount.vue'

const route = useRoute('/libraries/[id]/books')
const libraryId = route.params.id
const { libraryIds } = useGetLibrariesById(libraryId)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `${libraryId}_books`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

function clearFilters() {
  clearFilter(filterReadStatus.value)
  clearFilter(filterTag.value)
  clearFilter(filterUnavailable.value)
  clearFilter(filterOneShot.value)
  Object.entries(filterAuthors).map(([, filter]) => clearFilter(filter.filter))
}

const filterCount = computed(
  () =>
    (!!filterUnavailable.value.i ? 1 : 0) +
    (!!filterOneShot.value.i ? 1 : 0) +
    filterReadStatus.value.v.length +
    filterTag.value.v.length +
    Object.entries(filterAuthors)
      .map(([, filter]) => filter.filter.v.length)
      .reduce((sum, item) => sum + item, 0),
)

const { filterAuthors } = useFilterAuthors()
const { data: filterReadStatus } = useRouteQuerySchema('read', SchemaFilterReadStatus)
const { data: filterTag } = useRouteQuerySchema('tag', SchemaFilterStrings)
const { data: filterUnavailable } = useRouteQuerySchema('unavailable', SchemaIncludeExclude)
const { data: filterOneShot } = useRouteQuerySchema('oneshot', SchemaIncludeExclude)

const { convertSortOptionDescriptor } = useIntlFormatter()
const sortActive = appStore.getSortActive(viewName.value, [
  { key: 'series', order: 'asc' },
  { key: 'metadata.numberSort', order: 'asc' },
])
const sortOptions = sortBooks.map((it) => convertSortOptionDescriptor(it))

const conds = computed(() => ({
  allOf: [
    valuesToConditions(libraryIds.value, 'libraryId'),
    schemaFilterIncludeExcludeToConditions(filterUnavailable.value, 'deleted'),
    schemaFilterIncludeExcludeToConditions(filterOneShot.value, 'oneShot'),
    schemaFilterReadStatusToConditions(filterReadStatus.value),
    schemaFilterStringToConditions(filterTag.value, 'tag', true),
    ...Object.entries(filterAuthors).map(([, filter]) =>
      schemaFilterAuthorsToConditions(toValue(filter.filter), toValue(filter.role)),
    ),
  ].filter(Boolean),
}))

// clear selection if filter or paging changes
watch([conds, () => appStore.browsingPaging], () => selectionStore.clear())

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
} = useInfiniteQuery({
  key: () => ['infinite_books', apiQuery.value, sortActive.value],
  initialPageParam: new PageRequest(0, 50, sortActive.value),
  query: ({ pageParam }) =>
    komgaClient
      .POST('/api/v1/books/list', {
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
