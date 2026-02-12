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

    <v-icon-btn
      icon="i-mdi:filter-variant"
      @click="filterDrawer = true"
    />
  </v-app-bar>

  <!-- TODO: Move into its own component -->
  <!--  Teleport is needed so that the scrim covers the whole screen  -->
  <Teleport to="#app">
    <!--  order=-1 is needed for the drawer to open full height  -->
    <!--  disable-route-watcher is needed, else the drawer closes when the route query params are updated when the filters change -->
    <v-navigation-drawer
      v-model="filterDrawer"
      location="end"
      temporary
      order="-1"
      disable-route-watcher
    >
      <v-list>
        <v-expansion-panels
          v-model="filterExpansionPanels"
          variant="accordion"
          class="no-padding"
          flat
          tile
        >
          <FilterExpansionPanel
            title="Status"
            :count="filterSeriesStatus.v.length"
            @clear="clearFilter(filterSeriesStatus)"
          >
            <FilterBySeriesStatus v-model="filterSeriesStatus.v" />
          </FilterExpansionPanel>
        </v-expansion-panels>

        <v-divider />

        <v-list-subheader>CREATORS</v-list-subheader>

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
    </v-navigation-drawer>
  </Teleport>

  <div>FILTER AUTHORS</div>
  <p>{{ filterAuthors }}</p>
  <div>AUTHOR ROLES</div>
  <p>{{ authorRoles }}</p>
  <div>FILTER</div>
  <div>{{ filterSeriesStatus }}</div>
  <div>CONDITION</div>
  <div>{{ conds }}</div>

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
import {
  schemaFilterAuthorsToConditions,
  schemaFilterSeriesStatusToConditions,
} from '@/functions/filter'
import * as v from 'valibot'
import { type FilterType, SchemaFilterAuthors, SchemaFilterSeriesStatus } from '@/types/filter'
import { useRouteQuerySchema } from '@/composables/useRouteQuerySchema'
import { authorRoles } from '@/types/referential'
import { useIntl } from 'vue-intl'

const route = useRoute('/libraries/[id]/series')
const libraryId = route.params.id
const { libraries } = useGetLibrariesById(libraryId)
const { librariesCondition } = useSearchConditionLibraries(libraries)

const intl = useIntl()
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

const { data: filterSeriesStatus } = useRouteQuerySchema('status', SchemaFilterSeriesStatus)

const conds = computed(() => ({
  allOf: [
    librariesCondition.value as components['schemas']['AnyOfSeries'],
    schemaFilterSeriesStatusToConditions(filterSeriesStatus.value),
    ...Object.entries(filterAuthors).map(([, filter]) =>
      schemaFilterAuthorsToConditions(toValue(filter.filter), toValue(filter.role)),
    ),
  ],
}))

const { data: series } = useQuery(seriesListQuery, () => {
  const search: components['schemas']['SeriesSearch'] = {
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
