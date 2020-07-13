<template>
  <div>
    <toolbar-sticky v-if="selectedBooks.length === 0">
      <!--   Go back to parent library   -->
      <v-btn icon
             title="Go to library"
             :to="{name:'browse-libraries', params: {libraryId: series.libraryId ? series.libraryId : 0 }}"
      >
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>

      <series-actions-menu v-if="series"
                           :series="series"
      />

      <v-toolbar-title>
        <span v-if="$_.get(series, 'metadata.title')">{{ series.metadata.title }}</span>
        <badge class="ml-4" v-if="totalElements">{{ totalElements }}</badge>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="editSeries" v-if="isAdmin">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>

      <!--   Filter menu   -->
      <filter-menu-button :filters-options="filterOptions"
                          :filters-active.sync="filters"
      />

      <!--   Sort menu   -->
      <sort-menu-button :sort-default="sortDefault"
                        :sort-options="sortOptions"
                        :sort-active.sync="sortActive"
      />

      <page-size-select v-model="pageSize"/>

    </toolbar-sticky>

    <books-multi-select-bar
      v-model="selectedBooks"
      @unselect-all="selectedBooks = []"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @edit="editMultipleBooks"
    />

    <v-container fluid>
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <item-card
            v-if="series.hasOwnProperty('id')"
            width="212"
            :item="series"
            thumbnail-only
            no-link
            :action-menu="false"
          ></item-card>

        </v-col>
        <v-col cols="8">
          <v-row>
            <v-col>
              <div class="headline" v-if="$_.get(series, 'metadata.title')">{{ series.metadata.title }}</div>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="auto" class="body-2">STATUS</v-col>
            <v-col cols="auto" class="body-2 text-capitalize" v-if="series.metadata">{{
              series.metadata.status.toLowerCase() }}
            </v-col>
          </v-row>

          <v-row v-if="$vuetify.breakpoint.name !== 'xs'">
            <v-col>
              <collections-expansion-panels :collections="collections"/>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row v-if="$vuetify.breakpoint.name === 'xs'">
        <v-col class="pt-0 py-1">
          <collections-expansion-panels :collections="collections"/>
        </v-col>
      </v-row>

      <v-divider class="my-1"/>

      <empty-state
        v-if="totalPages === 0"
        title="The active filter has no matches"
        sub-title="Use the menu above to change the active filter"
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

        <item-browser :items="books"
                      :selected.sync="selectedBooks"
                      :edit-function="editSingleBook"
        />
      </template>

    </v-container>

  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import BooksMultiSelectBar from '@/components/bars/BooksMultiSelectBar.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import CollectionsExpansionPanels from '@/components/CollectionsExpansionPanels.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterMenuButton from '@/components/FilterMenuButton.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ItemCard from '@/components/ItemCard.vue'
