<template>
  <div :style="$vuetify.breakpoint.xs ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="selectedItems.length === 0">
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

      <library-item-type-select :libraryId="libraryId"/>

      <page-size-select v-model="pageSize"/>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="sortOrFilterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
      </v-btn>
    </toolbar-sticky>

    <multi-select-bar
      v-model="selectedItems"
      kind="books"
      show-select-all
      @unselect-all="selectedItems = []"
      @select-all="selectedItems = items"
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
        :group-count="itemGroups"
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
          :items="items"
          :item-context="itemContext"
          :selected.sync="selectedItems"
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
import LibraryItemTypeSelect from '@/components/LibraryItemTypeSelect.vue'
import {parseBooleanFilter, parseQuerySort} from '@/functions/query-params'
import {ReadStatus} from '@/types/enum-books'
import {
  LIBRARY_CHANGED,
  LIBRARY_DELETED,
  READPROGRESS_DELETED,
  READPROGRESS_CHANGED,
  BOOK_ADDED,
  BOOK_CHANGED,
  BOOK_DELETED,
} from '@/types/events'
import Vue from 'vue'
import {Location} from 'vue-router'
import {LIBRARIES_ALL, LIBRARY_ROUTE} from '@/types/library'
import FilterDrawer from '@/components/FilterDrawer.vue'
import SortList from '@/components/SortList.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {mergeFilterParams, sortOrFilterActive, toNameValue} from '@/functions/filter'
import {GroupCountDto} from '@/types/komga-series'
import {AuthorDto, BookDto} from '@/types/komga-books'
import {authorRoles} from '@/types/author-roles'
import {BookSseDto, LibrarySseDto, ReadProgressSseDto } from '@/types/komga-sse'
import {throttle} from 'lodash'
import AlphabeticalNavigation from '@/components/AlphabeticalNavigation.vue'
import {LibraryDto} from '@/types/komga-libraries'
import {ItemContext} from '@/types/items'
import { ContextOrigin } from '@/types/context'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: {
    AlphabeticalNavigation,
    LibraryActionsMenu,
    EmptyState,
    ToolbarSticky,
    ItemBrowser,
    PageSizeSelect,
    LibraryItemTypeSelect,
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
      items: [] as BookDto[],
      itemGroups: [] as GroupCountDto[],
      alphabeticalNavigation: ['ALL', '#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'],
      selectedSymbol: 'ALL',
      selectedItems: [] as BookDto[],
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      sortActive: {} as SortActive,
      filters: {} as FiltersActive,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      selectedItemTypeUnwatch: null as any,
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
    this.$eventHub.$on(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$on(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(LIBRARY_CHANGED, this.libraryChanged)
    this.$eventHub.$on(READPROGRESS_CHANGED, this.bookReadProgressChanged)
    this.$eventHub.$on(READPROGRESS_DELETED, this.bookReadProgressChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$off(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(LIBRARY_CHANGED, this.libraryChanged)
    this.$eventHub.$off(READPROGRESS_CHANGED, this.bookReadProgressChanged)
    this.$eventHub.$off(READPROGRESS_DELETED, this.bookReadProgressChanged)
  },
  async mounted() {
    this.$store.commit('setLibraryRoute', {id: this.libraryId, route: LIBRARY_ROUTE.BROWSE_BY_BOOK})
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
    if (to.params.libraryId !== from.params.libraryId || to.params.itemType !== from.params.itemType) {
      this.unsetWatches()

      // reset
      await this.resetParams(to, to.params.libraryId)
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.items = []
      this.itemGroups = []
      this.selectedSymbol = 'ALL'

      this.loadLibrary(to.params.libraryId)

      this.setWatches()
    }

    next()
  },
  computed: {
    searchRegex(): string | undefined {
        if (this.selectedSymbol === 'ALL') return undefined
        if (this.selectedSymbol === '#') return '^[^a-z]'
        return `^${this.selectedSymbol}`
    },
    itemContext(): ItemContext[] {
      var itemContext = []
      if (this.sortActive.key === 'metadata.releaseDate') itemContext.push(ItemContext.RELEASE_DATE)
      if (this.sortActive.key === 'created') itemContext.push(ItemContext.DATE_ADDED)
      if (this.sortActive.key === 'lastModified') itemContext.push(ItemContext.DATE_UPDATED)
      itemContext.push(ItemContext.SHOW_SERIES)
      return itemContext
    },
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('sort.name').toString(), key: 'metadata.title'},
        {name: this.$t('sort.date_added').toString(), key: 'created'},
        {name: this.$t('sort.date_updated').toString(), key: 'lastModified'},
        {name: this.$t('sort.release_date').toString(), key: 'metadata.releaseDate' },
        {name: this.$t('sort.folder_name').toString(), key: 'name'},
      ] as SortOption[]
    },
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {
          values: [
            {name: this.$t('filter.unread').toString(), value: ReadStatus.UNREAD},
            {name: this.$t('filter.in_progress').toString(), value: ReadStatus.IN_PROGRESS},
            {name: this.$t('filter.read').toString(), value: ReadStatus.READ},
          ],
        },
      } as FiltersOptions
    },
    filterOptionsPanel(): FiltersOptions {
      const r = {
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
    sortDefault() : SortActive {
      return {key: 'metadata.releaseDate', order: 'asc'}
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
      this.$store.commit('setLibraryBookFilter', {id: this.libraryId, filter: this.filters})
      this.$store.commit('setLibraryBookSort', {id: this.libraryId, sort: this.sortActive})
      this.updateRouteAndReload()
    },
    async resetParams(route: any, libraryId: string) {
      this.sortActive = parseQuerySort(route.query.sort, this.sortOptions) ||
        this.$store.getters.getLibraryBookSort(route.params.libraryId) ||
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
      this.$set(this.filterOptions, 'genre', toNameValue(genres))
      this.$set(this.filterOptions, 'tag', toNameValue(tags))
      this.$set(this.filterOptions, 'publisher', toNameValue(publishers))
      this.$set(this.filterOptions, 'language', (languages))
      this.$set(this.filterOptions, 'ageRating', toNameValue(ageRatings))
      this.$set(this.filterOptions, 'releaseDate', toNameValue(releaseDates))
      this.$set(this.filterOptions, 'sharingLabel', toNameValue(sharingLabels))

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating || route.query.publisher || authorRoles.some(role => role in route.query) || route.query.complete || route.query.sharingLabel) {
        activeFilters = {
          readStatus: route.query.readStatus || [],
          genre: route.query.genre || [],
          tag: route.query.tag || [],
          publisher: route.query.publisher || [],
          language: route.query.language || [],
          ageRating: route.query.ageRating || [],
          releaseDate: route.query.releaseDate || [],
          complete: route.query.complete || [],
          sharingLabel: route.query.sharingLabel || [],
        }
        authorRoles.forEach((role: string) => {
          activeFilters[role] = route.query[role] || []
        })
      } else {
        activeFilters = this.$store.getters.getLibraryBookFilter(route.params.libraryId) || {} as FiltersActive
      }
      this.filters = this.validateFilters(activeFilters)
    },
    validateFilters(filters: FiltersActive): FiltersActive {
      const validFilter = {
        readStatus: filters.readStatus?.filter(x => Object.keys(ReadStatus).includes(x)) || [],
        genre: filters.genre?.filter(x => this.filterOptions.genre.map(n => n.value).includes(x)) || [],
        tag: filters.tag?.filter(x => this.filterOptions.tag.map(n => n.value).includes(x)) || [],
        publisher: filters.publisher?.filter(x => this.filterOptions.publisher.map(n => n.value).includes(x)) || [],
        language: filters.language?.filter(x => this.filterOptions.language.map(n => n.value).includes(x)) || [],
        ageRating: filters.ageRating?.filter(x => this.filterOptions.ageRating.map(n => n.value).includes(x)) || [],
        releaseDate: filters.releaseDate?.filter(x => this.filterOptions.releaseDate.map(n => n.value).includes(x)) || [],
        complete: filters.complete?.filter(x => x === 'true' || x === 'false') || [],
        sharingLabel: filters.sharingLabel?.filter(x => this.filterOptions.sharingLabel.map(n => n.value).includes(x)) || [],
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
        this.$store.commit('setLibraryBookSort', {id: this.libraryId, sort: val})
        this.updateRouteAndReload()
      })
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$store.commit('setLibraryBookFilter', {id: this.libraryId, filter: val})
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
    bookChanged(event: BookSseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.reloadPage()
      }
    },
    libraryChanged(event: LibrarySseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.loadLibrary(this.libraryId)
      }
    },
    bookReadProgressChanged(event: ReadProgressSseDto) {
      if (this.items.some(b => b.id === event.bookId)) this.reloadPage()
    },
    async loadLibrary(libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)

      await this.loadPage(libraryId, this.page, this.sortActive, this.searchRegex)
    },
    updateRoute() {
      const loc = {
        name: this.$route.name,
        params: {
          libraryId: this.$route.params.libraryId,
        },
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
      this.selectedItems = []

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
      const booksPage = await this.$komgaBooks.getBooks(requestLibraryId, pageRequest, undefined, undefined, this.filters.readStatus, undefined, searchRegex, this.filters.publisher, this.filters.releaseDate, this.filters.tag, authorsFilter, this.filters.sharingLabel, this.filters.genre, this.filters.language, this.filters.ageRating)
      booksPage.content.forEach((x: BookDto) => x.context = {origin: ContextOrigin.LIBRARY, id: this.libraryId})

      this.totalPages = booksPage.totalPages
      this.totalElements = booksPage.totalElements
      this.items = booksPage.content

      const seriesGroups = await this.$komgaSeries.getAlphabeticalGroups(requestLibraryId, undefined, this.filters.status, this.filters.readStatus, this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter, complete, this.filters.sharingLabel)
      const nonAlpha = seriesGroups
        .filter((g) => !(/[a-zA-Z]/).test(g.group))
        .reduce((a, b) => a + b.count, 0)
      const all = seriesGroups.reduce((a, b) => a + b.count, 0)
      this.itemGroups = [
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
      await Promise.all(this.selectedItems.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, {completed: true}),
      ))

      this.selectedItems = []
    },
    async markSelectedUnread() {
      await Promise.all(this.selectedItems.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedItems = []
    },
    addToReadList() {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedItems)
    },
    editSingleBook(item: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', item)
    },
    editMultipleBooks() {
      this.$store.dispatch('dialogUpdateBooks', this.selectedItems)
    },
    deleteBooks() {
      this.$store.dispatch('dialogDeleteBook', this.selectedItems)
    },
    bulkEditMultipleBooks() {
      this.$store.dispatch('dialogUpdateBulkBooks', this.$_.sortBy(this.selectedItems, ['metadata.numberSort']))
    },
  },
})
</script>
<style scoped>
</style>
