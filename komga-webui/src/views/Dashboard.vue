<template>
  <div :style="$vuetify.breakpoint.name === 'xs' ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="individualLibrary && selectedSeries.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : $t('common.all_libraries') }}</span>
      </v-toolbar-title>

      <v-spacer/>

      <library-navigation v-if="individualLibrary && $vuetify.breakpoint.name !== 'xs'" :libraryId="libraryId"/>

      <v-spacer/>

    </toolbar-sticky>

    <library-navigation v-if="individualLibrary && $vuetify.breakpoint.name === 'xs'" :libraryId="libraryId"
                        bottom-navigation/>

    <multi-select-bar
      v-model="selectedSeries"
      kind="series"
      @unselect-all="selectedSeries = []"
      @mark-read="markSelectedSeriesRead"
      @mark-unread="markSelectedSeriesUnread"
      @add-to-collection="addToCollection"
      @edit="editMultipleSeries"
    />

    <multi-select-bar
      v-model="selectedBooks"
      kind="books"
      @unselect-all="selectedBooks = []"
      @mark-read="markSelectedBooksRead"
      @mark-unread="markSelectedBooksUnread"
      @add-to-readlist="addToReadList"
      @edit="editMultipleBooks"
    />

    <v-container fluid>
      <empty-state v-if="allEmpty"
                   :title="$t('common.nothing_to_show')"
                   icon="mdi-help-circle"
                   icon-color="secondary"
      >
      </empty-state>

      <horizontal-scroller v-if="inProgressBooks.length !== 0" class="mb-4">
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.keep_reading') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="inProgressBooks"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller v-if="onDeckBooks.length !== 0" class="mb-4">
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.on_deck') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="onDeckBooks"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller v-if="recentlyReleasedBooks.length !== 0" class="mb-4">
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_released_books') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="recentlyReleasedBooks"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller v-if="latestBooks.length !== 0" class="mb-4">
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_added_books') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="latestBooks"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller v-if="newSeries.length !== 0" class="mb-4">
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_added_series') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="newSeries"
                        nowrap
                        :edit-function="isAdmin ? singleEditSeries : undefined"
                        :selected.sync="selectedSeries"
                        :selectable="selectedBooks.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller v-if="updatedSeries.length !== 0" class="mb-4">
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_updated_series') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="updatedSeries"
                        nowrap
                        :edit-function="isAdmin ? singleEditSeries : undefined"
                        :selected.sync="selectedSeries"
                        :selectable="selectedBooks.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>
    </v-container>
  </div>
</template>

<script lang="ts">
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import LibraryNavigation from '@/components/LibraryNavigation.vue'
import {ReadStatus} from '@/types/enum-books'
import {BookDto} from '@/types/komga-books'
import {
  BOOK_ADDED,
  BOOK_CHANGED,
  BOOK_DELETED,
  READPROGRESS_CHANGED,
  READPROGRESS_DELETED,
  SERIES_ADDED,
  SERIES_CHANGED,
  SERIES_DELETED,
} from '@/types/events'
import Vue from 'vue'
import {SeriesDto} from "@/types/komga-series"
import {LIBRARIES_ALL, LIBRARY_ROUTE} from "@/types/library"
import {throttle} from 'lodash'
import {subMonths} from 'date-fns'
import {BookSseDto, ReadProgressSseDto, SeriesSseDto} from "@/types/komga-sse"

