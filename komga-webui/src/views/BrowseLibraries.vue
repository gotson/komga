<template>
  <div :style="$vuetify.breakpoint.xs ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="selectedSeries.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : $t('common.all_libraries') }}</span>
        <v-chip label class="mx-4" v-if="totalElements">
          <span style="font-size: 1.1rem">{{ totalElements }}</span>
        </v-chip>
      </v-toolbar-title>

      <v-spacer/>

      <library-navigation v-if="$vuetify.breakpoint.mdAndUp" :libraryId="libraryId"/>

      <v-spacer/>

      <page-size-select v-model="pageSize"/>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="sortOrFilterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
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
      @delete="deleteSeries"
    />

    <library-navigation v-if="$vuetify.breakpoint.smAndDown" :libraryId="libraryId" bottom-navigation/>

    <filter-drawer
      v-model="drawer"
      :clear-button="sortOrFilterActive"
      @clear="resetSortAndFilters"
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

      <template v-slot:sort>
        <sort-list
          :sort-default="sortDefault"
          :sort-options="sortOptions"
          :sort-active.sync="sortActive"
        />
      </template>
    </filter-drawer>

    <v-container fluid>
      <alphabetical-navigation
        class="text-center"
        :symbols="alphabeticalNavigation"
        :selected="selectedSymbol"
        :group-count="seriesGroups"
        @clicked="filterByStarting"
      />

      <empty-state
        v-if="totalPages === 0 && sortOrFilterActive"
        :title="$t('common.filter_no_matches')"
        :sub-title="$t('common.use_filter_panel_to_change_filter')"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
        <v-btn @click="resetSortAndFilters">{{ $t('common.reset_filters') }}</v-btn>
      </empty-state>

      <empty-state
        v-if="totalPages === 0 && !sortOrFilterActive"
        :title="$t('common.nothing_to_show')"
        icon="mdi-help-circle"
        icon-color="secondary"
      />

      <template v-if="totalPages > 0">
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />

        <item-browser
          :items="series"
          :item-context="itemContext"
          :selected.sync="selectedSeries"
          :edit-function="isAdmin ? editSingleSeries : undefined"
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
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import EmptyState from '@/components/EmptyState.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import LibraryNavigation from '@/components/LibraryNavigation.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import {parseBooleanFilter, parseQuerySort} from '@/functions/query-params'
import {ReadStatus, replaceCompositeReadStatus} from '@/types/enum-books'
import {SeriesStatus, SeriesStatusKeyValue} from '@/types/enum-series'
import {
  LIBRARY_CHANGED,
  LIBRARY_DELETED,
  READPROGRESS_SERIES_CHANGED,
  READPROGRESS_SERIES_DELETED,
  SERIES_ADDED,
  SERIES_CHANGED,
  SERIES_DELETED,
} from '@/types/events'
import Vue from 'vue'
import {Location} from 'vue-router'
import {LIBRARIES_ALL, LIBRARY_ROUTE} from '@/types/library'
import FilterDrawer from '@/components/FilterDrawer.vue'
import SortList from '@/components/SortList.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {mergeFilterParams, sortOrFilterActive, toNameValue} from '@/functions/filter'
import {GroupCountDto, SeriesDto} from '@/types/komga-series'
import {AuthorDto} from '@/types/komga-books'
import {authorRoles} from '@/types/author-roles'
import {LibrarySseDto, ReadProgressSeriesSseDto, SeriesSseDto} from '@/types/komga-sse'
import {throttle} from 'lodash'
import AlphabeticalNavigation from '@/components/AlphabeticalNavigation.vue'
import {LibraryDto} from '@/types/komga-libraries'
import {ItemContext} from '@/types/items'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: {
    AlphabeticalNavigation,
    LibraryActionsMenu,
    EmptyState,
    ToolbarSticky,
    ItemBrowser,
    PageSizeSelect,
    LibraryNavigation,
    MultiSelectBar,
    FilterDrawer,
    FilterPanels,
    FilterList,
    SortList,
  },
  data: function () {
    return {
      library: undefined as LibraryDto | undefined,
      series: [] as SeriesDto[],
      seriesGroups: [] as GroupCountDto[],
      alphabeticalNavigation: ['ALL', '#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'],
      selectedSymbol: 'ALL',
      selectedSeries: [] as SeriesDto[],
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      sortActive: {} as SortActive,
      sortDefault: {key: 'metadata.titleSort', order: 'asc'} as SortActive,
      filters: {} as FiltersActive,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      drawer: false,
      filterOptions: {
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
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  created() {
    this.$eventHub.$on(SERIES_ADDED, this.seriesChanged)
    this.$eventHub.$on(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$on(SERIES_DELETED, this.seriesChanged)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(LIBRARY_CHANGED, this.libraryChanged)
    this.$eventHub.$on(READPROGRESS_SERIES_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_SERIES_DELETED, this.readProgressChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(SERIES_ADDED, this.seriesChanged)
    this.$eventHub.$off(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$off(SERIES_DELETED, this.seriesChanged)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(LIBRARY_CHANGED, this.libraryChanged)
    this.$eventHub.$off(READPROGRESS_SERIES_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_SERIES_DELETED, this.readProgressChanged)
  },
  async mounted() {
    this.$store.commit('setLibraryRoute', {id: this.libraryId, route: LIBRARY_ROUTE.BROWSE})
    this.pageSize = this.$store.state.persistedState.browsingPageSize || this.pageSize

    // restore from query param
    await this.resetParams(this.$route, this.libraryId)
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)
    if (this.$route.query.nav) this.selectedSymbol = this.$route.query.nav.toString()

    this.loadLibrary(this.libraryId)

    this.setWatches()
  },
  async beforeRouteUpdate(to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.unsetWatches()

      // reset
      await this.resetParams(to, to.params.libraryId)
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.series = []
      this.seriesGroups = []
      this.selectedSymbol = 'ALL'

      this.loadLibrary(to.params.libraryId)

      this.setWatches()
    }

    next()
  },
  computed: {
    searchRegex(): string | undefined {
        if (this.selectedSymbol === 'ALL') return undefined
        if (this.selectedSymbol === '#') return '^[^a-z],title_sort'
        return `^${this.selectedSymbol},title_sort`
    },
    itemContext(): ItemContext[] {
      if (this.sortActive.key === 'booksMetadata.releaseDate') return [ItemContext.RELEASE_DATE]
      if (this.sortActive.key === 'createdDate') return [ItemContext.DATE_ADDED]
      if (this.sortActive.key === 'lastModifiedDate') return [ItemContext.DATE_UPDATED]
      return []
    },
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('sort.name').toString(), key: 'metadata.titleSort'},
        {name: this.$t('sort.date_added').toString(), key: 'createdDate'},
        {name: this.$t('sort.date_updated').toString(), key: 'lastModifiedDate'},
        {name: this.$t('sort.release_date').toString(), key: 'booksMetadata.releaseDate'},
        {name: this.$t('sort.folder_name').toString(), key: 'name'},
        {name: this.$t('sort.books_count').toString(), key: 'booksCount'},
      ] as SortOption[]
    },
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {name: this.$t('filter.unread').toString(), value: ReadStatus.UNREAD_AND_IN_PROGRESS},
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
            return (await this.$komgaReferential.getAuthors(search, role, this.libraryId !== LIBRARIES_ALL ? this.libraryId : undefined))
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
    sortOrFilterActive(): boolean {
      return sortOrFilterActive(this.sortActive, this.sortDefault, this.filters)
    },
  },
  methods: {
    filterByStarting(symbol: string) {
      this.selectedSymbol = symbol
      this.page = 1
      this.updateRoute()
      this.loadPage(this.libraryId, 1, this.sortActive, this.searchRegex)
    },
    resetSortAndFilters() {
      this.drawer = false
      for (const prop in this.filters) {
        this.$set(this.filters, prop, [])
      }
      this.sortActive = this.sortDefault
      this.$store.commit('setLibraryFilter', {id: this.libraryId, filter: this.filters})
      this.$store.commit('setLibrarySort', {id: this.libraryId, sort: this.sortActive})
      this.updateRouteAndReload()
    },
    async resetParams(route: any, libraryId: string) {
      this.sortActive = parseQuerySort(route.query.sort, this.sortOptions) ||
        this.$store.getters.getLibrarySort(route.params.libraryId) ||
        this.$_.clone(this.sortDefault)

      const requestLibraryId = libraryId !== LIBRARIES_ALL ? libraryId : undefined

      // load dynamic filters
      const [genres, tags, publishers, languages, ageRatings, releaseDates] = await Promise.all([
        this.$komgaReferential.getGenres(requestLibraryId),
        this.$komgaReferential.getSeriesAndBookTags(requestLibraryId),
        this.$komgaReferential.getPublishers(requestLibraryId),
        this.$komgaReferential.getLanguages(requestLibraryId),
        this.$komgaReferential.getAgeRatings(requestLibraryId),
        this.$komgaReferential.getSeriesReleaseDates(requestLibraryId),
      ])
      this.$set(this.filterOptions, 'genre', toNameValue(genres))
      this.$set(this.filterOptions, 'tag', toNameValue(tags))
      this.$set(this.filterOptions, 'publisher', toNameValue(publishers))
      this.$set(this.filterOptions, 'language', (languages))
      this.$set(this.filterOptions, 'ageRating', toNameValue(ageRatings))
      this.$set(this.filterOptions, 'releaseDate', toNameValue(releaseDates))

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating || route.query.publisher || authorRoles.some(role => role in route.query) || route.query.complete) {
        activeFilters = {
          status: route.query.status || [],
          readStatus: route.query.readStatus || [],
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
        activeFilters = this.$store.getters.getLibraryFilter(route.params.libraryId) || {} as FiltersActive
      }
      this.filters = this.validateFilters(activeFilters)
    },
    validateFilters(filters: FiltersActive): FiltersActive {
      const validFilter = {
        status: filters.status?.filter(x => Object.keys(SeriesStatus).includes(x)) || [],
        readStatus: filters.readStatus?.filter(x => Object.keys(ReadStatus).includes(x)) || [],
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
    libraryDeleted(event: LibrarySseDto) {
      if (event.libraryId === this.libraryId) {
        this.$router.push({name: 'home'})
      } else if (this.libraryId === LIBRARIES_ALL) {
        this.loadLibrary(this.libraryId)
      }
    },
    setWatches() {
      this.sortUnwatch = this.$watch('sortActive', (val) => {
        this.$store.commit('setLibrarySort', {id: this.libraryId, sort: val})
        this.updateRouteAndReload()
      })
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$store.commit('setLibraryFilter', {id: this.libraryId, filter: val})
        this.updateRouteAndReload()
      })
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$store.commit('setBrowsingPageSize', val)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.libraryId, val, this.sortActive, this.searchRegex)
      })
    },
    unsetWatches() {
      this.sortUnwatch()
      this.filterUnwatch()
      this.pageUnwatch()
      this.pageSizeUnwatch()
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.page = 1

      this.updateRoute()
      this.loadPage(this.libraryId, this.page, this.sortActive, this.searchRegex)

      this.setWatches()
    },
    seriesChanged(event: SeriesSseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.reloadPage()
      }
    },
    libraryChanged(event: LibrarySseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.loadLibrary(this.libraryId)
      }
    },
    readProgressChanged(event: ReadProgressSeriesSseDto) {
      if (this.series.some(b => b.id === event.seriesId)) this.reloadPage()
    },
    async loadLibrary(libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)

      await this.loadPage(libraryId, this.page, this.sortActive, this.searchRegex)
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {libraryId: this.$route.params.libraryId},
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
          sort: `${this.sortActive.key},${this.sortActive.order}`,
          nav: this.selectedSymbol,
        },
      } as Location
      mergeFilterParams(this.filters, loc.query)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    reloadPage: throttle(function (this: any) {
      this.loadPage(this.libraryId, this.page, this.sortActive, this.searchRegex)
    }, 1000),
    async loadPage(libraryId: string, page: number, sort: SortActive, searchRegex?: string) {
      this.selectedSeries = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }

      let authorsFilter = [] as AuthorDto[]
      authorRoles.forEach((role: string) => {
        if (role in this.filters) this.filters[role].forEach((name: string) => authorsFilter.push({
          name: name,
          role: role,
        }))
      })

      const requestLibraryId = libraryId !== LIBRARIES_ALL ? libraryId : undefined
      const complete = parseBooleanFilter(this.filters.complete)
      const seriesPage = await this.$komgaSeries.getSeries(requestLibraryId, pageRequest, undefined, this.filters.status, replaceCompositeReadStatus(this.filters.readStatus), this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter, searchRegex, complete)

      this.totalPages = seriesPage.totalPages
      this.totalElements = seriesPage.totalElements
      this.series = seriesPage.content

      const seriesGroups = await this.$komgaSeries.getAlphabeticalGroups(requestLibraryId, undefined, this.filters.status, replaceCompositeReadStatus(this.filters.readStatus), this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter, complete)
      const nonAlpha = seriesGroups
        .filter((g) => !(/[a-zA-Z]/).test(g.group))
        .reduce((a, b) => a + b.count, 0)
      const all = seriesGroups.reduce((a, b) => a + b.count, 0)
      this.seriesGroups = [
        ...seriesGroups.filter((g) => (/[a-zA-Z]/).test(g.group)),
        {group: '#', count: nonAlpha} as GroupCountDto,
        {group: 'ALL', count: all} as GroupCountDto,
      ]
    },
    getLibraryLazy(libraryId: string): LibraryDto | undefined {
      if (libraryId !== LIBRARIES_ALL) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
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
    editSingleSeries(series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    editMultipleSeries() {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    deleteSeries() {
      this.$store.dispatch('dialogDeleteSeries', this.selectedSeries)
    },
  },
})
</script>
<style scoped>
</style>
