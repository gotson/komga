<template>
  <div v-if="collection">
    <toolbar-sticky v-if="!editElements && selectedSeries.length === 0">

      <collection-actions-menu v-if="collection"
                               :collection="collection"
      />

      <v-toolbar-title v-if="collection">
        <span>{{ collection.name }}</span>
        <v-chip label class="ml-4">
          <span style="font-size: 1.1rem">{{ collection.seriesIds.length }}</span>
        </v-chip>
        <span v-if="collection.ordered"
              class="font-italic text-overline"
        >(manual ordering)</span>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="startEditElements" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-edit</v-icon>
          </template>
          <span>Edit elements</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="editCollection" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>Edit collection</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="filterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
      </v-btn>

    </toolbar-sticky>

    <series-multi-select-bar
      v-model="selectedSeries"
      @unselect-all="selectedSeries = []"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @add-to-collection="addToCollection"
      @edit="editMultipleSeries"
    />

    <!--  Edit elements sticky bar  -->
    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="editElements" :elevation="5" color="base">
        <v-btn icon @click="cancelEditElements">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-btn icon color="primary" @click="doEditElements" :disabled="series.length === 0">
          <v-icon>mdi-check</v-icon>
        </v-btn>

      </toolbar-sticky>
    </v-scroll-y-transition>

    <filter-drawer v-model="drawer">
      <template v-slot:default>
        <filter-list
          :filters-options="filterOptionsList"
          :filters-active.sync="filters"
        />
      </template>

      <template v-slot:filter>
        <filter-panels
          :filters-options="filterOptionsPanel"
          :filters-active.sync="filters"
        />
      </template>
    </filter-drawer>

    <v-container fluid>
      <empty-state
        v-if="series.length === 0"
        title="The active filter has no matches"
        sub-title="Use the filter panel to change the active filter"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
      </empty-state>

      <item-browser
        v-else
        :items.sync="series"
        :selected.sync="selectedSeries"
        :edit-function="editSingleSeries"
        :draggable="editElements && collection.ordered"
        :deletable="editElements"
      />

    </v-container>

  </div>
</template>

<script lang="ts">
import CollectionActionsMenu from '@/components/menus/CollectionActionsMenu.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import { COLLECTION_CHANGED, COLLECTION_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'
import SeriesMultiSelectBar from '@/components/bars/SeriesMultiSelectBar.vue'
import { LIBRARIES_ALL } from '@/types/library'
import { ReadStatus } from '@/types/enum-books'
import { SeriesStatus, SeriesStatusKeyValue } from '@/types/enum-series'
import { mergeFilterParams, toNameValue } from '@/functions/filter'
import FilterDrawer from '@/components/FilterDrawer.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import { Location } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import { parseQueryFilter } from '@/functions/query-params'
import {SeriesDto} from "@/types/komga-series";