export default Vue.extend({
  name: 'Dashboard',
  components: {
    HorizontalScroller,
    EmptyState,
    ToolbarSticky,
    LibraryNavigation,
    ItemBrowser,
    MultiSelectBar,
    LibraryActionsMenu,
  },
  data: () => {
    return {
      library: undefined as LibraryDto | undefined,
      newSeries: [] as SeriesDto[],
      updatedSeries: [] as SeriesDto[],
      latestBooks: [] as BookDto[],
      inProgressBooks: [] as BookDto[],
      onDeckBooks: [] as BookDto[],
      recentlyReleasedBooks: [] as BookDto[],
      selectedSeries: [] as SeriesDto[],
      selectedBooks: [] as BookDto[],
    }
  },
  created() {
    this.$eventHub.$on(SERIES_ADDED, this.seriesChanged)
    this.$eventHub.$on(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$on(SERIES_DELETED, this.seriesChanged)
    this.$eventHub.$on(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$on(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$on(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_DELETED, this.readProgressChanged)
  },
  beforeDestroy() {
    this.$eventHub.$off(SERIES_ADDED, this.seriesChanged)
    this.$eventHub.$off(SERIES_CHANGED, this.seriesChanged)
    this.$eventHub.$off(SERIES_DELETED, this.seriesChanged)
    this.$eventHub.$off(BOOK_ADDED, this.bookChanged)
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$off(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$off(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_DELETED, this.readProgressChanged)
  },
  mounted() {
    if (this.individualLibrary) this.$store.commit('setLibraryRoute', {
      id: this.libraryId,
      route: LIBRARY_ROUTE.RECOMMENDED,
    })
    this.reload()
  },
  props: {
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  watch: {
    libraryId(val) {
      this.loadAll(val)
    },
    '$store.state.komgaLibraries.libraries': {
      handler(val){
        if(val.length === 0) this.$router.push({name: 'welcome'})
        else this.reload()
      },
    },
  },
  computed: {
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    fixedCardWidth(): number {
      return this.$vuetify.breakpoint.name === 'xs' ? 120 : 150
    },
    allEmpty(): boolean {
      return this.newSeries.length === 0 &&
        this.updatedSeries.length === 0 &&
        this.latestBooks.length === 0 &&
        this.inProgressBooks.length === 0 &&
        this.onDeckBooks.length === 0 &&
        this.recentlyReleasedBooks.length === 0
    },
    individualLibrary(): boolean {
      return this.libraryId !== LIBRARIES_ALL
    },
  },
  methods: {
    getRequestLibraryId(libraryId: string): string | undefined {
      return libraryId !== LIBRARIES_ALL ? libraryId : undefined
    },
    seriesChanged(event: SeriesSseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.reload()
      }
    },
    bookChanged(event: BookSseDto){
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.reload()
      }
    },
    readProgressChanged(event: ReadProgressSseDto){
      if (this.inProgressBooks.some(b => b.id === event.bookId)) this.reload()
      else if (this.latestBooks.some(b => b.id === event.bookId)) this.reload()
      else if (this.onDeckBooks.some(b => b.id === event.bookId)) this.reload()
      else if (this.recentlyReleasedBooks.some(b => b.id === event.bookId)) this.reload()
    },
    reload: throttle(function(this: any) {
      this.loadAll(this.libraryId)
    }, 5000),
    loadAll(libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)
      this.selectedSeries = []
      this.selectedBooks = []
      this.loadInProgressBooks(libraryId)
      this.loadOnDeckBooks(libraryId)
      this.loadRecentlyReleasedBooks(libraryId)
      this.loadLatestBooks(libraryId)
      this.loadNewSeries(libraryId)
      this.loadUpdatedSeries(libraryId)
    },
    async loadNewSeries(libraryId: string) {
      this.newSeries = (await this.$komgaSeries.getNewSeries(this.getRequestLibraryId(libraryId))).content
    },
    async loadUpdatedSeries(libraryId: string) {
      this.updatedSeries = (await this.$komgaSeries.getUpdatedSeries(this.getRequestLibraryId(libraryId))).content
    },
    async loadLatestBooks(libraryId: string) {
      const pageRequest = {
        sort: ['createdDate,desc'],
      } as PageRequest

      this.latestBooks = (await this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageRequest)).content
    },
    async loadRecentlyReleasedBooks(libraryId: string) {
      const pageRequest = {
        sort: ['metadata.releaseDate,desc'],
      } as PageRequest

      const releasedAfter = subMonths(new Date(), 1)
      this.recentlyReleasedBooks = (await this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageRequest, undefined, undefined, undefined, releasedAfter)).content
    },
    async loadInProgressBooks(libraryId: string) {
      const pageRequest = {
        sort: ['readProgress.lastModified,desc'],
      } as PageRequest

      this.inProgressBooks = (await this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageRequest, undefined, undefined, [ReadStatus.IN_PROGRESS])).content
    },
    async loadOnDeckBooks(libraryId: string) {
      this.onDeckBooks = (await this.$komgaBooks.getBooksOnDeck(this.getRequestLibraryId(libraryId))).content
    },
    singleEditSeries(series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    singleEditBook(book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    async markSelectedSeriesRead() {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsRead(s.id),
      ))
      this.selectedSeries = []
    },
    async markSelectedSeriesUnread() {
      await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.markAsUnread(s.id),
      ))
      this.selectedSeries = []
    },
    addToCollection() {
      this.$store.dispatch('dialogAddSeriesToCollection', this.selectedSeries)
      this.selectedSeries = []
    },
    editMultipleSeries() {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    editMultipleBooks() {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    addToReadList() {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks)
      this.selectedBooks = []
    },
    async markSelectedBooksRead() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, {completed: true}),
      ))
      this.selectedBooks = []
    },
    async markSelectedBooksUnread() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = []
    },
    getLibraryLazy(libraryId: string): LibraryDto | undefined {
      if (libraryId !== LIBRARIES_ALL) {
        return this.$store.getters.getLibraryById(libraryId)
      } else {
        return undefined
      }
    },
  },
})
</script>

<style scoped>

</style>
