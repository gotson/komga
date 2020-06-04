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

      <!--   Action menu   -->
      <v-menu offset-y>
        <template v-slot:activator="{ on }">
          <v-btn icon v-on="on">
            <v-icon>mdi-dots-vertical</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item @click="analyze()" v-if="isAdmin">
            <v-list-item-title>Analyze</v-list-item-title>
          </v-list-item>
          <v-list-item @click="refreshMetadata()" v-if="isAdmin">
            <v-list-item-title>Refresh metadata</v-list-item-title>
          </v-list-item>
          <v-list-item
            @click="markRead()">
            <v-list-item-title>Mark as read</v-list-item-title>
          </v-list-item>
          <v-list-item
            @click="markUnread()">
            <v-list-item-title>Mark as unread</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>

      <v-toolbar-title>
        <span v-if="$_.get(series, 'metadata.title')">{{ series.metadata.title }}</span>
        <badge class="ml-4" v-if="totalElements">{{ totalElements }}</badge>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="dialogEdit = true" v-if="isAdmin">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>

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
        </v-col>
      </v-row>

      <v-divider class="my-4"/>

      <v-pagination
        v-model="page"
        :total-visible="paginationVisible"
        :length="totalPages"
      />

      <item-browser :items="books" :selected.sync="selected" :edit-function="this.singleEdit" class="px-4"/>

    </v-container>

    <edit-series-dialog v-model="dialogEdit" :series.sync="series"/>
  </div>
</template>

<script lang="ts">
import Badge from '@/components/Badge.vue'
import EditBooksDialog from '@/components/EditBooksDialog.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ItemCard from '@/components/ItemCard.vue'
import PageSizeSelect from '@/components/PageSizeSelect.vue'
import SortMenuButton from '@/components/SortMenuButton.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import { parseQuerySort } from '@/functions/query-params'
import { seriesThumbnailUrl } from '@/functions/urls'
import Vue from 'vue'

const cookiePageSize = 'pagesize'

export default Vue.extend({
  name: 'BrowseSeries',
  components: {
    ToolbarSticky,
    SortMenuButton,
    Badge,
    EditSeriesDialog,
    EditBooksDialog,
    ItemBrowser,
    PageSizeSelect,
    ItemCard,
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
      dialogEdit: false,
      sortUnwatch: null as any,
      pageUnwatch: null as any,
      pageSizeUnwatch: null as any,
      selected: [],
      dialogEditBooks: false,
      dialogEditBookSingle: false,
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
      this.page = 1
      this.totalPages = 1
      this.totalElements = null
      this.books = []

      this.loadSeries(Number(to.params.seriesId))

      this.setWatches()
    }

    next()
  },
  methods: {
    setWatches () {
      this.sortUnwatch = this.$watch('sortActive', this.updateRouteAndReload)
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
      await this.loadPage(seriesId, this.page, this.sortActive)
    },
    parseQuerySortOrDefault (querySort: any): SortActive {
      return parseQuerySort(querySort, this.sortOptions) || this.$_.clone(this.sortDefault)
    },
    updateRoute (index?: string) {
      this.$router.replace({
        name: this.$route.name,
        params: { seriesId: this.$route.params.seriesId },
        query: {
          page: `${this.page}`,
          pageSize: `${this.pageSize}`,
          sort: `${this.sortActive.key},${this.sortActive.order}`,
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
      const booksPage = await this.$komgaSeries.getBooks(seriesId, pageRequest)

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
    },
    async markSelectedUnread () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
    },
    async markRead () {
      await this.$komgaSeries.markAsRead(this.seriesId)
      this.loadSeries(this.seriesId)
    },
    async markUnread () {
      await this.$komgaSeries.markAsUnread(this.seriesId)
      this.loadSeries(this.seriesId)
    },
  },
})
</script>

<style scoped>
</style>
