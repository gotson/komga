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

    <multi-select-bar
      v-model="selectedSeries"
      kind="series"
      show-select-all
      @unselect-all="selectedSeries = []"
      @select-all="selectedSeries = series"
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
        :edit-function="isAdmin ? editSingleSeries : undefined"
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
import {
  COLLECTION_CHANGED,
  COLLECTION_DELETED,
  READPROGRESS_SERIES_CHANGED,
  READPROGRESS_SERIES_DELETED,
  SERIES_CHANGED,
  SERIES_DELETED,
} from '@/types/events'
import Vue from 'vue'
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import {LIBRARIES_ALL} from '@/types/library'
import {ReadStatus, replaceCompositeReadStatus} from '@/types/enum-books'
import {SeriesStatus, SeriesStatusKeyValue} from '@/types/enum-series'
import {mergeFilterParams, toNameValue} from '@/functions/filter'
import FilterDrawer from '@/components/FilterDrawer.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {Location} from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import {SeriesDto} from '@/types/komga-series'
import {authorRoles} from '@/types/author-roles'
import {AuthorDto} from '@/types/komga-books'
import {CollectionSseDto, ReadProgressSeriesSseDto, SeriesSseDto} from '@/types/komga-sse'
import {throttle} from 'lodash'
import {LibraryDto} from '@/types/komga-libraries'
import {parseBooleanFilter} from '@/functions/query-params'

export default Vue.extend({
  name: 'BrowseCollection',
  components: {
    ToolbarSticky,
    ItemBrowser,
    CollectionActionsMenu,
    MultiSelectBar,
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
  created() {
    this.$eventHub.$on(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$on(COLLECTION_DELETED, this.collectionDeleted)
    this.$eventHub.$on(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$on(SERIES_DELETED, this.seriesChanged)
    this.$eventHub.$on(READPROGRESS_SERIES_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_SERIES_DELETED, this.readProgressChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(COLLECTION_CHANGED, this.collectionChanged)
    this.$eventHub.$off(COLLECTION_DELETED, this.collectionDeleted)
    this.$eventHub.$off(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$off(SERIES_DELETED, this.seriesChanged)
    this.$eventHub.$off(READPROGRESS_SERIES_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_SERIES_DELETED, this.readProgressChanged)
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
        complete: {
          values: [{name: this.$t('filter.complete').toString(), value: 'true', nValue: 'false'}],
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
        r[role] = {
          name: this.$t(`author_roles.${role}`).toString(),
          search: async search => {
            return (await this.$komgaReferential.getAuthors(search, role, undefined, this.collectionId))
              .content
              .map(x => x.name)
          },
        }
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

      const [genres, tags, publishers, languages, ageRatings, releaseDates] = await Promise.all([
        this.$komgaReferential.getGenres(undefined, collectionId),
        this.$komgaReferential.getSeriesAndBookTags(undefined, collectionId),
        this.$komgaReferential.getPublishers(undefined, collectionId),
        this.$komgaReferential.getLanguages(undefined, collectionId),
        this.$komgaReferential.getAgeRatings(undefined, collectionId),
        this.$komgaReferential.getSeriesReleaseDates(undefined, collectionId),
      ])
      this.$set(this.filterOptions, 'genre', toNameValue(genres))
      this.$set(this.filterOptions, 'tag', toNameValue(tags))
      this.$set(this.filterOptions, 'publisher', toNameValue(publishers))
      this.$set(this.filterOptions, 'language', (languages))
      this.$set(this.filterOptions, 'ageRating', toNameValue(ageRatings))
      this.$set(this.filterOptions, 'releaseDate', toNameValue(releaseDates))

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating || route.query.library || route.query.publisher || authorRoles.some(role => role in route.query) || route.query.complete) {
        activeFilters = {
          status: route.query.status || [],
          readStatus: route.query.readStatus || [],
          library: route.query.library || [],
          genre: route.query.genre || [],
          tag: route.query.tag || [],
          publisher: route.query.publisher || [],
          language: route.query.language || [],
          ageRating: route.query.ageRating || [],
          releaseDate: route.query.releaseDate || [],
          complete: route.query.complete || [],
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = route.query[role] || []
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
        complete: filters.complete?.filter(x => x === 'true' || x === 'false') || [],
      } as any
      authorRoles.forEach((role: string) => {
        validFilter[role] = filters[role] || []
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
    collectionChanged(event: CollectionSseDto) {
      if (event.collectionId === this.collectionId) {
        this.loadCollection(this.collectionId)
      }
    },
    collectionDeleted(event: CollectionSseDto) {
      if (event.collectionId === this.collectionId) {
        this.$router.push({name: 'browse-collections', params: {libraryId: LIBRARIES_ALL}})
      }
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.updateRoute()
      this.loadSeries(this.collectionId)

      this.setWatches()
    },
    reloadSeries: throttle(function (this: any) {
      this.loadSeries(this.collectionId)
    }, 1000),
    async loadSeries(collectionId: string) {
      let authorsFilter = [] as AuthorDto[]
      authorRoles.forEach((role: string) => {
        if (role in this.filters) this.filters[role].forEach((name: string) => authorsFilter.push({
          name: name,
          role: role,
        }))
      })

      const complete = parseBooleanFilter(this.filters.complete)
      this.series = (await this.$komgaCollections.getSeries(collectionId, {unpaged: true} as PageRequest, this.filters.library, this.filters.status, replaceCompositeReadStatus(this.filters.readStatus), this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter, complete)).content
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
      this.selectedSeries = []
    },
    async markSelectedUnread() {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsUnread(s.id),
      ))
      this.selectedSeries = []
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
    },
    editCollection() {
      this.$store.dispatch('dialogEditCollection', this.collection)
    },
    seriesChanged(event: SeriesSseDto) {
      if (this.series.some(s => s.id === event.seriesId)) this.reloadSeries()
    },
    readProgressChanged(event: ReadProgressSeriesSseDto) {
      if (this.series.some(b => b.id === event.seriesId)) this.reloadSeries()
    },
  },
})
</script>
