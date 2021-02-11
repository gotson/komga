<template>
  <div :style="$vuetify.breakpoint.name === 'xs' ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="selectedSeries.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : $t('common.all_libraries') }}</span>
        <v-chip label class="ml-4" v-if="totalElements">
          <span style="font-size: 1.1rem">{{ totalElements }}</span>
        </v-chip>
      </v-toolbar-title>

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

    <library-navigation :libraryId="libraryId"/>

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
import {parseQueryFilter, parseQuerySort} from '@/functions/query-params'
import {ReadStatus} from '@/types/enum-books'
import {SeriesStatus, SeriesStatusKeyValue} from '@/types/enum-series'
import {LIBRARY_CHANGED, LIBRARY_DELETED, SERIES_CHANGED} from '@/types/events'
import Vue from 'vue'
import {Location} from 'vue-router'
import {LIBRARIES_ALL} from '@/types/library'
import FilterDrawer from '@/components/FilterDrawer.vue'
import SortList from '@/components/SortList.vue'
import FilterPanels from '@/components/FilterPanels.vue'
import FilterList from '@/components/FilterList.vue'
import {mergeFilterParams, sortOrFilterActive, toNameValue} from '@/functions/filter'
import {SeriesDto} from "@/types/komga-series";

const cookiePageSize = 'pagesize'

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
      sortOptions: [
        {name: this.$i18n.t('sort.name').toString(), key: 'metadata.titleSort'},
        {name: this.$i18n.t('sort.date_added').toString(), key: 'createdDate'},
        {name: this.$i18n.t('sort.date_updated').toString(), key: 'lastModifiedDate'},
      ] as SortOption[],
      sortActive: {} as SortActive,
      sortDefault: {key: 'metadata.titleSort', order: 'asc'} as SortActive,
      filterOptionsList: {
        readStatus: {values: [{name: this.$i18n.t('filter.unread').toString(), value: ReadStatus.UNREAD}]},
      } as FiltersOptions,
      filterOptionsPanel: {
        status: {name: this.$i18n.t('filter.status').toString(), values: SeriesStatusKeyValue},
        genre: {name: this.$i18n.t('filter.genre').toString(), values: []},
        tag: {name: this.$i18n.t('filter.tag').toString(), values: []},
        publisher: {name: this.$i18n.t('filter.publisher').toString(), values: []},
        language: {name: this.$i18n.t('filter.language').toString(), values: []},
        ageRating: {name: this.$i18n.t('filter.age_rating').toString(), values: []},
        releaseDate: {name: this.$i18n.t('filter.release_date').toString(), values: []},
      } as FiltersOptions,
      filters: {} as FiltersActive,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      drawer: false,
    }
  },
  props: {
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  watch: {
    selectedSeries (val: SeriesDto[]) {
      val.forEach(s => {
        const index = this.series.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.series.splice(index, 1, s)
        }
      })
    },
  },
  created () {
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(LIBRARY_CHANGED, this.reloadLibrary)
  },
  beforeDestroy () {
    this.$eventHub.$off(SERIES_CHANGED, this.reloadSeries)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(LIBRARY_CHANGED, this.reloadLibrary)
  },
  async mounted () {
    if (this.$cookies.isKey(cookiePageSize)) {
      this.pageSize = Number(this.$cookies.get(cookiePageSize))
    }

    // restore from query param
    this.resetParams(this.$route)
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

    this.loadLibrary(this.libraryId)

    this.setWatches()
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.unsetWatches()

      // reset
      this.resetParams(to)
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.series = []
      this.filterOptionsPanel.genre.values = []
      this.filterOptionsPanel.tag.values = []
      this.filterOptionsPanel.publisher.values = []
      this.filterOptionsPanel.language.values = []
      this.filterOptionsPanel.ageRating.values = []
      this.filterOptionsPanel.releaseDate.values = []

      this.loadLibrary(to.params.libraryId)

      this.setWatches()
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    paginationVisible (): number {
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
    sortOrFilterActive (): boolean {
      return sortOrFilterActive(this.sortActive, this.sortDefault, this.filters)
    },
  },
  methods: {
    cookieSort (libraryId: string): string {
      return `library.sort.${libraryId}`
    },
    cookieFilter (libraryId: string): string {
      return `library.filter.${libraryId}`
    },
    resetParams (route: any) {
      this.sortActive = parseQuerySort(route.query.sort, this.sortOptions) ||
        this.$cookies.get(this.cookieSort(route.params.libraryId)) ||
        this.$_.clone(this.sortDefault)

      if (route.query.status || route.query.readStatus || route.query.genre || route.query.tag || route.query.language || route.query.ageRating) {
        this.filters.status = parseQueryFilter(route.query.status, Object.keys(SeriesStatus))
        this.filters.readStatus = parseQueryFilter(route.query.readStatus, Object.keys(ReadStatus))
        this.filters.genre = parseQueryFilter(route.query.genre, this.filterOptionsPanel.genre.values.map(x => x.value))
        this.filters.tag = parseQueryFilter(route.query.tag, this.filterOptionsPanel.tag.values.map(x => x.value))
        this.filters.language = parseQueryFilter(route.query.language, this.filterOptionsPanel.language.values.map(x => x.value))
        this.filters.ageRating = parseQueryFilter(route.query.ageRating, this.filterOptionsPanel.ageRating.values.map(x => x.value))
        this.filters.releaseDate = parseQueryFilter(route.query.releaseDate, this.filterOptionsPanel.releaseDate.values.map(x => x.value))
      } else {
        this.filters = this.$cookies.get(this.cookieFilter(route.params.libraryId)) || {} as FiltersActive
      }
    },
    libraryDeleted (event: EventLibraryDeleted) {
      if (event.id === this.libraryId) {
        this.$router.push({ name: 'home' })
      } else if (this.libraryId === LIBRARIES_ALL) {
        this.loadLibrary(this.libraryId)
      }
    },
    setWatches () {
      this.sortUnwatch = this.$watch('sortActive', (val) => {
        this.$cookies.set(this.cookieSort(this.libraryId), val, Infinity)
        this.updateRouteAndReload()
      })
      this.filterUnwatch = this.$watch('filters', (val) => {
        this.$cookies.set(this.cookieFilter(this.libraryId), val, Infinity)
        this.updateRouteAndReload()
      })
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$cookies.set(cookiePageSize, val, Infinity)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.libraryId, val, this.sortActive)
      })
    },
    unsetWatches () {
      this.sortUnwatch()
      this.filterUnwatch()
      this.pageUnwatch()
      this.pageSizeUnwatch()
    },
    updateRouteAndReload () {
      this.unsetWatches()

      this.page = 1

      this.updateRoute()
      this.loadPage(this.libraryId, this.page, this.sortActive)

      this.setWatches()
    },
    reloadSeries (event: EventSeriesChanged) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.loadPage(this.libraryId, this.page, this.sortActive)
      }
    },
    reloadLibrary (event: EventLibraryChanged) {
      if (this.libraryId === LIBRARIES_ALL || event.id === this.libraryId) {
        this.loadLibrary(this.libraryId)
      }
    },
    async loadLibrary (libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)

      const requestLibraryId = libraryId !== LIBRARIES_ALL ? libraryId : undefined
      this.filterOptionsPanel.genre.values = toNameValue(await this.$komgaReferential.getGenres(requestLibraryId))
      this.filterOptionsPanel.tag.values = toNameValue(await this.$komgaReferential.getTags(requestLibraryId))
      this.filterOptionsPanel.publisher.values = toNameValue(await this.$komgaReferential.getPublishers(requestLibraryId))
      this.filterOptionsPanel.language.values = (await this.$komgaReferential.getLanguages(requestLibraryId))
      this.filterOptionsPanel.ageRating.values = toNameValue(await this.$komgaReferential.getAgeRatings(requestLibraryId))
      this.filterOptionsPanel.releaseDate.values = toNameValue(await this.$komgaReferential.getSeriesReleaseDates(requestLibraryId))

      await this.loadPage(libraryId, this.page, this.sortActive)
    },
    updateRoute () {
      const loc = {
        name: this.$route.name,
        params: { libraryId: this.$route.params.libraryId },
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
    async loadPage (libraryId: string, page: number, sort: SortActive) {
      this.selectedSeries = []

      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }

      const requestLibraryId = libraryId !== LIBRARIES_ALL ? libraryId : undefined
      const seriesPage = await this.$komgaSeries.getSeries(requestLibraryId, pageRequest, undefined, this.filters.status, this.filters.readStatus, this.filters.genre, this.filters.tag, this.filters.language, this.filters.publisher, this.filters.ageRating, this.filters.releaseDate)

      this.totalPages = seriesPage.totalPages
      this.totalElements = seriesPage.totalElements
      this.series = seriesPage.content
    },
    getLibraryLazy (libraryId: any): LibraryDto | undefined {
      if (libraryId !== 0) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
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
    editSingleSeries (series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    editMultipleSeries () {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
  },
})
</script>
<style scoped>
</style>
