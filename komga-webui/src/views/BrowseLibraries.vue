<template>
  <div :style="$vuetify.breakpoint.xs ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="selectedSeries.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>
      <libraries-actions-menu v-else/>

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
          :filters-active-mode.sync="filtersMode"
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
import {parseQuerySort} from '@/functions/query-params'
import {ReadStatus} from '@/types/enum-books'
import {SeriesStatus} from '@/types/enum-series'
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
import {
  extractFilterOptionsValues,
  mergeFilterParams,
  sortOrFilterActive,
  toNameValueCondition,
} from '@/functions/filter'
import {GroupCountDto, Oneshot, SeriesDto} from '@/types/komga-series'
import {authorRoles} from '@/types/author-roles'
import {LibrarySseDto, ReadProgressSeriesSseDto, SeriesSseDto} from '@/types/komga-sse'
import {throttle} from 'lodash'
import AlphabeticalNavigation from '@/components/AlphabeticalNavigation.vue'
import {LibraryDto} from '@/types/komga-libraries'
import {ItemContext} from '@/types/items'
import {
  BookSearch,
  SearchConditionAgeRating,
  SearchConditionAllOfSeries, SearchConditionAnyOfBook,
  SearchConditionAnyOfSeries,
  SearchConditionAuthor,
  SearchConditionComplete,
  SearchConditionGenre,
  SearchConditionLanguage,
  SearchConditionLibraryId,
  SearchConditionOneShot,
  SearchConditionPublisher,
  SearchConditionReadStatus,
  SearchConditionReleaseDate,
  SearchConditionSeries,
  SearchConditionSeriesId,
  SearchConditionSeriesStatus,
  SearchConditionSharingLabel,
  SearchConditionTag,
  SearchConditionTitleSort,
  SearchOperatorAfter,
  SearchOperatorBefore,
  SearchOperatorBeginsWith,
  SearchOperatorDoesNotBeginWith,
  SearchOperatorIs,
  SearchOperatorIsFalse,
  SearchOperatorIsNot,
  SearchOperatorIsNotNull,
  SearchOperatorIsNull,
  SearchOperatorIsTrue,
  SeriesSearch,
} from '@/types/komga-search'
import i18n from '@/i18n'
import {objIsEqual} from '@/functions/object'
import {
  FILTER_ANY,
  FILTER_NONE,
  FilterMode,
  FiltersActive,
  FiltersActiveMode,
  FiltersOptions,
  NameValue,
} from '@/types/filter'
import LibrariesActionsMenu from '@/components/menus/LibrariesActionsMenu.vue'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: {
    LibrariesActionsMenu,
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
      filtersMode: {} as FiltersActiveMode,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      filterModeUnwatch: null as any,
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
        sharingLabel: [] as NameValue[],
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
    symbolCondition(): SearchConditionSeries | undefined {
      if (this.selectedSymbol === 'ALL') return undefined
      if (this.selectedSymbol === '#') return new SearchConditionAllOfSeries(
        this.alphabeticalNavigation
          .filter(it => it !== 'ALL' && it !== '#')
          .map(it => new SearchConditionTitleSort(new SearchOperatorDoesNotBeginWith(it))),
      )
      return new SearchConditionTitleSort(new SearchOperatorBeginsWith(this.selectedSymbol))
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
        {name: this.$t('sort.date_read').toString(), key: 'readDate'},
        {name: this.$t('sort.release_date').toString(), key: 'booksMetadata.releaseDate'},
        {name: this.$t('sort.folder_name').toString(), key: 'name'},
        {name: this.$t('sort.books_count').toString(), key: 'booksCount'},
        {name: this.$t('sort.random').toString(), key: 'random'},
      ] as SortOption[]
    },
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {
              name: this.$t('filter.unread').toString(),
              value: new SearchConditionReadStatus(new SearchOperatorIs(ReadStatus.UNREAD)),
              nValue: new SearchConditionReadStatus(new SearchOperatorIsNot(ReadStatus.UNREAD)),
            },
            {
              name: this.$t('filter.in_progress').toString(),
              value: new SearchConditionReadStatus(new SearchOperatorIs(ReadStatus.IN_PROGRESS)),
              nValue: new SearchConditionReadStatus(new SearchOperatorIsNot(ReadStatus.IN_PROGRESS)),
            },
            {
              name: this.$t('filter.read').toString(),
              value: new SearchConditionReadStatus(new SearchOperatorIs(ReadStatus.READ)),
              nValue: new SearchConditionReadStatus(new SearchOperatorIsNot(ReadStatus.READ)),
            },
          ],
        },
        complete: {
          values: [{
            name: this.$t('filter.complete').toString(),
            value: new SearchConditionComplete(new SearchOperatorIsTrue()),
            nValue: new SearchConditionComplete(new SearchOperatorIsFalse()),
          }],
        },
        oneshot: {
          values: [{
            name: this.$t('filter.oneshot').toString(),
            value: new SearchConditionOneShot(new SearchOperatorIsTrue()),
            nValue: new SearchConditionOneShot(new SearchOperatorIsFalse()),
          }],
        },
      } as FiltersOptions
    },
    filterOptionsPanel(): FiltersOptions {
      const r = {
        status: {
          name: this.$t('filter.status').toString(), values: Object.values(SeriesStatus).map(x => ({
            name: i18n.t(`enums.series_status.${x}`),
            value: new SearchConditionSeriesStatus(new SearchOperatorIs(x)),
            nValue: new SearchConditionSeriesStatus(new SearchOperatorIsNot(x)),
          } as NameValue)),
        },
        genre: {name: this.$t('filter.genre').toString(), values: this.filterOptions.genre, anyAllSelector: true},
        tag: {name: this.$t('filter.tag').toString(), values: this.filterOptions.tag, anyAllSelector: true},
        publisher: {name: this.$t('filter.publisher').toString(), values: this.filterOptions.publisher},
        language: {name: this.$t('filter.language').toString(), values: this.filterOptions.language},
        ageRating: {
          name: this.$t('filter.age_rating').toString(),
          values: this.filterOptions.ageRating.map((x: NameValue) => ({
              name: (x.value === 'None' ? this.$t('filter.age_rating_none').toString() : x.name),
              value: x.value,
              nValue: x.nValue,
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
          values: [{
            name: this.$t('filter.any').toString(),
            value: FILTER_ANY,
            nValue: FILTER_NONE,
          }],
          anyAllSelector: true,
        }
      })
      r['sharingLabel'] = {name: this.$t('filter.sharing_label').toString(), values: this.filterOptions.sharingLabel}
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
    selectedOneshots(): boolean {
      return this.selectedSeries.every(s => s.oneshot)
    },
  },
  methods: {
    filterByStarting(symbol: string) {
      this.selectedSymbol = symbol
      this.page = 1
      this.updateRoute()
      this.loadPage(this.libraryId, 1, this.sortActive, this.symbolCondition)
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
      const [genres, tags, publishers, languages, ageRatings, releaseDates, sharingLabels] = await Promise.all([
        this.$komgaReferential.getGenres(requestLibraryId),
        this.$komgaReferential.getSeriesAndBookTags(requestLibraryId),
        this.$komgaReferential.getPublishers(requestLibraryId),
        this.$komgaReferential.getLanguages(requestLibraryId),
        this.$komgaReferential.getAgeRatings(requestLibraryId),
        this.$komgaReferential.getSeriesReleaseDates(requestLibraryId),
        this.$komgaReferential.getSharingLabels(requestLibraryId),
      ])
      this.$set(this.filterOptions, 'genre', toNameValueCondition(genres, x => new SearchConditionGenre(new SearchOperatorIs(x)), x => new SearchConditionGenre(new SearchOperatorIsNot(x))))
      this.$set(this.filterOptions, 'tag', toNameValueCondition(tags, x => new SearchConditionTag(new SearchOperatorIs(x)), x => new SearchConditionTag(new SearchOperatorIsNot(x))))
      this.$set(this.filterOptions, 'publisher', toNameValueCondition(publishers, x => new SearchConditionPublisher(new SearchOperatorIs(x)), x => new SearchConditionPublisher(new SearchOperatorIsNot(x))))
      this.$set(this.filterOptions, 'language', languages.map((x: NameValue) => {
        return {
          name: x.name,
          value: new SearchConditionLanguage(new SearchOperatorIs(x.value)),
          nValue: new SearchConditionLanguage(new SearchOperatorIsNot(x.value)),
        } as NameValue
      }))
      this.$set(this.filterOptions, 'ageRating', toNameValueCondition(ageRatings, x => new SearchConditionAgeRating(isFinite(x) ? new SearchOperatorIs(x) : new SearchOperatorIsNull()), x => new SearchConditionAgeRating(isFinite(x) ? new SearchOperatorIsNot(x) : new SearchOperatorIsNotNull())))
      this.$set(this.filterOptions, 'releaseDate', toNameValueCondition(
        releaseDates,
        x => {
          const year = Number.parseInt(x)
          return year ? new SearchConditionAllOfSeries([
            new SearchConditionReleaseDate(new SearchOperatorAfter(`${year - 1}-12-31T12:00:00Z`)),
            new SearchConditionReleaseDate(new SearchOperatorBefore(`${year + 1}-01-01T12:00:00Z`)),
          ]) : new SearchConditionAllOfSeries([])
        },
        year =>
          new SearchConditionAnyOfSeries([
              new SearchConditionReleaseDate(new SearchOperatorAfter(`${year}-12-31T12:00:00Z`)),
              new SearchConditionReleaseDate(new SearchOperatorBefore(`${year}-01-01T12:00:00Z`)),
              new SearchConditionReleaseDate(new SearchOperatorIsNull()),
            ],
          ),
      ))
      this.$set(this.filterOptions, 'sharingLabel', toNameValueCondition(sharingLabels, x => new SearchConditionSharingLabel(new SearchOperatorIs(x)), x => new SearchConditionSharingLabel(new SearchOperatorIsNot(x))))

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating || route.query.publisher || authorRoles.some(role => role in route.query) || route.query.complete || route.query.oneshot || route.query.sharingLabel) {
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
          oneshot: route.query.oneshot || [],
          sharingLabel: route.query.sharingLabel || [],
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = route.query[role] || []
        })
      } else {
        activeFilters = this.$store.getters.getLibraryFilter(route.params.libraryId) || {} as FiltersActive
      }
      this.filters = this.validateFilters(activeFilters)

      // get filter mode from query params or local storage
      let activeFiltersMode: any
      if (route.query.filterMode) {
        activeFiltersMode = route.query.filterMode
      } else {
        activeFiltersMode = this.$store.getters.getLibraryFilterMode(route.params.libraryId) || {} as FiltersActiveMode
      }
      this.filtersMode = this.validateFiltersMode(activeFiltersMode)
    },
    validateFiltersMode(filtersMode: any): FiltersActiveMode {
      const validFilterMode = {} as FiltersActiveMode
      for (let key in filtersMode) {
        if (filtersMode[key].allOf == 'true' || filtersMode[key].allOf == true) validFilterMode[key] = {allOf: true} as FilterMode
      }
      return validFilterMode
    },
    validateFilters(filters: FiltersActive): FiltersActive {
      const validFilter = {
        status: this.$_.intersectionWith(filters.status, extractFilterOptionsValues(this.filterOptionsPanel.status.values), objIsEqual) || [],
        readStatus: this.$_.intersectionWith(filters.readStatus, extractFilterOptionsValues(this.filterOptionsList.readStatus.values), objIsEqual) || [],
        genre: this.$_.intersectionWith(filters.genre, extractFilterOptionsValues(this.filterOptions.genre), objIsEqual) || [],
        tag: this.$_.intersectionWith(filters.tag, extractFilterOptionsValues(this.filterOptions.tag), objIsEqual) || [],
        publisher: this.$_.intersectionWith(filters.publisher, extractFilterOptionsValues(this.filterOptions.publisher), objIsEqual) || [],
        language: this.$_.intersectionWith(filters.language, extractFilterOptionsValues(this.filterOptions.language), objIsEqual) || [],
        ageRating: this.$_.intersectionWith(filters.ageRating, extractFilterOptionsValues(this.filterOptions.ageRating), objIsEqual) || [],
        releaseDate: this.$_.intersectionWith(filters.releaseDate, extractFilterOptionsValues(this.filterOptions.releaseDate), objIsEqual) || [],
        complete: this.$_.intersectionWith(filters.complete, extractFilterOptionsValues(this.filterOptionsList.complete.values), objIsEqual) || [],
        oneshot: this.$_.intersectionWith(filters.oneshot, extractFilterOptionsValues(this.filterOptionsList.oneshot.values), objIsEqual) || [],
        sharingLabel: this.$_.intersectionWith(filters.sharingLabel, extractFilterOptionsValues(this.filterOptions.sharingLabel), objIsEqual) || [],
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
      this.filterModeUnwatch = this.$watch('filtersMode', (val) => {
        this.$store.commit('setLibraryFilterMode', {id: this.libraryId, filterMode: val})
        this.updateRouteAndReload()
      })
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$store.commit('setBrowsingPageSize', val)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.libraryId, val, this.sortActive, this.symbolCondition)
      })
    },
    unsetWatches() {
      this.sortUnwatch()
      this.filterUnwatch()
      this.filterModeUnwatch()
      this.pageUnwatch()
      this.pageSizeUnwatch()
    },
    updateRouteAndReload() {
      this.unsetWatches()

      this.page = 1

      this.updateRoute()
      this.loadPage(this.libraryId, this.page, this.sortActive, this.symbolCondition)

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
      if (this.library != undefined) document.title = `Komga - ${this.library.name}`

      await this.loadPage(libraryId, this.page, this.sortActive, this.symbolCondition)
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
      loc.query['filterMode'] = this.validateFiltersMode(this.filtersMode)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    reloadPage: throttle(function (this: any) {
      this.loadPage(this.libraryId, this.page, this.sortActive, this.symbolCondition)
    }, 1000),
    async loadPage(libraryId: string, page: number, sort: SortActive, symbolCondition?: SearchConditionSeries) {
      this.selectedSeries = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }

      const conditions = [] as SearchConditionSeries[]
      if (libraryId !== LIBRARIES_ALL) conditions.push(new SearchConditionLibraryId(new SearchOperatorIs(libraryId)))
      if (this.filters.status && this.filters.status.length > 0) this.filtersMode?.status?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.status)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.status))
      if (this.filters.readStatus && this.filters.readStatus.length > 0) conditions.push(new SearchConditionAnyOfSeries(this.filters.readStatus))
      if (this.filters.genre && this.filters.genre.length > 0) this.filtersMode?.genre?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.genre)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.genre))
      if (this.filters.tag && this.filters.tag.length > 0) this.filtersMode?.tag?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.tag)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.tag))
      if (this.filters.language && this.filters.language.length > 0) this.filtersMode?.language?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.language)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.language))
      if (this.filters.publisher && this.filters.publisher.length > 0) this.filtersMode?.publisher?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.publisher)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.publisher))
      if (this.filters.ageRating && this.filters.ageRating.length > 0) this.filtersMode?.ageRating?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.ageRating)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.ageRating))
      if (this.filters.releaseDate && this.filters.releaseDate.length > 0) this.filtersMode?.releaseDate?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.releaseDate)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.releaseDate))
      if (this.filters.sharingLabel && this.filters.sharingLabel.length > 0) this.filtersMode?.sharingLabel?.allOf ? conditions.push(new SearchConditionAllOfSeries(this.filters.sharingLabel)) : conditions.push(new SearchConditionAnyOfSeries(this.filters.sharingLabel))
      if (this.filters.complete && this.filters.complete.length > 0) conditions.push(...this.filters.complete)
      if (this.filters.oneshot && this.filters.oneshot.length > 0) conditions.push(...this.filters.oneshot)
      authorRoles.forEach((role: string) => {
        if (role in this.filters) {
          const authorConditions = this.filters[role].map((name: string) => {
            if (name === FILTER_ANY)
              return new SearchConditionAuthor(new SearchOperatorIs({
                role: role,
              }))
            else if (name === FILTER_NONE)
              return new SearchConditionAuthor(new SearchOperatorIsNot({
                role: role,
              }))
            else
              return new SearchConditionAuthor(new SearchOperatorIs({
                name: name,
                role: role,
              }))
          })
          conditions.push(this.filtersMode[role]?.allOf ? new SearchConditionAllOfSeries(authorConditions) : new SearchConditionAnyOfSeries(authorConditions))
        }
      })

      const groupConditions = this.$_.cloneDeep(conditions)
      if (symbolCondition) conditions.push(symbolCondition)

      const seriesPage = await this.$komgaSeries.getSeriesList({
        condition: new SearchConditionAllOfSeries(conditions),
      } as SeriesSearch, pageRequest)

      this.totalPages = seriesPage.totalPages
      this.totalElements = seriesPage.totalElements
      this.series = seriesPage.content

      const seriesGroups = await this.$komgaSeries.getSeriesListByAlphabeticalGroups({
        condition: new SearchConditionAllOfSeries(groupConditions),
      } as SeriesSearch)
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
      this.$store.dispatch('dialogAddSeriesToCollection', this.selectedSeries.map(s => s.id))
    },
    async addToReadList() {
      const conditions = this.selectedSeries.map(s => new SearchConditionSeriesId(new SearchOperatorIs(s.id)))
      const books = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionAnyOfBook(conditions),
      } as BookSearch, {unpaged: true})
      this.$store.dispatch('dialogAddBooksToReadList', books.content.map(b => b.id))
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
      if (this.selectedOneshots) {
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
  },
})
</script>
<style scoped>
</style>
