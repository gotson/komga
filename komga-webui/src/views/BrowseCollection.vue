<template>
  <div v-if="collection">
    <toolbar-sticky v-if="!editElements && selectedSeries.length === 0">

      <collection-actions-menu v-if="collection"
                               :collection="collection"
      />

      <v-toolbar-title v-if="collection">
        <span>{{ collection.name }}</span>
        <v-chip label class="mx-4">
          <span style="font-size: 1.1rem">{{ totalElements }}</span>
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

      <page-size-select v-model="pageSize"/>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="filterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
      </v-btn>

    </toolbar-sticky>

    <multi-select-bar
      v-model="selectedSeries"
      kind="series"
      :oneshots="selectedOneshots"
      show-select-all
      @unselect-all="selectedSeries = []"
      @select-all="selectedSeries = series"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @add-to-collection="addToCollection"
      @add-to-readlist="addToReadList"
      @edit="editMultipleSeries"
      @delete="deleteSeries"
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
        v-if="totalPages === 0"
        :title="$t('common.filter_no_matches')"
        :sub-title="$t('common.use_filter_panel_to_change_filter')"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
        <v-btn @click="resetFilters">{{ $t('common.reset_filters') }}</v-btn>
      </empty-state>

      <template v-else>
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />

        <item-browser
          :items.sync="series"
          :selected.sync="selectedSeries"
          :edit-function="isAdmin ? editSingleSeries : undefined"
          :draggable="editElements && collection.ordered"
          :deletable="editElements"
        />

        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />
      </template>

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
import {ReadStatus} from '@/types/enum-books'
import {SeriesStatus, SeriesStatusKeyValue} from '@/types/enum-series'
import {mergeFilterParams, toNameValue} from '@/functions/filter'
import FilterDrawer from '@/components/FilterDrawer.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {Location} from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import {Oneshot, SeriesDto} from '@/types/komga-series'
import {authorRoles} from '@/types/author-roles'
import {AuthorDto} from '@/types/komga-books'
import {CollectionSseDto, ReadProgressSeriesSseDto, SeriesSseDto} from '@/types/komga-sse'
import {throttle} from 'lodash'
import {LibraryDto} from '@/types/komga-libraries'
import {parseBooleanFilter} from '@/functions/query-params'
import {ContextOrigin} from '@/types/context'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import {BookSearch, SearchConditionAnyOfBook, SearchConditionSeriesId, SearchOperatorIs} from '@/types/komga-search'
import {FiltersActive, FiltersOptions, NameValue} from '@/types/filter'

