<template>
  <div :style="$vuetify.breakpoint.name === 'xs' ? 'margin-bottom: 56px' : undefined">
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

      <library-navigation v-if="$vuetify.breakpoint.name !== 'xs'" :libraryId="libraryId"/>

      <v-spacer/>

      <page-size-select v-model="pageSize"/>

      <v-btn icon @click="drawer = !drawer">
        <v-icon :color="sortOrFilterActive ? 'secondary' : ''">mdi-filter-variant</v-icon>
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

    <library-navigation v-if="$vuetify.breakpoint.name === 'xs'" :libraryId="libraryId" bottom-navigation/>

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
        v-if="totalPages === 0"
        :title="$t('common.filter_no_matches')"
        :sub-title="$t('common.use_filter_panel_to_change_filter')"
        icon="mdi-book-multiple"
        icon-color="secondary"
      >
      </empty-state>

      <template v-else>
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />

        <item-browser
          :items="series"
          :selected.sync="selectedSeries"
          :edit-function="editSingleSeries"
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
import SeriesMultiSelectBar from '@/components/bars/SeriesMultiSelectBar.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import EmptyState from '@/components/EmptyState.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import LibraryNavigation from '@/components/LibraryNavigation.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import {parseQueryParam, parseQuerySort} from '@/functions/query-params'
import {ReadStatus} from '@/types/enum-books'
import {SeriesStatus, SeriesStatusKeyValue} from '@/types/enum-series'
import {LIBRARY_CHANGED, LIBRARY_DELETED, SERIES_CHANGED} from '@/types/events'
import Vue from 'vue'
import {Location} from 'vue-router'
import {LIBRARIES_ALL, LIBRARY_ROUTE} from '@/types/library'
import FilterDrawer from '@/components/FilterDrawer.vue'
import SortList from '@/components/SortList.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {mergeFilterParams, sortOrFilterActive, toNameValue} from '@/functions/filter'
import {SeriesDto} from "@/types/komga-series";
import {groupAuthorsByRole} from "@/functions/authors";
import {AuthorDto} from "@/types/komga-books";
import {authorRoles} from "@/types/author-roles";

export default Vue.extend({
  name: 'BrowseLibraries',
  components: {
    LibraryActionsMenu,
    EmptyState,
    ToolbarSticky,
    ItemBrowser,
    PageSizeSelect,
    LibraryNavigation,
    SeriesMultiSelectBar,
    FilterDrawer,
    FilterPanels,
    FilterList,
    SortList,
  },
  data: function () {
    return {
      library: undefined as LibraryDto | undefined,
      series: [] as SeriesDto[],
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
  watch: {
    selectedSeries(val: SeriesDto[]) {
      val.forEach(s => {
        const index = this.series.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.series.splice(index, 1, s)
        }
      })
    },
  },
  created() {
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(LIBRARY_CHANGED, this.reloadLibrary)
  },
  beforeDestroy() {
    this.$eventHub.$off(SERIES_CHANGED, this.reloadSeries)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(LIBRARY_CHANGED, this.reloadLibrary)
  },
  async mounted() {
    this.$store.commit('setLibraryRoute', {id: this.libraryId, route: LIBRARY_ROUTE.BROWSE})
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
      this.series = []

      this.loadLibrary(to.params.libraryId)

      this.setWatches()
    }

    next()
  },
  computed: {
    sortOptions(): SortOption[] {
      return [
        {name: this.$t('sort.name').toString(), key: 'metadata.titleSort'},
        {name: this.$t('sort.date_added').toString(), key: 'createdDate'},
        {name: this.$t('sort.date_updated').toString(), key: 'lastModifiedDate'},
        {name: this.$t('sort.folder_name').toString(), key: 'name'},
        {name: this.$t('sort.books_count').toString(), key: 'booksCount'},
      ] as SortOption[]
    },
    filterOptionsList(): FiltersOptions {
      return {
        readStatus: {values: [{name: this.$t('filter.unread').toString(), value: ReadStatus.UNREAD}]},
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
        //@ts-ignore
        r[role] = {name: this.$t(`author_roles.${role}`).toString(), values: this.$_.get(this.filterOptions, role, [])}
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
    async resetParams(route: any, libraryId: string) {
      this.sortActive = parseQuerySort(route.query.sort, this.sortOptions) ||
        this.$store.getters.getLibrarySort(route.params.libraryId) ||
        this.$_.clone(this.sortDefault)

      const requestLibraryId = libraryId !== LIBRARIES_ALL ? libraryId : undefined

      // load dynamic filters
      this.$set(this.filterOptions, 'genre', toNameValue(await this.$komgaReferential.getGenres(requestLibraryId)))
      this.$set(this.filterOptions, 'tag', toNameValue(await this.$komgaReferential.getSeriesTags(requestLibraryId)))
      this.$set(this.filterOptions, 'publisher', toNameValue(await this.$komgaReferential.getPublishers(requestLibraryId)))
      this.$set(this.filterOptions, 'language', (await this.$komgaReferential.getLanguages(requestLibraryId)))
      this.$set(this.filterOptions, 'ageRating', toNameValue(await this.$komgaReferential.getAgeRatings(requestLibraryId)))
      this.$set(this.filterOptions, 'releaseDate', toNameValue(await this.$komgaReferential.getSeriesReleaseDates(requestLibraryId)))
      const grouped = groupAuthorsByRole(await this.$komgaReferential.getAuthors(undefined, requestLibraryId))
      authorRoles.forEach((role: string) => {
        this.$set(this.filterOptions, role, role in grouped ? toNameValue(grouped[role]) : [])
      })

      // get filter from query params or local storage and validate with available filter values
      let activeFilters: any
      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating || route.query.publisher || authorRoles.some(role => role in route.query)) {
        activeFilters = {
          status: parseQueryParam(route.query.status),
          readStatus: parseQueryParam(route.query.readStatus),
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
      } as any
      authorRoles.forEach((role: string) => {
        validFilter[role] = filters[role]?.filter(x => ((this.filterOptions as any)[role] as NameValue[]).map(n => n.value).includes(x)) || []
      })
      return validFilter
    },
    libraryDeleted(event: EventLibraryDeleted) {
      if (event.id === this.libraryId) {
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
        this.loadPage(this.libraryId, val, this.sortActive)
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
      this.loadPage(this.libraryId, this.page, this.sortActive)

      this.setWatches()
    },
    reloadSeries(event: EventSeriesChanged) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.loadPage(this.libraryId, this.page, this.sortActive)
      }
    },
    reloadLibrary(event: EventLibraryChanged) {
      if (this.libraryId === LIBRARIES_ALL || event.id === this.libraryId) {
        this.loadLibrary(this.libraryId)
      }
    },
    async loadLibrary(libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)

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
      this.$router.replace(loc).catch((_: any) => {
      })
    },
    async loadPage(libraryId: string, page: number, sort: SortActive) {
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
      const seriesPage = await this.$komgaSeries.getSeries(requestLibraryId, pageRequest, undefined, this.filters.status, this.filters.readStatus, this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate, authorsFilter)

      this.totalPages = seriesPage.totalPages
      this.totalElements = seriesPage.totalElements
      this.series = seriesPage.content
    },
    getLibraryLazy (libraryId: string): LibraryDto | undefined {
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
    editSingleSeries(series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    editMultipleSeries() {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
  },
})
</script>
<style scoped>
</style>
