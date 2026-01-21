<template>
  <v-app-bar>
    <v-spacer />

    <v-slider
      v-if="presentationMode === 'grid'"
      v-model="appStore.gridCardWidth"
      :min="130"
      :max="200"
      color="surface-darken"
      hide-details
      thumb-size="15"
      max-width="80"
    />

    <PresentationSelector
      v-model="presentationMode"
      :modes="['grid', 'list']"
    />

    <PageSizeSelector
      v-model="appStore.browsingPageSize"
      allow-unpaged
      :sizes="[1, 10, 20]"
    />
  </v-app-bar>

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
          v-if="presentationMode === 'grid'"
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
                :width="appStore.gridCardWidth"
                @selection="toggleSelect(item)"
              />
            </v-col>
          </v-row>
        </v-container>

        <v-list v-if="presentationMode === 'list'">
          <v-list-item
            v-for="item in items"
            :key="item.raw.id"
            :title="item.raw.metadata.title"
            :base-color="isSelected(item) ? 'red' : 'blue'"
            @click="toggleSelect(item)"
          />
        </v-list>
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

const route = useRoute('/libraries/[id]/series')
const libraryId = route.params.id
const { libraries } = useGetLibrariesById(libraryId)
const { librariesCondition } = useSearchConditionLibraries(libraries)

const appStore = useAppStore()
const { browsingPageSize } = storeToRefs(appStore)
const presentationMode = appStore.getPresentationMode(`${libraryId}_series`, 'grid')

const { itemsPerPage } = useItemsPerPage(browsingPageSize)
const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()
const { selection: selectedItems } = storeToRefs(selectionStore)
const preSelect = computed(() => selectedItems.value.length > 0)

const { data: series } = useQuery(seriesListQuery, () => {
  const search: components['schemas']['SeriesSearch'] = {
    condition: librariesCondition.value as components['schemas']['AnyOfSeries'],
  }

  return {
    search: search,
    pageRequest: PageRequest.FromPageSize(appStore.browsingPageSize, page0.value),
  }
})

watch(series, (newSeries) => {
  if (newSeries) pageCount.value = newSeries.totalPages ?? 0
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