export default Vue.extend({
  name: 'BrowseCollection',
  components: {
    PageSizeSelect,
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
      page: 1,
      pageSize: 20,
      unpaged: false,
      totalPages: 1,
      totalElements: null as number | null,
      editElements: false,
      filters: {} as FiltersActive,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
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
    this.pageSize = this.$store.state.persistedState.browsingPageSize || this.pageSize

    // restore from query param
    await this.resetParams(this.$route, this.collectionId)
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

    this.loadCollection(this.collectionId)

    this.setWatches()
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.collectionId !== from.params.collectionId) {
      this.unsetWatches()

      // reset
      await this.resetParams(this.$route, to.params.collectionId)
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.series = []
      this.editElements = false

      this.loadCollection(to.params.collectionId)

      this.setWatches()
    }

    next()
  },
  computed: {
    paginationVisible(): number {
      switch (this.$vuetify.breakpoint.name) {
        case 'xs':
          return 5
        case 'sm':
        case 'md':
          return 10
        case 'lg':
        case 'xl':
        default:
          return 15
      }
    },
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {name: this.$i18n.t('filter.unread').toString(), value: ReadStatus.UNREAD},
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
    selectedOneshots(): boolean {
      return this.selectedSeries.every(s => s.oneshot)
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
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$store.commit('setBrowsingPageSize', val)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.collectionId, val)
      })
    },
    unsetWatches() {
      this.filterUnwatch()
      this.pageUnwatch()
      this.pageSizeUnwatch()
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

      this.page = 1

      this.updateRoute()
      this.loadPage(this.collectionId, this.page)

      this.setWatches()
    },
    async loadPage(collectionId: string, page: number) {
      this.selectedSeries = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
        unpaged: this.unpaged,
      } as PageRequest

      let authorsFilter = [] as AuthorDto[]
      authorRoles.forEach((role: string) => {
        if (role in this.filters) this.filters[role].forEach((name: string) => authorsFilter.push({
          name: name,
          role: role,
        }))
      })

      const complete = parseBooleanFilter(this.filters.complete)
      const seriesPage = await this.$komgaCollections.getSeries(collectionId, pageRequest, this.filters.library, this.filters.status, this.filters.readStatus, this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter, complete)

      this.totalPages = seriesPage.totalPages
      this.totalElements = seriesPage.totalElements
      this.series = seriesPage.content

      this.series.forEach((x: SeriesDto) => x.context = {origin: ContextOrigin.COLLECTION, id: collectionId})
      this.seriesCopy = [...this.series]
      this.selectedSeries = []
    },
    reloadPage: throttle(function (this: any) {
      this.loadPage(this.collectionId, this.page)
    }, 1000),
    async loadCollection(collectionId: string) {
      this.$komgaCollections.getOneCollection(collectionId)
        .then(v => {
          this.collection = v
          document.title = `Komga - ${v.name}`
        })

      await this.loadPage(collectionId, this.page)
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {collectionId: this.$route.params.collectionId},
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
        },
      } as Location
      mergeFilterParams(this.filters, loc.query)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    async editSingleSeries(series: SeriesDto) {
      if (series.oneshot) {
        const book = (await this.$komgaBooks.getBooksList({
          condition: new SearchConditionSeriesId(new SearchOperatorIs(series.id)),
        } as BookSearch)).content[0]
        this.$store.dispatch('dialogUpdateOneshots', {series: series, book: book})
      } else
        this.$store.dispatch('dialogUpdateSeries', series)
    },
    async editMultipleSeries() {
      if (this.selectedSeries.every(s => s.oneshot)) {
        const books = await Promise.all(this.selectedSeries.map(s => this.$komgaBooks.getBooksList({
          condition: new SearchConditionSeriesId(new SearchOperatorIs(s.id)),
        } as BookSearch)))
        const oneshots = this.selectedSeries.map((s, index) => ({series: s, book: books[index].content[0]} as Oneshot))
        this.$store.dispatch('dialogUpdateOneshots', oneshots)
      } else
        this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    deleteSeries() {
      this.$store.dispatch('dialogDeleteSeries', this.selectedSeries)
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
      this.$store.dispatch('dialogAddSeriesToCollection', this.selectedSeries.map(s => s.id))
    },
    async addToReadList() {
      const conditions = this.selectedSeries.map(s => new SearchConditionSeriesId(new SearchOperatorIs(s.id)))
      const books = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionAnyOfBook(conditions),
      } as BookSearch, {unpaged: true})
      this.$store.dispatch('dialogAddBooksToReadList', books.content.map(b => b.id))
    },
    async startEditElements() {
      this.filters = {}
      this.unpaged = true
      await this.reloadPage()
      this.editElements = true
    },
    cancelEditElements() {
      this.editElements = false
      this.series = [...this.seriesCopy]
      this.unpaged = false
      this.reloadPage()
    },
    doEditElements() {
      this.editElements = false
      const update = {
        seriesIds: this.series.map(x => x.id),
      } as CollectionUpdateDto
      this.$komgaCollections.patchCollection(this.collectionId, update)
      this.unpaged = false
      this.reloadPage()
    },
    editCollection() {
      this.$store.dispatch('dialogEditCollection', this.collection)
    },
    seriesChanged(event: SeriesSseDto) {
      if (this.series.some(s => s.id === event.seriesId)) this.reloadPage()
    },
    readProgressChanged(event: ReadProgressSeriesSseDto) {
      if (this.series.some(b => b.id === event.seriesId)) this.reloadPage()
    },
  },
})
</script>
