<template>
  <div :style="$vuetify.breakpoint.xs ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="selectedBooks.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ toolbarTitle }}</span>
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
      v-model="selectedBooks"
      kind="books"
      show-select-all
      @unselect-all="selectedBooks = []"
      @select-all="selectedBooks = books"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @add-to-readlist="addToReadList"
      @bulk-edit="bulkEditMultipleBooks"
      @edit="editMultipleBooks"
      @delete="deleteBooks"
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
          :items="books"
          :item-context="itemContext"
          :selected.sync="selectedBooks"
          :edit-function="isAdmin ? editSingleBook : undefined"
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
import {MediaProfile, ReadStatus} from '@/types/enum-books'
import {
  BOOK_ADDED,
  BOOK_CHANGED,
  BOOK_DELETED,
  LIBRARY_CHANGED,
  LIBRARY_DELETED,
  READPROGRESS_CHANGED,
  READPROGRESS_DELETED,
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
import {GroupCountDto} from '@/types/komga-series'
import {authorRoles} from '@/types/author-roles'
import {BookSseDto, LibrarySseDto, ReadProgressSeriesSseDto} from '@/types/komga-sse'
import {throttle} from 'lodash'
import {LibraryDto} from '@/types/komga-libraries'
import {ItemContext} from '@/types/items'
import {
  BookSearch,
  SearchConditionAllOfBook,
  SearchConditionAnyOfBook,
  SearchConditionAuthor,
  SearchConditionLibraryId,
  SearchConditionMediaProfile,
  SearchConditionOneShot,
  SearchConditionReadStatus,
  SearchConditionSeries,
  SearchConditionTag,
  SearchOperatorIs,
  SearchOperatorIsFalse,
  SearchOperatorIsNot,
  SearchOperatorIsTrue,
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
import {BookDto} from '@/types/komga-books'

export default Vue.extend({
  name: 'BrowseBooks',
  components: {
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
      books: [] as BookDto[],
      seriesGroups: [] as GroupCountDto[],
      selectedBooks: [] as BookDto[],
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      sortActive: {} as SortActive,
      sortDefault: {key: 'series,metadata.numberSort', order: 'asc'} as SortActive,
      filters: {} as FiltersActive,
      filtersMode: {} as FiltersActiveMode,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      filterModeUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      drawer: false,
      filterOptions: {
        tag: [] as NameValue[],
      },
    }
  },
  props: {
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  watch: {
    '$store.getters.getLibrariesPinned': {
      handler(val) {
        if (this.libraryId === LIBRARIES_ALL)
          this.loadLibrary(this.libraryId)
      },
    },
  },
  created() {
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(LIBRARY_CHANGED, this.libraryChanged)
    this.$eventHub.$on(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$on(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$on(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_DELETED, this.readProgressChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(LIBRARY_CHANGED, this.libraryChanged)
    this.$eventHub.$off(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$off(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$off(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_DELETED, this.readProgressChanged)
  },
  async mounted() {
    this.$store.commit('setLibraryRoute', {id: this.libraryId, route: LIBRARY_ROUTE.BOOKS})
    this.pageSize = this.$store.state.persistedState.browsingPageSize || this.pageSize

    // restore from query param
    await this.resetParams(this.$route, this.libraryId)
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

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
      this.books = []
      this.seriesGroups = []

      this.loadLibrary(to.params.libraryId)

      this.setWatches()
    }

    next()
  },
  computed: {
    library(): LibraryDto | undefined {
      return this.getLibraryLazy(this.libraryId)
    },
    toolbarTitle(): string {
      if (this.library) return this.library.name
      else if (this.$store.getters.getLibrariesPinned.length > 0) return this.$t('common.pinned_libraries').toString()
      else return this.$t('common.all_libraries').toString()
    },
    requestLibraryIds(): string[] {
      return this.libraryId !== LIBRARIES_ALL ? [this.libraryId] : this.$store.getters.getLibrariesPinned.map((it: LibraryDto) => it.id)
    },
    itemContext(): ItemContext[] {
      if (this.sortActive.key === 'metadata.releaseDate') return [ItemContext.SHOW_SERIES, ItemContext.RELEASE_DATE]
      if (this.sortActive.key === 'createdDate') return [ItemContext.SHOW_SERIES, ItemContext.DATE_ADDED]
      if (this.sortActive.key === 'lastModifiedDate') return [ItemContext.SHOW_SERIES, ItemContext.DATE_UPDATED]
      if (this.sortActive.key === 'readProgress.readDate') return [ItemContext.SHOW_SERIES, ItemContext.READ_DATE]
      if (this.sortActive.key === 'fileSize') return [ItemContext.SHOW_SERIES, ItemContext.FILE_SIZE]
      return [ItemContext.SHOW_SERIES]
    },
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('sort.series').toString(), key: 'series,metadata.numberSort'},
        {name: this.$t('sort.name').toString(), key: 'metadata.title'},
        {name: this.$t('sort.date_added').toString(), key: 'createdDate'},
        {name: this.$t('sort.date_updated').toString(), key: 'lastModifiedDate'},
        {name: this.$t('sort.release_date').toString(), key: 'metadata.releaseDate'},
        {name: this.$t('sort.date_read').toString(), key: 'readProgress.readDate'},
        {name: this.$t('sort.file_size').toString(), key: 'fileSize'},
        {name: this.$t('sort.file_name').toString(), key: 'name'},
        {name: this.$t('sort.page_count').toString(), key: 'media.pagesCount'},
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
        tag: {name: this.$t('filter.tag').toString(), values: this.filterOptions.tag, anyAllSelector: true},
        mediaProfile: {
          name: this.$t('filter.media_profile').toString(), values: Object.values(MediaProfile).map(x => ({
            name: i18n.t(`enums.media_profile.${x}`),
            value: new SearchConditionMediaProfile(new SearchOperatorIs(x)),
            nValue: new SearchConditionMediaProfile(new SearchOperatorIsNot(x)),
          } as NameValue)),
        },
      } as FiltersOptions
      authorRoles.forEach((role: string) => {
        r[role] = {
          name: this.$t(`author_roles.${role}`).toString(),
          search: async search => {
            return (await this.$komgaReferential.getAuthors(search, role, this.requestLibraryIds))
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
    resetSortAndFilters() {
      this.drawer = false
      for (const prop in this.filters) {
        this.$set(this.filters, prop, [])
      }
      this.sortActive = this.sortDefault
      this.$store.commit('setLibraryFilterBooks', {id: this.libraryId, filter: this.filters})
      this.$store.commit('setLibrarySortBooks', {id: this.libraryId, sort: this.sortActive})
      this.updateRouteAndReload()
    },
    async resetParams(route: any, libraryId: string) {
      this.sortActive = parseQuerySort(route.query.sort, this.sortOptions) ||
        this.$store.getters.getLibrarySortBooks(route.params.libraryId) ||
        this.$_.clone(this.sortDefault)

      const requestLibraryIds = libraryId !== LIBRARIES_ALL ? [libraryId] : this.$store.getters.getLibrariesPinned.map((it: LibraryDto) => it.id)

      // load dynamic filters
      const [tags] = await Promise.all([
        this.$komgaReferential.getBookTags(undefined, undefined, requestLibraryIds),
      ])
      this.$set(this.filterOptions, 'tag', toNameValueCondition(tags, x => new SearchConditionTag(new SearchOperatorIs(x)), x => new SearchConditionTag(new SearchOperatorIsNot(x))))

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.readStatus || route.query.tag || authorRoles.some(role => role in route.query) || route.query.oneshot) {
        activeFilters = {
          readStatus: route.query.readStatus || [],
          tag: route.query.tag || [],
          oneshot: route.query.oneshot || [],
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = route.query[role] || []
        })
      } else {
        activeFilters = this.$store.getters.getLibraryFilterBooks(route.params.libraryId) || {} as FiltersActive
      }
      this.filters = this.validateFilters(activeFilters)

      // get filter mode from query params or local storage
      let activeFiltersMode: any
      if (route.query.filterMode) {
        activeFiltersMode = route.query.filterMode
      } else {
        activeFiltersMode = this.$store.getters.getLibraryFilterModeBooks(route.params.libraryId) || {} as FiltersActiveMode
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
        readStatus: this.$_.intersectionWith(filters.readStatus, extractFilterOptionsValues(this.filterOptionsList.readStatus.values), objIsEqual) || [],
        tag: this.$_.intersectionWith(filters.tag, extractFilterOptionsValues(this.filterOptions.tag), objIsEqual) || [],
        oneshot: this.$_.intersectionWith(filters.oneshot, extractFilterOptionsValues(this.filterOptionsList.oneshot.values), objIsEqual) || [],
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
        this.$store.commit('setLibrarySortBooks', {id: this.libraryId, sort: val})
        this.updateRouteAndReload()
      })
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$store.commit('setLibraryFilterBooks', {id: this.libraryId, filter: val})
        this.updateRouteAndReload()
      })
      this.filterModeUnwatch = this.$watch('filtersMode', (val) => {
        this.$store.commit('setLibraryFilterModeBooks', {id: this.libraryId, filterMode: val})
        this.updateRouteAndReload()
      })
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$store.commit('setBrowsingPageSize', val)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.libraryId, val, this.sortActive)
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
      this.loadPage(this.libraryId, this.page, this.sortActive)

      this.setWatches()
    },
    libraryChanged(event: LibrarySseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.loadLibrary(this.libraryId)
      }
    },
    bookChanged(event: BookSseDto) {
      if (event.libraryId === this.libraryId) this.reloadPage()
    },
    readProgressChanged(event: ReadProgressSeriesSseDto) {
      if (this.books.some(b => b.id === event.seriesId)) this.reloadPage()
    },
    async loadLibrary(libraryId: string) {
      if (this.library != undefined) document.title = `Komga - ${this.library.name}`

      await this.loadPage(libraryId, this.page, this.sortActive)
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {libraryId: this.$route.params.libraryId},
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
          sort: `${this.sortActive.key},${this.sortActive.order}`,
        },
      } as Location
      mergeFilterParams(this.filters, loc.query)
      loc.query['filterMode'] = this.validateFiltersMode(this.filtersMode)
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    reloadPage: throttle(function (this: any) {
      this.loadPage(this.libraryId, this.page, this.sortActive)
    }, 1000),
    async loadPage(libraryId: string, page: number, sort: SortActive) {
      this.selectedBooks = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }

      const conditions = [] as SearchConditionSeries[]
      if (libraryId !== LIBRARIES_ALL) conditions.push(new SearchConditionLibraryId(new SearchOperatorIs(libraryId)))
      else {
        conditions.push(new SearchConditionAnyOfBook(
          this.$store.getters.getLibrariesPinned.map((it: LibraryDto) => new SearchConditionLibraryId(new SearchOperatorIs(it.id))),
        ))
      }
      if (this.filters.readStatus && this.filters.readStatus.length > 0) conditions.push(new SearchConditionAnyOfBook(this.filters.readStatus))
      if (this.filters.tag && this.filters.tag.length > 0) this.filtersMode?.tag?.allOf ? conditions.push(new SearchConditionAllOfBook(this.filters.tag)) : conditions.push(new SearchConditionAnyOfBook(this.filters.tag))
      if (this.filters.oneshot && this.filters.oneshot.length > 0) conditions.push(...this.filters.oneshot)
      if (this.filters.mediaProfile && this.filters.mediaProfile.length > 0) this.filtersMode?.mediaProfile?.allOf ? conditions.push(new SearchConditionAllOfBook(this.filters.mediaProfile)) : conditions.push(new SearchConditionAnyOfBook(this.filters.mediaProfile))
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
          conditions.push(this.filtersMode[role]?.allOf ? new SearchConditionAllOfBook(authorConditions) : new SearchConditionAnyOfBook(authorConditions))
        }
      })

      const booksPage = await this.$komgaBooks.getBooksList({
        condition: new SearchConditionAllOfBook(conditions),
      } as BookSearch, pageRequest)

      this.totalPages = booksPage.totalPages
      this.totalElements = booksPage.totalElements
      this.books = booksPage.content
    },
    getLibraryLazy(libraryId: string): LibraryDto | undefined {
      if (libraryId !== LIBRARIES_ALL) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
    },
    async markSelectedRead() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, {completed: true}),
      ))
      this.selectedBooks = []
    },
    async markSelectedUnread() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = []
    },
    addToReadList() {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks.map(b => b.id))
    },
    editSingleBook(book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    editMultipleBooks() {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    bulkEditMultipleBooks() {
      this.$store.dispatch('dialogUpdateBulkBooks', this.$_.sortBy(this.selectedBooks, ['metadata.numberSort']))
    },
    deleteBooks() {
      this.$store.dispatch('dialogDeleteBook', this.selectedBooks)
    },
  },
})
</script>
<style scoped>
</style>
