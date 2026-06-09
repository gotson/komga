<template>
  <v-app-bar>
    <span class="ms-4 text-title-large">{{ readList?.name }}</span>
    <ReadlistMenuButton
      v-if="readList"
      :read-list="readList"
      class="mx-2"
    />

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
          :count="countFilter(filterLibrary)"
          @clear="clearFilter(filterLibrary)"
        >
          <FilterByLibrary v-model="filterLibrary.v" />
        </FilterExpansionPanel>

        <FilterExpansionPanel
          :title="$formatMessage(commonMessages.filterPanelTag)"
          :count="countFilter(filterTag)"
          @clear="clearFilter(filterTag)"
        >
          <FilterByTag
            v-model="filterTag.v"
            v-model:mode="filterTag.m"
            include="BOOK"
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
          v-for="(filterAuthor, role) in filterCreators"
          :key="role"
          :title="
            creatorRolesMessages?.[role] ? $formatMessage(creatorRolesMessages?.[role]) : role
          "
          :count="countFilter(filterAuthor)"
          @clear="filterCreatorsClear(role)"
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

  <v-container
    v-if="readList?.summary"
    fluid
  >
    <v-row>
      <v-col cols="12">
        <ReadMore :text="readList.summary" />
      </v-col>
    </v-row>
    <v-divider class="mt-4" />
  </v-container>

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
        show-series
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
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePresentationMode } from '@/composables/presentationMode'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { usePagination } from '@/composables/pagination'
import { readListDetailQuery } from '@/colada/readlists'
import { useSelectionStore } from '@/stores/selection'
import { PageRequest, type Sort } from '@/types/PageRequest'
import {
  clearFilter,
  countFilter,
  schemaFilterAuthorsToConditions,
  schemaFilterIncludeExcludeToConditions,
  schemaFilterReadStatusToConditions,
  schemaFilterStringToConditions,
  valuesToConditions,
} from '@/functions/filter'
import type { components } from '@/generated/openapi/komga'
import { bookListQuery, bookListQueryInfinite } from '@/colada/books'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import ChipCount from '@/components/ChipCount.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import ReadlistMenuButton from '@/components/readlist/menu/ReadlistMenuButton.vue'
import { useFilterCreators } from '@/composables/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import {
  filterKeys,
  SchemaFilterReadStatus,
  SchemaFilterStrings,
  SchemaIncludeExclude,
} from '@/types/filter'
import { commonMessages } from '@/utils/i18n/common-messages'
import { creatorRolesMessages } from '@/types/referential'

const route = useRoute('/readlist/[id]')
const router = useRouter()
const readListId = computed(() => route.params.id)

provide(
  filterKeys.context,
  computed(() => ({ readlist_id: [readListId.value] })),
)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `readlist_${readListId.value}`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const { data: readList, error } = useQuery(() => ({
  ...readListDetailQuery({
    readListId: readListId.value,
  }),
}))
// redirect to home if the entity is deleted
watch(error, () => router.push('/'))

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

function clearFilters() {
  clearFilter(filterLibrary.value)
  clearFilter(filterReadStatus.value)
  clearFilter(filterTag.value)
  clearFilter(filterUnavailable.value)
  clearFilter(filterOneShot.value)
  filterCreatorsClearAll()
}

const filterCount = computed(
  () =>
    countFilter(filterLibrary.value) +
    countFilter(filterReadStatus.value) +
    countFilter(filterTag.value) +
    countFilter(filterUnavailable.value) +
    countFilter(filterOneShot.value) +
    filterCreatorsCount.value,
)

const sortActive = computed<Sort[]>(() => {
  if (readList.value?.ordered) return [{ key: 'readList.number', order: 'asc' }]
  else return [{ key: 'metadata.releaseDate', order: 'asc' }]
})

const {
  filter: filterCreators,
  countAll: filterCreatorsCount,
  clearAll: filterCreatorsClearAll,
  clear: filterCreatorsClear,
} = useFilterCreators()
const { data: filterLibrary } = useRouteQuerySchema('library', SchemaFilterStrings)
const { data: filterReadStatus } = useRouteQuerySchema('read', SchemaFilterReadStatus)
const { data: filterTag } = useRouteQuerySchema('tag', SchemaFilterStrings)
const { data: filterUnavailable } = useRouteQuerySchema('unavailable', SchemaIncludeExclude)
const { data: filterOneShot } = useRouteQuerySchema('oneshot', SchemaIncludeExclude)

const conds = computed(() => ({
  allOf: [
    valuesToConditions([readListId.value], 'readListId'),
    schemaFilterIncludeExcludeToConditions(filterUnavailable.value, 'deleted'),
    schemaFilterIncludeExcludeToConditions(filterOneShot.value, 'oneShot'),
    schemaFilterStringToConditions(filterLibrary.value, 'libraryId', false),
    schemaFilterReadStatusToConditions(filterReadStatus.value),
    schemaFilterStringToConditions(filterTag.value, 'tag', true),
    ...Object.entries(filterCreators.value).map(([role, filter]) =>
      schemaFilterAuthorsToConditions(filter, role),
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
