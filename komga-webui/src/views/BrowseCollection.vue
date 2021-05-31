<template>
  <div v-if="collection">
    <toolbar-sticky v-if="!editElements && selectedSeries.length === 0">

      <collection-actions-menu v-if="collection"
                               :collection="collection"
      />

      <v-toolbar-title v-if="collection">
        <span>{{ collection.name }}</span>
        <v-chip label class="mx-4">
          <span style="font-size: 1.1rem">{{ collection.seriesIds.length }}</span>
        </v-chip>
        <span v-if="collection.ordered"
              class="font-italic text-overline"
        >({{ $t('browse_collection.manual_ordering') }})</span>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="startEditElements" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-edit</v-icon>
          </template>
          <span>{{ $t('browse_collection.edit_elements') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="editCollection" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>{{ $t('browse_collection.edit_collection') }}</span>
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

    <filter-drawer
      v-model="drawer"
      :clear-button="filterActive"
      @clear="resetFilters"
    >
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
        :title="$t('common.filter_no_matches')"
        :sub-title="$t('common.use_filter_panel_to_change_filter')"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
        <v-btn @click="resetFilters">{{ $t('common.reset_filters') }}</v-btn>
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
import {COLLECTION_CHANGED, COLLECTION_DELETED, SERIES_CHANGED} from '@/types/events'
import Vue from 'vue'
import SeriesMultiSelectBar from '@/components/bars/SeriesMultiSelectBar.vue'
import {LIBRARIES_ALL} from '@/types/library'
import {ReadStatus, replaceCompositeReadStatus} from '@/types/enum-books'
import {SeriesStatus, SeriesStatusKeyValue} from '@/types/enum-series'
import {mergeFilterParams, toNameValue} from '@/functions/filter'
import FilterDrawer from '@/components/FilterDrawer.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {Location} from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import {parseQueryParam} from '@/functions/query-params'
import {SeriesDto} from "@/types/komga-series";
import {authorRoles} from "@/types/author-roles";
import {groupAuthorsByRole} from "@/functions/authors";
import {AuthorDto} from "@/types/komga-books";

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
  data: function () {
    return {
      collection: undefined as CollectionDto | undefined,
      series: [] as SeriesDto[],
      seriesCopy: [] as SeriesDto[],
      selectedSeries: [] as SeriesDto[],
      editElements: false,
      filters: {} as FiltersActive,
      filterUnwatch: null as any,
      drawer: false,
      filterOptions: {
        library: [] as NameValue[],
        genre: [] as NameValue[],
        tag: [] as NameValue[],
        publisher: [] as NameValue[],
        language: [] as NameValue[],
        ageRating: [] as NameValue[],
        releaseDate: [] as NameValue[],
      },
    }
  },
  props: {
    collectionId: {
      type: String,
      required: true,
    },
  },
  watch: {
    selectedSeries(val: SeriesDto[]) {
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
  created() {
    this.$eventHub.$on(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$on(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
  },
  beforeDestroy() {
    this.$eventHub.$off(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$off(COLLECTION_DELETED, this.afterDelete)
    this.$eventHub.$off(SERIES_CHANGED, this.reloadSeries)
  },
  async mounted() {
    await this.resetParams(this.$route, this.collectionId)

    this.loadCollection(this.collectionId)

    this.setWatches()
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.collectionId !== from.params.collectionId) {
      this.unsetWatches()

      // reset
      await this.resetParams(this.$route, to.params.collectionId)
      this.series = []
      this.editElements = false

      this.loadCollection(to.params.collectionId)

      this.setWatches()
    }

    next()
  },
  computed: {
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {name: this.$i18n.t('filter.unread').toString(), value: ReadStatus.UNREAD_AND_IN_PROGRESS},
            {name: this.$t('filter.in_progress').toString(), value: ReadStatus.IN_PROGRESS},
            {name: this.$t('filter.read').toString(), value: ReadStatus.READ},
          ],
        },
      } as FiltersOptions
    },
    filterOptionsPanel(): FiltersOptions {
      const r = {
        library: {name: this.$t('filter.library').toString(), values: this.filterOptions.library},
        status: {name: this.$t('filter.status').toString(), values: SeriesStatusKeyValue()},
        genre: {name: this.$t('filter.genre').toString(), values: this.filterOptions.genre},
        tag: {name: this.$t('filter.tag').toString(), values: this.filterOptions.tag},
        publisher: {name: this.$t('filter.publisher').toString(), values: this.filterOptions.publisher},
        language: {name: this.$t('filter.language').toString(), values: this.filterOptions.language},
        ageRating: {
          name: this.$t('filter.age_rating').toString(),
          values: this.filterOptions.ageRating.map((x: NameValue) => ({
              name: (x.value === 'None' ? this.$t('filter.age_rating_none').toString() : x.name),
              value: x.value,
            } as NameValue),
          ),
        },
        releaseDate: {name: this.$t('filter.release_date').toString(), values: this.filterOptions.releaseDate},
      } as FiltersOptions
      authorRoles.forEach((role: string) => {
        //@ts-ignore
        r[role] = {name: this.$t(`author_roles.${role}`).toString(), values: this.$_.get(this.filterOptions, role, [])}
      })
      return r
    },
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    filterActive(): boolean {
      return Object.keys(this.filters).some(x => this.filters[x].length !== 0)
    },
  },
  methods: {
    resetFilters() {
      this.drawer = false
      for (const prop in this.filters) {
        this.$set(this.filters, prop, [])
      }
      this.$store.commit('setCollectionFilter', {id: this.collectionId, filter: this.filters})
      this.updateRouteAndReload()
    },
    async resetParams(route: any, collectionId: string) {
      // load dynamic filters
      this.$set(this.filterOptions, 'library', this.$store.state.komgaLibraries.libraries.map((x: LibraryDto) => ({
        name: x.name,
        value: x.id,
      })))
      this.$set(this.filterOptions, 'genre', toNameValue(await this.$komgaReferential.getGenres(undefined, collectionId)))
      this.$set(this.filterOptions, 'tag', toNameValue(await this.$komgaReferential.getSeriesTags(undefined, collectionId)))
      this.$set(this.filterOptions, 'publisher', toNameValue(await this.$komgaReferential.getPublishers(undefined, collectionId)))
      this.$set(this.filterOptions, 'language', (await this.$komgaReferential.getLanguages(undefined, collectionId)))
      this.$set(this.filterOptions, 'ageRating', toNameValue(await this.$komgaReferential.getAgeRatings(undefined, collectionId)))
      this.$set(this.filterOptions, 'releaseDate', toNameValue(await this.$komgaReferential.getSeriesReleaseDates(undefined, collectionId)))
      const grouped = groupAuthorsByRole(await this.$komgaReferential.getAuthors(undefined, undefined, collectionId))
      authorRoles.forEach((role: string) => {
        this.$set(this.filterOptions, role, role in grouped ? toNameValue(grouped[role]) : [])
      })

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating || route.query.library || route.query.publisher || authorRoles.some(role => role in route.query)) {
        activeFilters = {
          status: parseQueryParam(route.query.status),
          readStatus: parseQueryParam(route.query.readStatus),
          library: parseQueryParam(route.query.library),
          genre: parseQueryParam(route.query.genre),
          tag: parseQueryParam(route.query.tag),
          publisher: parseQueryParam(route.query.publisher),
          language: parseQueryParam(route.query.language),
          ageRating: parseQueryParam(route.query.ageRating),
          releaseDate: parseQueryParam(route.query.releaseDate),
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = parseQueryParam(route.query[role])
        })
      } else {
        activeFilters = this.$store.getters.getCollectionFilter(route.params.collectionId) || {} as FiltersActive
      }
      this.filters = this.validateFilters(activeFilters)
    },
    validateFilters(filters: FiltersActive): FiltersActive {
      const validFilter = {
        status: filters.status?.filter(x => Object.keys(SeriesStatus).includes(x)) || [],
        readStatus: filters.readStatus?.filter(x => Object.keys(ReadStatus).includes(x)) || [],
        library: filters.library?.filter(x => this.filterOptions.library.map(n => n.value).includes(x)) || [],
        genre: filters.genre?.filter(x => this.filterOptions.genre.map(n => n.value).includes(x)) || [],
        tag: filters.tag?.filter(x => this.filterOptions.tag.map(n => n.value).includes(x)) || [],
        publisher: filters.publisher?.filter(x => this.filterOptions.publisher.map(n => n.value).includes(x)) || [],
        language: filters.language?.filter(x => this.filterOptions.language.map(n => n.value).includes(x)) || [],
        ageRating: filters.ageRating?.filter(x => this.filterOptions.ageRating.map(n => n.value).includes(x)) || [],
        releaseDate: filters.releaseDate?.filter(x => this.filterOptions.releaseDate.map(n => n.value).includes(x)) || [],
      } as any
      authorRoles.forEach((role: string) => {
        validFilter[role] = filters[role]?.filter(x => ((this.filterOptions as any)[role] as NameValue[]).map(n => n.value).includes(x)) || []
      })
      return validFilter
    },
    setWatches() {
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$store.commit('setCollectionFilter', {id: this.collectionId, filter: val})
        this.updateRouteAndReload()
      })
    },
    unsetWatches() {
      this.filterUnwatch()
    },
    collectionChanged(event: EventCollectionChanged) {
      if (event.id === this.collectionId) {
        this.loadCollection(this.collectionId)
      }
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.updateRoute()
      this.loadSeries(this.collectionId)

      this.setWatches()
    },
    async loadSeries(collectionId: string) {
      let authorsFilter = [] as AuthorDto[]
      authorRoles.forEach((role: string) => {
        if (role in this.filters) this.filters[role].forEach((name: string) => authorsFilter.push({
          name: name,
          role: role,
        }))
      })

      this.series = (await this.$komgaCollections.getSeries(collectionId, {unpaged: true} as PageRequest, this.filters.library, this.filters.status, replaceCompositeReadStatus(this.filters.readStatus), this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter)).content
      this.seriesCopy = [...this.series]
      this.selectedSeries = []
    },
    async loadCollection(collectionId: string) {
      this.$komgaCollections.getOneCollection(collectionId)
        .then(v => this.collection = v)

      await this.loadSeries(collectionId)
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {collectionId: this.$route.params.collectionId},
        query: {},
      } as Location
      mergeFilterParams(this.filters, loc.query)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    editSingleSeries(series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    editMultipleSeries() {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    async markSelectedRead() {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsRead(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    async markSelectedUnread() {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsUnread(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    addToCollection() {
      this.$store.dispatch('dialogAddSeriesToCollection', this.selectedSeries)
    },
    startEditElements() {
      this.filters = {}
      this.editElements = true
    },
    cancelEditElements() {
      this.editElements = false
      this.series = [...this.seriesCopy]
    },
    doEditElements() {
      this.editElements = false
      const update = {
        seriesIds: this.series.map(x => x.id),
      } as CollectionUpdateDto
      this.$komgaCollections.patchCollection(this.collectionId, update)
      this.loadCollection(this.collectionId)
    },
    editCollection() {
      this.$store.dispatch('dialogEditCollection', this.collection)
    },
    afterDelete() {
      this.$router.push({name: 'browse-collections', params: {libraryId: LIBRARIES_ALL}})
    },
    reloadSeries(event: EventSeriesChanged) {
      if (this.series.some(s => s.id === event.id)) this.loadCollection(this.collectionId)
    },
  },
})
</script>
