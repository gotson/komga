<template>
  <v-app-bar>
    <template #prepend>
      <NavigationBreadcrumbs />
    </template>
  </v-app-bar>

  <TempDrawer v-model="filterDrawer">
    <FilterPanel
      v-model="filters"
      :count="filtersCountAll"
      @clear="filtersClearAll()"
    />
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

    <template v-else-if="collection">
      <CollectionView :collection="collection" />

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
          <SeriesCard
            v-if="presentationModeEffective === 'grid'"
            :stretch-poster="appStore.stretchPoster"
            :series="item"
            :selected="isSelected"
            :pre-select="preSelect"
            :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
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
  </v-container>
</template>

<script lang="ts" setup>
import ChipCount from '@/components/ChipCount.vue'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import FilterButton from '@/components/filter/FilterButton.vue'
import { filtersToConditions, valuesToConditions } from '@/functions/filter'
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'
import { usePresentationMode } from '@/composables/presentationMode'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useFilters } from '@/composables/filter'
import { filterKeys } from '@/types/filter'
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { seriesListQuery, seriesListQueryInfinite } from '@/colada/series'
import { PageRequest, type Sort } from '@/types/PageRequest'
import { collectionDetailQuery } from '@/colada/collections'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import { useSelectionContextualActions } from '@/composables/selection'
import type { SearchConditionSeries } from '@/generated/openapi'
import { useBrowsingContextFromRouteQuery } from '@/composables/browsingContext'
import { BrowsingContextKey, pushBrowsingContext } from '@/functions/browsing-context'

const route = useRoute('/collection/[id]')
const router = useRouter()
const collectionId = computed(() => route.params.id)

const filterContext = computed(() => ({ collection_id: [collectionId.value] }))
provide(filterKeys.context, filterContext)

const { context } = useBrowsingContextFromRouteQuery()
provide(
  BrowsingContextKey,
  computed(() =>
    pushBrowsingContext(context.value, { type: 'collection', id: collectionId.value }),
  ),
)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const viewName = computed(() => `collection_${collectionId.value}`)
const { presentationMode, presentationModeEffective } = usePresentationMode(viewName)

const {
  data: collection,
  error,
  isPending,
} = useQuery(() => ({
  ...collectionDetailQuery({
    collectionId: collectionId.value,
  }),
}))
// redirect to home if the entity is deleted
watch(error, () => router.push('/'))

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

const {
  filters,
  clearAll: filtersClearAll,
  countAll: filtersCountAll,
} = useFilters(
  [
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
    'contributors',
    'notableEntities',
  ],
  { context: filterContext, viewName: viewName.value, persist: true },
)

const sortActive = computed<Sort[]>(() => {
  if (collection.value?.ordered) return [{ key: 'collection.number', order: 'asc' }]
  else return [{ key: 'metadata.titleSort', order: 'asc' }]
})

const conds = computed(() => ({
  allOf: [
    valuesToConditions([collectionId.value], 'collectionId'),
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

const filterDrawer = ref(false)

useSelectionContextualActions(dataItems)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