import SeriesActionsMenu from '@/components/menus/SeriesActionsMenu.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import SortMenuButton from '@/components/SortMenuButton.vue'
import { parseQueryFilter, parseQuerySort } from '@/functions/query-params'
import { seriesThumbnailUrl } from '@/functions/urls'
import { ReadStatus } from '@/types/enum-books'
import { BOOK_CHANGED, LIBRARY_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'

const cookiePageSize = 'pagesize'

export default Vue.extend({
  name: 'BrowseSeries',
  components: {
    ToolbarSticky,
    SortMenuButton,
    FilterMenuButton,
    Badge,
    ItemBrowser,
    PageSizeSelect,
    SeriesActionsMenu,
    ItemCard,
    EmptyState,
    BooksMultiSelectBar,
    CollectionsExpansionPanels,
  },
  data: () => {
    return {
      series: {} as SeriesDto,
      books: [] as BookDto[],
      selectedBooks: [] as BookDto[],
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalElements: null as number | null,
      sortOptions: [{ name: 'Number', key: 'metadata.numberSort' }, { name: 'Date added', key: 'createdDate' }, {
        name: 'File size',
        key: 'fileSize',
      }] as SortOption[],
      sortActive: {} as SortActive,
      sortDefault: { key: 'metadata.numberSort', order: 'asc' } as SortActive,
      filterOptions: {
        readStatus: {
          values: [ReadStatus.UNREAD],
        },
      } as FiltersOptions,
      filters: { readStatus: [] } as FiltersActive,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      collections: [] as CollectionDto[],
    }
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    thumbnailUrl (): string {
      return seriesThumbnailUrl(this.seriesId)
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
  },
  props: {
    seriesId: {
      type: String,
      required: true,
    },
  },
  watch: {
    series (val) {
      if (this.$_.has(val, 'metadata.title')) {
        document.title = `Komga - ${val.metadata.title}`
      }
    },
  },
  created () {
    this.$eventHub.$on(SERIES_CHANGED, this.reloadSeries)
    this.$eventHub.$on(BOOK_CHANGED, this.reloadBooks)
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
  },
  beforeDestroy () {
    this.$eventHub.$off(SERIES_CHANGED, this.reloadSeries)
    this.$eventHub.$off(BOOK_CHANGED, this.reloadBooks)
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
  },
  mounted () {
    if (this.$cookies.isKey(cookiePageSize)) {
      this.pageSize = Number(this.$cookies.get(cookiePageSize))
    }

    // restore from query param
    this.sortActive = this.parseQuerySortOrDefault(this.$route.query.sort)
    this.filters.readStatus = parseQueryFilter(this.$route.query.readStatus, ReadStatus)
    if (this.$route.query.page) this.page = Number(this.$route.query.page)
    if (this.$route.query.pageSize) this.pageSize = Number(this.$route.query.pageSize)

    this.loadSeries(this.seriesId)

    this.setWatches()
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.seriesId !== from.params.seriesId) {
      this.unsetWatches()

      // reset
      this.sortActive = this.parseQuerySortOrDefault(to.query.sort)
      this.filters.readStatus = parseQueryFilter(to.query.readStatus, ReadStatus)
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.books = []
      this.collections = []

      this.loadSeries(to.params.seriesId)

      this.setWatches()
    }

    next()
  },
  methods: {
    setWatches () {
      this.sortUnwatch = this.$watch('sortActive', this.updateRouteAndReload)
      this.filterUnwatch = this.$watch('filters', this.updateRouteAndReload)
      this.pageSizeUnwatch = this.$watch('pageSize', (val) => {
        this.$cookies.set(cookiePageSize, val, Infinity)
        this.updateRouteAndReload()
      })

      this.pageUnwatch = this.$watch('page', (val) => {
        this.updateRoute()
        this.loadPage(this.seriesId, val, this.sortActive)
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

      this.selectedBooks = []
      this.page = 1

      this.updateRoute()
      this.loadPage(this.seriesId, this.page, this.sortActive)

      this.setWatches()
    },
    libraryDeleted (event: EventLibraryDeleted) {
      if (event.id === this.series.libraryId) {
        this.$router.push({ name: 'home' })
      }
    },
    reloadSeries (event: EventSeriesChanged) {
      if (event.id === this.seriesId) this.loadSeries(this.seriesId)
    },
    reloadBooks (event: EventBookChanged) {
      if (event.seriesId === this.seriesId) this.loadSeries(this.seriesId)
    },
    async loadSeries (seriesId: string) {
      this.series = await this.$komgaSeries.getOneSeries(seriesId)
      this.collections = await this.$komgaSeries.getCollections(seriesId)
      await this.loadPage(seriesId, this.page, this.sortActive)
    },
    parseQuerySortOrDefault (querySort: any): SortActive {
      return parseQuerySort(querySort, this.sortOptions) || this.$_.clone(this.sortDefault)
    },
    parseQueryFilterStatus (queryStatus: any): string[] {
      return queryStatus ? queryStatus.toString().split(',').filter((x: string) => Object.keys(ReadStatus).includes(x)) : []
    },
    updateRoute (index?: string) {
      this.$router.replace({
        name: this.$route.name,
        params: { seriesId: this.$route.params.seriesId },
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
          sort: `${this.sortActive.key},${this.sortActive.order}`,
          readStatus: `${this.filters.readStatus}`,
        },
      }).catch(_ => {
      })
    },
    async loadPage (seriesId: string, page: number, sort: SortActive) {
      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }
      const booksPage = await this.$komgaSeries.getBooks(seriesId, pageRequest, this.filters.readStatus)

      this.totalPages = booksPage.totalPages
      this.totalElements = booksPage.totalElements
      this.books = booksPage.content
    },
    analyze () {
      this.$komgaSeries.analyzeSeries(this.series)
    },
    refreshMetadata () {
      this.$komgaSeries.refreshMetadata(this.series)
    },
    editSeries () {
      this.$store.dispatch('dialogUpdateSeries', this.series)
    },
    editSingleBook (book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    editMultipleBooks () {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    async markSelectedRead () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, { completed: true }),
      ))
      await this.loadSeries(this.seriesId)
    },
    async markSelectedUnread () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      await this.loadSeries(this.seriesId)
    },
  },
})
</script>

<style scoped>
</style>
