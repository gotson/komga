<template>
  <div>
    <toolbar-sticky v-if="selected.length === 0">
      <!--   Go back to parent library   -->
      <v-btn icon
             title="Go to library"
             :to="{name:'browse-libraries', params: {libraryId: series.libraryId ? series.libraryId : 0 }}"
      >
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>

      <series-actions-menu v-if="series"
                           :series.sync="series"
                           @add-to-collection="addToCollection"
                           @mark-read="markRead"
                           @mark-unread="markUnread"
      />

      <v-toolbar-title>
        <span v-if="$_.get(series, 'metadata.title')">{{ series.metadata.title }}</span>
        <badge class="ml-4" v-if="totalElements">{{ totalElements }}</badge>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="dialogEdit = true" v-if="isAdmin">
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

    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="selected.length > 0" :elevation="5" color="white">
        <v-btn icon @click="selected=[]">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>
          <span>{{ selected.length }} selected</span>
        </v-toolbar-title>

        <v-spacer/>

        <v-btn icon @click="markSelectedRead()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-bookmark-check</v-icon>
            </template>
            <span>Mark as Read</span>
          </v-tooltip>
        </v-btn>

        <v-btn icon @click="markSelectedUnread()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on">mdi-bookmark-remove</v-icon>
            </template>
            <span>Mark as Unread</span>
          </v-tooltip>
        </v-btn>

        <v-btn icon @click="dialogEditBooks = true" v-if="isAdmin">
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
      </toolbar-sticky>
    </v-scroll-y-transition>

    <edit-books-dialog v-model="dialogEditBooks"
                       :books.sync="selectedBooks"
    />

    <edit-books-dialog v-model="dialogEditBookSingle"
                       :books.sync="editBookSingle"
    />

    <v-container fluid class="px-6">
      <v-row>
        <v-col cols="4" sm="4" md="auto" lg="auto" xl="auto">
          <item-card
            v-if="series.hasOwnProperty('id')"
            width="212"
            :item="series"
            thumbnail-only
            no-link
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

          <v-row>
            <v-col>
              <v-expansion-panels v-model="collectionPanel">
                <v-expansion-panel v-for="(c, i) in collections"
                                   :key="i"
                >
                  <v-expansion-panel-header>{{ c.name }} collection</v-expansion-panel-header>
                  <v-expansion-panel-content>
                    <horizontal-scroller>
                      <template v-slot:prepend>
                        <router-link class="overline"
                                     :to="{name: 'browse-collection', params: {collectionId: c.id}}"
                        >Manage collection
                        </router-link>
                      </template>
                      <template v-slot:content>
                        <div v-for="(s, i) in collectionsContent[c.id]"
                             :key="i"
                        >
                          <item-card class="ma-2 card" :item="s"/>
                        </div>
                      </template>
                    </horizontal-scroller>
                  </v-expansion-panel-content>
                </v-expansion-panel>
              </v-expansion-panels>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-divider class="my-4"/>

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
          v-model="page"
          :total-visible="paginationVisible"
          :length="totalPages"
        />

        <item-browser :items="books" :selected.sync="selected" :edit-function="this.singleEdit" class="px-4"/>
      </template>

    </v-container>

    <edit-series-dialog v-model="dialogEdit" :series.sync="series"/>

    <collection-add-to-dialog v-model="dialogAddToCollection"
                              :series="series"
    />
  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import CollectionAddToDialog from '@/components/CollectionAddToDialog.vue'
import EditBooksDialog from '@/components/EditBooksDialog.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterMenuButton from '@/components/FilterMenuButton.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ItemCard from '@/components/ItemCard.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import SeriesActionsMenu from '@/components/SeriesActionsMenu.vue'
import SortMenuButton from '@/components/SortMenuButton.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import { parseQueryFilter, parseQuerySort } from '@/functions/query-params'
import { seriesThumbnailUrl } from '@/functions/urls'
import { ReadStatus } from '@/types/enum-books'
import Vue from 'vue'

