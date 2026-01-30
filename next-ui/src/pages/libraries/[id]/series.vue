<template>
  <v-app-bar>
    <v-spacer />

    <v-slider
      v-if="display.smAndUp.value"
      v-model="appStore.gridCardWidth"
      :min="130"
      :max="200"
      color="surface-darken"
      hide-details
      thumb-size="15"
      max-width="80"
    />

    <PresentationSelector
      v-if="display.smAndUp.value"
      v-model="presentationMode"
      :modes="['grid', 'list']"
      toggle
    />

    <PageSizeSelector
      v-model="appStore.browsingPageSize"
      allow-unpaged
      :sizes="[1, 10, 20]"
    />
  </v-app-bar>

  <div>FILTER</div>
  <div>{{ filter }}</div>
  <div>CONDITION</div>
  <div>{{ conds }}</div>

  <FilterList
    v-model="filter.v"
    v-model:mode="filter.m"
    :items="filterStatusItems"
  />

  <template v-if="series">
    <v-data-iterator
      v-model="selectedItems"
      return-object
      :items="series.content"
      :items-per-page="itemsPerPage"
      :page="page1"
      show-select
    >
      <template #default="{ items, toggleSelect, isSelected }">
        <v-container
          v-if="presentationModeEffective === 'grid'"
          fluid
        >
          <v-row>
            <v-col
              v-for="item in items"
              :key="item.raw.id"
              cols="auto"
            >
              <SeriesCard
                stretch-poster
                :series="item.raw"
                :selected="isSelected(item)"
                :pre-select="preSelect"
                :width="display.xs.value ? undefined : appStore.gridCardWidth"
                @selection="toggleSelect(item)"
              />
            </v-col>
          </v-row>
        </v-container>

        <v-container
          v-if="presentationModeEffective === 'list'"
          fluid
        >
          <v-row
            v-for="item in items"
            :key="item.raw.id"
          >
            <v-col>
              <SeriesCardWide
                stretch-poster
                :series="item.raw"
                :selected="isSelected(item)"
                :pre-select="preSelect"
                :width="appStore.gridCardWidth"
                @selection="toggleSelect(item)"
              />
            </v-col>
          </v-row>
        </v-container>
      </template>
    </v-data-iterator>

    <v-pagination
      v-model="page1"
      :length="pageCount"
    ></v-pagination>
  </template>
</template>

<script lang="ts" setup>
import { useQuery } from '@pinia/colada'
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
import { schemaFilterSeriesStatusToConditions } from '@/functions/filter'
import * as v from 'valibot'
import { SchemaFilterSeriesStatus, SchemaSeriesStatus } from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'

const route = useRoute('/libraries/[id]/series')
const libraryId = route.params.id
const { libraries } = useGetLibrariesById(libraryId)
const { librariesCondition } = useSearchConditionLibraries(libraries)

const display = useDisplay()
const appStore = useAppStore()
const { browsingPageSize } = storeToRefs(appStore)
const presentationMode = appStore.getPresentationMode(`${libraryId}_series`, 'grid')
const presentationModeEffective = computed(() =>
  display.xs.value ? 'grid' : presentationMode.value,
)

const { itemsPerPage } = useItemsPerPage(browsingPageSize)
const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()
const { selection: selectedItems } = storeToRefs(selectionStore)
const preSelect = computed(() => selectedItems.value.length > 0)

const { data: filter } = useRouteQuerySchema('status', SchemaFilterSeriesStatus)

const conds = computed(() => ({
  allOf: [
    librariesCondition.value as components['schemas']['AnyOfSeries'],
    schemaFilterSeriesStatusToConditions(filter.value),
  ],
}))

const { data: series } = useQuery(seriesListQuery, () => {
  const search: components['schemas']['SeriesSearch'] = {
    // condition: librariesCondition.value as components['schemas']['AnyOfSeries'],
    condition: conds.value as components['schemas']['AllOfSeries'],
  }

  return {
    search: search,
    pageRequest: PageRequest.FromPageSize(appStore.browsingPageSize, page0.value),
  }
})

watch(series, (newSeries) => {
  if (newSeries) pageCount.value = newSeries.totalPages ?? 0
})

const filterStatusItems: {
  title: string
  value: v.InferOutput<typeof SchemaSeriesStatus>
  valueExclude?: v.InferOutput<typeof SchemaSeriesStatus>
}[] = [
  { title: 'Ended', value: { i: 'i', v: 'ENDED' }, valueExclude: { i: 'e', v: 'ENDED' } },
  { title: 'Ongoing', value: { i: 'i', v: 'ONGOING' }, valueExclude: { i: 'e', v: 'ONGOING' } },
  { title: 'Hiatus', value: { i: 'i', v: 'HIATUS' }, valueExclude: { i: 'e', v: 'HIATUS' } },
  {
    title: 'Abandoned',
    value: { i: 'i', v: 'ABANDONED' },
    valueExclude: { i: 'e', v: 'ABANDONED' },
  },
]
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
