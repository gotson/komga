<template>
  <div>
    <toolbar-sticky v-if="showToolbar">
      <v-toolbar-title>
        <span>Search results for "{{ $route.query.q }}"</span>
      </v-toolbar-title>
    </toolbar-sticky>

    <series-multi-select-bar
      v-model="selectedSeries"
      @unselect-all="selectedSeries = []"
      @mark-read="markSelectedSeriesRead"
      @mark-unread="markSelectedSeriesUnread"
      @add-to-collection="addToCollection"
      @edit="editMultipleSeries"
    />

    <books-multi-select-bar
      v-model="selectedBooks"
      @unselect-all="selectedBooks = []"
      @mark-read="markSelectedBooksRead"
      @mark-unread="markSelectedBooksUnread"
      @add-to-readlist="addToReadList"
      @edit="editMultipleBooks"
    />

    <v-container fluid>
      <empty-state
        v-if="emptyResults"
        title="The search returned no results"
        sub-title="Try searching for something else"
        icon="mdi-magnify"
        icon-color="secondary"
        class="my-4"
      >
      </empty-state>

      <template v-else>
        <horizontal-scroller v-if="series.length !== 0" class="mb-4">
          <template v-slot:prepend>
            <div class="title">Series</div>
          </template>
          <template v-slot:content>
            <item-browser :items="series"
                          nowrap
                          :edit-function="singleEditSeries"
                          :selected.sync="selectedSeries"
                          :selectable="selectedBooks.length === 0"
                          :fixed-item-width="fixedCardWidth"
            />
          </template>
        </horizontal-scroller>

        <horizontal-scroller v-if="books.length !== 0" class="mb-4">
          <template v-slot:prepend>
            <div class="title">Books</div>
          </template>
          <template v-slot:content>
            <item-browser :items="books"
                          nowrap
                          :edit-function="singleEditBook"
                          :selected.sync="selectedBooks"
                          :selectable="selectedSeries.length === 0"
                          :fixed-item-width="fixedCardWidth"
            />
          </template>
        </horizontal-scroller>

        <horizontal-scroller v-if="collections.length !== 0" class="mb-4">
          <template v-slot:prepend>
            <div class="title">Collections</div>
          </template>
          <template v-slot:content>
            <item-browser :items="collections"
                          nowrap
                          :edit-function="singleEditCollection"
                          :selectable="false"
                          :fixed-item-width="fixedCardWidth"
            />
          </template>
        </horizontal-scroller>

        <horizontal-scroller v-if="readLists.length !== 0" class="mb-4">
          <template v-slot:prepend>
            <div class="title">Read Lists</div>
          </template>
          <template v-slot:content>
            <item-browser :items="readLists"
                          nowrap
                          :edit-function="singleEditReadList"
                          :selectable="false"
                          :fixed-item-width="fixedCardWidth"
            />
          </template>
        </horizontal-scroller>

      </template>
    </v-container>

  </div>
</template>