const cookiePageSize = 'pagesize'

export default Vue.extend({
  name: 'BrowseSeries',
  components: {
    ToolbarSticky,
    SortMenuButton,
    FilterMenuButton,
    Badge,
    EditSeriesDialog,
    EditBooksDialog,
    ItemBrowser,
    PageSizeSelect,
    SeriesActionsMenu,
    CollectionAddToDialog,
    HorizontalScroller,
    ItemCard,
    EmptyState,
  },
  data: () => {
    return {
      series: {} as SeriesDto,
      books: [] as BookDto[],
      selectedBooks: [] as BookDto[],
      editBookSingle: {} as BookDto,
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
      filterOptions: [{ values: [ReadStatus.UNREAD] }],
      filters: [[]] as any[],
      dialogEdit: false,
      sortUnwatch: null as any,
      filterUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      selected: [],
      dialogEditBooks: false,
      dialogEditBookSingle: false,
      dialogAddToCollection: false,
      collections: [] as CollectionDto[],
      collectionsContent: [] as any[][],
      collectionPanel: -1,
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
      type: Number,
      required: true,
    },
  },
  watch: {
    series (val) {
      if (this.$_.has(val, 'metadata.title')) {
        document.title = `Komga - ${val.metadata.title}`
      }
    },
    selected (val: number[]) {
      this.selectedBooks = val.map(id => this.books.find(s => s.id === id))
        .filter(x => x !== undefined) as BookDto[]
    },
    selectedBooks (val: BookDto[]) {
      val.forEach(s => {
        const index = this.books.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.books.splice(index, 1, s)
        }
      })
    },
    editBookSingle (val: BookDto) {
      const index = this.books.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.books.splice(index, 1, val)
      }
    },
  },
  mounted () {
    if (this.$cookies.isKey(cookiePageSize)) {
      this.pageSize = Number(this.$cookies.get(cookiePageSize))
    }

    // restore from query param
    this.sortActive = this.parseQuerySortOrDefault(this.$route.query.sort)
    this.filters.splice(0, 1, parseQueryFilter(this.$route.query.readStatus, ReadStatus))
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
      this.filters.splice(0, 1, parseQueryFilter(to.query.readStatus, ReadStatus))
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.books = []
      this.collections = []
      this.collectionPanel = -1

      this.loadSeries(Number(to.params.seriesId))

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

      this.selected = []
      this.page = 1

      this.updateRoute()
      this.loadPage(this.seriesId, this.page, this.sortActive)

      this.setWatches()
    },
    async loadSeries (seriesId: number) {
      this.series = await this.$komgaSeries.getOneSeries(seriesId)
      this.collections = await this.$komgaSeries.getCollections(seriesId)
      for (const c of this.collections) {
        this.collectionsContent[c.id] = await this.$komgaCollections.getSeries(c.id)
      }
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
          readStatus: `${this.filters[0]}`,
        },
      }).catch(_ => {
      })
    },
    async loadPage (seriesId: number, page: number, sort: SortActive) {
      const pageRequest = {
        page: page - 1,
        size: this.pageSize,
      } as PageRequest

      if (sort) {
        pageRequest.sort = [`${sort.key},${sort.order}`]
      }
      const booksPage = await this.$komgaSeries.getBooks(seriesId, pageRequest, this.filters[0])

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
    singleEdit (book: BookDto) {
      this.editBookSingle = book
      this.dialogEditBookSingle = true
    },
    async markSelectedRead () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, { completed: true }),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
      this.loadSeries(this.seriesId)
    },
    async markSelectedUnread () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
      this.loadSeries(this.seriesId)
    },
    addToCollection () {
      this.dialogAddToCollection = true
    },
    async markRead () {
      this.loadSeries(this.seriesId)
    },
    async markUnread () {
      this.loadSeries(this.seriesId)
    },
  },
})
</script>

<style scoped>
</style>