export default Vue.extend({
  name: 'BrowseCollection',
  components: {
    ToolbarSticky,
    ItemBrowser,
    CollectionActionsMenu,
    SeriesMultiSelectBar,
    FilterDrawer,
    FilterPanels,
    FilterList,
    EmptyState,
  },
  data: () => {
    return {
      collection: undefined as CollectionDto | undefined,
      series: [] as SeriesDto[],
      seriesCopy: [] as SeriesDto[],
      selectedSeries: [] as SeriesDto[],
      editElements: false,
      filterOptionsList: {
        readStatus: { values: [{ name: 'Unread', value: ReadStatus.UNREAD }] },
      } as FiltersOptions,
      filterOptionsPanel: {
        library: { name: 'LIBRARY', values: [] },
        status: { name: 'STATUS', values: SeriesStatusKeyValue },
        genre: { name: 'GENRE', values: [] },
        tag: { name: 'TAG', values: [] },
        publisher: { name: 'PUBLISHER', values: [] },
        language: { name: 'LANGUAGE', values: [] },
        ageRating: { name: 'AGE RATING', values: [] },
      } as FiltersOptions,
      filters: {} as FiltersActive,
      filterUnwatch: null as any,
      drawer: false,
    }
  },
  props: {
    collectionId: {
      type: String,
      required: true,
    },
  },
  watch: {
    selectedSeries (val: SeriesDto[]) {
      val.forEach(s => {
        let index = this.series.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.series.splice(index, 1, s)
        }
        index = this.seriesCopy.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.seriesCopy.splice(index, 1, s)
        }
      })
    },
  },
  created () {
    this.$eventHub.$on(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$on(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
  },
  beforeDestroy () {
    this.$eventHub.$off(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$off(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$off(SERIES_CHANGED, this.reloadSeries)
  },
  mounted () {
    this.loadCollection(this.collectionId)

    this.resetParams(this.$route)

    this.setWatches()
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.collectionId !== from.params.collectionId) {
      this.unsetWatches()

      // reset
      this.resetParams(this.$route)
      this.series = []
      this.editElements = false
      this.filterOptionsPanel.library.values = []
      this.filterOptionsPanel.genre.values = []
      this.filterOptionsPanel.tag.values = []
      this.filterOptionsPanel.publisher.values = []
      this.filterOptionsPanel.language.values = []
      this.filterOptionsPanel.ageRating.values = []

      this.loadCollection(to.params.collectionId)

      this.setWatches()
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    filterActive (): boolean {
      return Object.keys(this.filters).some(x => this.filters[x].length !== 0)
    },
  },
  methods: {
    resetParams (route: any) {
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating) {
        this.filters.status = parseQueryFilter(route.query.status, Object.keys(SeriesStatus))
        this.filters.readStatus = parseQueryFilter(route.query.readStatus, Object.keys(ReadStatus))
        this.filters.genre = parseQueryFilter(route.query.genre, this.filterOptionsPanel.genre.values.map(x => x.value))
        this.filters.tag = parseQueryFilter(route.query.tag, this.filterOptionsPanel.tag.values.map(x => x.value))
        this.filters.language = parseQueryFilter(route.query.language, this.filterOptionsPanel.language.values.map(x => x.value))
        this.filters.ageRating = parseQueryFilter(route.query.ageRating, this.filterOptionsPanel.ageRating.values.map(x => x.value))
      } else {
        this.filters = this.$cookies.get(this.cookieFilter(route.params.collectionId)) || {} as FiltersActive
      }
    },
    cookieFilter (collectionId: string): string {
      return `collection.filter.${collectionId}`
    },
    setWatches () {
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$cookies.set(this.cookieFilter(this.collectionId), val, Infinity)
        this.updateRouteAndReload()
      })
    },
    unsetWatches () {
      this.filterUnwatch()
    },
    collectionChanged (event: EventCollectionChanged) {
      if (event.id === this.collectionId) {
        this.loadCollection(this.collectionId)
      }
    },
    updateRouteAndReload () {
      this.unsetWatches()

      this.updateRoute()
      this.loadSeries(this.collectionId)

      this.setWatches()
    },
    async loadSeries (collectionId: string) {
      this.series = (await this.$komgaCollections.getSeries(collectionId, { unpaged: true } as PageRequest, this.filters.library, this.filters.status, this.filters.readStatus, this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating)).content
      this.seriesCopy = [...this.series]
      this.selectedSeries = []
    },
    async loadCollection (collectionId: string) {
      this.collection = await this.$komgaCollections.getOneCollection(collectionId)
      await this.loadSeries(collectionId)

      this.filterOptionsPanel.library.values = this.$store.state.komgaLibraries.libraries.map((x: LibraryDto) => ({
        name: x.name,
        value: x.id,
      }))
      this.filterOptionsPanel.genre.values = toNameValue(await this.$komgaReferential.getGenres(undefined, collectionId))
      this.filterOptionsPanel.tag.values = toNameValue(await this.$komgaReferential.getTags(undefined, undefined, collectionId))
      this.filterOptionsPanel.publisher.values = toNameValue(await this.$komgaReferential.getPublishers(undefined, collectionId))
      this.filterOptionsPanel.language.values = (await this.$komgaReferential.getLanguages(undefined, collectionId))
      this.filterOptionsPanel.ageRating.values = toNameValue(await this.$komgaReferential.getAgeRatings(undefined, collectionId))
    },
    updateRoute () {
      const loc = {
        name: this.$route.name,
        params: { collectionId: this.$route.params.collectionId },
        query: {},
      } as Location
      mergeFilterParams(this.filters, loc.query)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    editSingleSeries (series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    editMultipleSeries () {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    async markSelectedRead () {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsRead(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    async markSelectedUnread () {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsUnread(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    addToCollection () {
      this.$store.dispatch('dialogAddSeriesToCollection', this.selectedSeries)
    },
    startEditElements () {
      this.filters = {}
      this.editElements = true
    },
    cancelEditElements () {
      this.editElements = false
      this.series = [...this.seriesCopy]
    },
    doEditElements () {
      this.editElements = false
      const update = {
        seriesIds: this.series.map(x => x.id),
      } as CollectionUpdateDto
      this.$komgaCollections.patchCollection(this.collectionId, update)
      this.loadCollection(this.collectionId)
    },
    editCollection () {
      this.$store.dispatch('dialogEditCollection', this.collection)
    },
    afterDelete () {
      this.$router.push({ name: 'browse-collections', params: { libraryId: LIBRARIES_ALL } })
    },
    reloadSeries (event: EventSeriesChanged) {
      if (this.series.some(s => s.id === event.id)) this.loadCollection(this.collectionId)
    },
  },
})
</script>