<script lang="ts">
import BooksMultiSelectBar from '@/components/bars/BooksMultiSelectBar.vue'
import SeriesMultiSelectBar from '@/components/bars/SeriesMultiSelectBar.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import EmptyState from '@/components/EmptyState.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import {
  BOOK_CHANGED,
  COLLECTION_CHANGED,
  COLLECTION_DELETED,
  LIBRARY_DELETED,
  READLIST_CHANGED,
  READLIST_DELETED,
  SERIES_CHANGED,
} from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Search',
  components: {
    EmptyState,
    ToolbarSticky,
    HorizontalScroller,
    ItemBrowser,
    BooksMultiSelectBar,
    SeriesMultiSelectBar,
  },
  data: () => {
    return {
      series: [] as SeriesDto[],
      books: [] as BookDto[],
      collections: [] as CollectionDto[],
      readLists: [] as ReadListDto[],
      pageSize: 50,
      loading: false,
      selectedSeries: [] as SeriesDto[],
      selectedBooks: [] as BookDto[],
    }
  },
  created () {
    this.$eventHub.$on(LIBRARY_DELETED, this.reloadResults)
    this.$eventHub.$on(SERIES_CHANGED, this.reloadResults)
    this.$eventHub.$on(BOOK_CHANGED, this.reloadResults)
    this.$eventHub.$on(COLLECTION_CHANGED, this.reloadResults)
    this.$eventHub.$on(COLLECTION_DELETED, this.reloadResults)
    this.$eventHub.$on(READLIST_CHANGED, this.reloadResults)
    this.$eventHub.$on(READLIST_DELETED, this.reloadResults)
  },
  beforeDestroy () {
    this.$eventHub.$off(LIBRARY_DELETED, this.reloadResults)
    this.$eventHub.$off(SERIES_CHANGED, this.reloadResults)
    this.$eventHub.$off(BOOK_CHANGED, this.reloadResults)
    this.$eventHub.$off(COLLECTION_CHANGED, this.reloadResults)
    this.$eventHub.$off(COLLECTION_DELETED, this.reloadResults)
    this.$eventHub.$off(READLIST_CHANGED, this.reloadResults)
    this.$eventHub.$off(READLIST_DELETED, this.reloadResults)
  },
  watch: {
    '$route.query.q': {
      handler: function (val) {
        this.loadResults(val)
        this.selectedBooks = []
        this.selectedSeries = []
      },
      deep: true,
      immediate: true,
    },
    selectedSeries (val: SeriesDto[]) {
      val.forEach(s => {
        const index = this.series.findIndex(x => x.id === s.id)
        if (index !== -1) {
          this.series.splice(index, 1, s)
        }
      })
    },
    selectedBooks (val: BookDto[]) {
      val.forEach(b => {
        const index = this.books.findIndex(x => x.id === b.id)
        if (index !== -1) {
          this.books.splice(index, 1, b)
        }
      })
    },
  },
  computed: {
    fixedCardWidth (): number {
      return this.$vuetify.breakpoint.name === 'xs' ? 120 : 150
    },
    showToolbar (): boolean {
      return this.selectedSeries.length === 0 && this.selectedBooks.length === 0
    },
    emptyResults (): boolean {
      return !this.loading && this.series.length === 0 && this.books.length === 0 && this.collections.length === 0 && this.readLists.length === 0
    },
  },
  methods: {
    singleEditSeries (series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    singleEditBook (book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    singleEditCollection (collection: CollectionDto) {
      this.$store.dispatch('dialogEditCollection', collection)
    },
    singleEditReadList (readList: ReadListDto) {
      this.$store.dispatch('dialogEditReadList', readList)
    },
    async markSelectedSeriesRead () {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsRead(s.id),
      ))
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    async markSelectedSeriesUnread () {
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
    addToReadList () {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks)
    },
    editMultipleSeries () {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    editMultipleBooks () {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    async markSelectedBooksRead () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, { completed: true }),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
    },
    async markSelectedBooksUnread () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
    },
    reloadResults () {
      this.loadResults(this.$route.query.q.toString())
    },
    async loadResults (search: string) {
      this.selectedBooks = []
      this.selectedSeries = []
      if (search) {
        this.loading = true

        this.series = (await this.$komgaSeries.getSeries(undefined, { size: this.pageSize }, search)).content
        this.books = (await this.$komgaBooks.getBooks(undefined, { size: this.pageSize }, search)).content
        this.collections = (await this.$komgaCollections.getCollections(undefined, { size: this.pageSize }, search)).content
        this.readLists = (await this.$komgaReadLists.getReadLists(undefined, { size: this.pageSize }, search)).content

        this.loading = false
      } else {
        this.series = []
        this.books = []
        this.collections = []
        this.readLists = []
      }
    },
  },
})
</script>
<style scoped>
</style>
