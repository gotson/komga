<template>
  <div :style="$vuetify.breakpoint.xs ? 'margin-bottom: 56px' : undefined">
    <toolbar-sticky v-if="individualLibrary && selectedSeries.length === 0 && selectedBooks.length === 0">
      <!--   Action menu   -->
      <library-actions-menu v-if="library"
                            :library="library"/>

      <v-toolbar-title>
        <span>{{ library ? library.name : $t('common.all_libraries') }}</span>
      </v-toolbar-title>

      <v-spacer/>

      <library-navigation v-if="individualLibrary && $vuetify.breakpoint.mdAndUp" :libraryId="libraryId"/>

      <v-spacer/>

    </toolbar-sticky>

    <library-navigation v-if="individualLibrary && $vuetify.breakpoint.smAndDown" :libraryId="libraryId"
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
      @bulk-edit="bulkEditMultipleBooks"
    />

    <v-container fluid>
      <empty-state v-if="allEmpty && !loading"
                   :title="$t('common.nothing_to_show')"
                   icon="mdi-help-circle"
                   icon-color="secondary"
      >
      </empty-state>

      <horizontal-scroller
        v-if="loaderInProgressBooks && loaderInProgressBooks.items.length !== 0"
        class="mb-4"
        :tick="loaderInProgressBooks.tick"
        @scroll-changed="(percent) => scrollChanged(loaderInProgressBooks, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.keep_reading') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderInProgressBooks.items"
                        :item-context="[ItemContext.SHOW_SERIES]"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller
        v-if="loaderOnDeckBooks && loaderOnDeckBooks.items.length !== 0"
        class="mb-4"
        :tick="loaderOnDeckBooks.tick"
        @scroll-changed="(percent) => scrollChanged(loaderOnDeckBooks, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.on_deck') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderOnDeckBooks.items"
                        :item-context="[ItemContext.SHOW_SERIES]"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller
        v-if="loaderRecentlyReleasedBooks && loaderRecentlyReleasedBooks.items.length !== 0"
        class="mb-4"
        :tick="loaderRecentlyReleasedBooks.tick"
        @scroll-changed="(percent) => scrollChanged(loaderRecentlyReleasedBooks, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_released_books') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderRecentlyReleasedBooks.items"
                        :item-context="[ItemContext.RELEASE_DATE, ItemContext.SHOW_SERIES]"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller
        v-if="loaderLatestBooks && loaderLatestBooks.items.length !== 0"
        class="mb-4"
        :tick="loaderLatestBooks.tick"
        @scroll-changed="(percent) => scrollChanged(loaderLatestBooks, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_added_books') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderLatestBooks.items"
                        :item-context="[ItemContext.SHOW_SERIES]"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller
        v-if="loaderNewSeries && loaderNewSeries.items.length !== 0"
        class="mb-4"
        :tick="loaderNewSeries.tick"
        @scroll-changed="(percent) => scrollChanged(loaderNewSeries, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_added_series') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderNewSeries.items"
                        nowrap
                        :edit-function="isAdmin ? singleEditSeries : undefined"
                        :selected.sync="selectedSeries"
                        :selectable="selectedBooks.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller
        v-if="loaderUpdatedSeries && loaderUpdatedSeries.items.length !== 0"
        class="mb-4"
        :tick="loaderUpdatedSeries.tick"
        @scroll-changed="(percent) => scrollChanged(loaderUpdatedSeries, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_updated_series') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderUpdatedSeries.items"
                        nowrap
                        :edit-function="isAdmin ? singleEditSeries : undefined"
                        :selected.sync="selectedSeries"
                        :selectable="selectedBooks.length === 0"
                        :fixed-item-width="fixedCardWidth"
          />
        </template>
      </horizontal-scroller>

      <horizontal-scroller
        v-if="loaderRecentlyReadBooks && loaderRecentlyReadBooks.items.length !== 0"
        class="mb-4"
        :tick="loaderRecentlyReadBooks.tick"
        @scroll-changed="(percent) => scrollChanged(loaderRecentlyReadBooks, percent)"
      >
        <template v-slot:prepend>
          <div class="title">{{ $t('dashboard.recently_read_books') }}</div>
        </template>
        <template v-slot:content>
          <item-browser :items="loaderRecentlyReadBooks.items"
                        :item-context="[ItemContext.SHOW_SERIES, ItemContext.READ_DATE]"
                        nowrap
                        :edit-function="isAdmin ? singleEditBook : undefined"
                        :selected.sync="selectedBooks"
                        :selectable="selectedSeries.length === 0"
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
  READPROGRESS_SERIES_CHANGED,
  READPROGRESS_SERIES_DELETED,
  SERIES_ADDED,
  SERIES_CHANGED,
  SERIES_DELETED,
} from '@/types/events'
import Vue from 'vue'
import {SeriesDto} from '@/types/komga-series'
import {LIBRARIES_ALL, LIBRARY_ROUTE} from '@/types/library'
import {throttle} from 'lodash'
import {subMonths} from 'date-fns'
import {BookSseDto, ReadProgressSeriesSseDto, ReadProgressSseDto, SeriesSseDto} from '@/types/komga-sse'
import {LibraryDto} from '@/types/komga-libraries'
import {PageLoader} from '@/types/pageLoader'
import {ItemContext} from '@/types/items'

export default Vue.extend({
  name: 'DashboardView',
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
      ItemContext,
      loading: false,
      library: undefined as LibraryDto | undefined,
      loaderNewSeries: undefined as unknown as PageLoader<SeriesDto>,
      loaderUpdatedSeries: undefined as unknown as PageLoader<SeriesDto>,
      loaderLatestBooks: undefined as unknown as PageLoader<BookDto>,
      loaderInProgressBooks: undefined as unknown as PageLoader<BookDto>,
      loaderOnDeckBooks: undefined as unknown as PageLoader<BookDto>,
      loaderRecentlyReleasedBooks: undefined as unknown as PageLoader<BookDto>,
      loaderRecentlyReadBooks: undefined as unknown as PageLoader<BookDto>,
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
    this.$eventHub.$on(READPROGRESS_SERIES_CHANGED, this.readProgressSeriesChanged)
    this.$eventHub.$on(READPROGRESS_SERIES_DELETED, this.readProgressSeriesChanged)
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
    this.$eventHub.$off(READPROGRESS_SERIES_CHANGED, this.readProgressSeriesChanged)
    this.$eventHub.$off(READPROGRESS_SERIES_DELETED, this.readProgressSeriesChanged)
  },
  mounted() {
    if (this.individualLibrary) this.$store.commit('setLibraryRoute', {
      id: this.libraryId,
      route: LIBRARY_ROUTE.RECOMMENDED,
    })
    this.setupLoaders(this.libraryId)
    this.loadAll(this.libraryId)
  },
  props: {
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  watch: {
    libraryId(val) {
      this.setupLoaders(val)
      this.loadAll(val)
    },
    '$store.state.komgaLibraries.libraries': {
      handler(val) {
        if (val.length === 0) this.$router.push({name: 'welcome'})
        else this.reload()
      },
    },
  },
  computed: {
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    fixedCardWidth(): number {
      return this.$vuetify.breakpoint.xs ? 120 : 150
    },
    allEmpty(): boolean {
      return this.loaderNewSeries?.items.length === 0 &&
        this.loaderUpdatedSeries?.items.length === 0 &&
        this.loaderLatestBooks?.items.length === 0 &&
        this.loaderInProgressBooks?.items.length === 0 &&
        this.loaderOnDeckBooks?.items.length === 0 &&
        this.loaderRecentlyReleasedBooks?.items.length === 0 &&
        this.loaderRecentlyReadBooks?.items.length === 0
    },
    individualLibrary(): boolean {
      return this.libraryId !== LIBRARIES_ALL
    },
  },
  methods: {
    async scrollChanged(loader: PageLoader<any>, percent: number) {
      if (percent > 0.95) await loader.loadNext()
    },
    getRequestLibraryId(libraryId: string): string | undefined {
      return libraryId !== LIBRARIES_ALL ? libraryId : undefined
    },
    seriesChanged(event: SeriesSseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.reload()
      }
    },
    bookChanged(event: BookSseDto) {
      if (this.libraryId === LIBRARIES_ALL || event.libraryId === this.libraryId) {
        this.reload()
      }
    },
    readProgressChanged(event: ReadProgressSseDto) {
      if (this.loaderInProgressBooks?.items.some(b => b.id === event.bookId)) this.reload()
      else if (this.loaderLatestBooks?.items.some(b => b.id === event.bookId)) this.reload()
      else if (this.loaderOnDeckBooks?.items.some(b => b.id === event.bookId)) this.reload()
      else if (this.loaderRecentlyReleasedBooks?.items.some(b => b.id === event.bookId)) this.reload()
      else if (this.loaderRecentlyReadBooks?.items.some(b => b.id === event.bookId)) this.reload()
    },
    readProgressSeriesChanged(event: ReadProgressSeriesSseDto) {
      if (this.loaderUpdatedSeries?.items.some(s => s.id === event.seriesId)) this.reload()
      else if (this.loaderNewSeries?.items.some(s => s.id === event.seriesId)) this.reload()
    },
    reload: throttle(function (this: any) {
      this.loadAll(this.libraryId, true)
    }, 5000),
    setupLoaders(libraryId: string) {
      this.loaderInProgressBooks = new PageLoader<BookDto>(
        {sort: ['readProgress.readDate,desc']},
        (pageable: PageRequest) => this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageable, undefined, undefined, [ReadStatus.IN_PROGRESS]),
      )
      this.loaderOnDeckBooks = new PageLoader<BookDto>(
        {},
        (pageable: PageRequest) => this.$komgaBooks.getBooksOnDeck(this.getRequestLibraryId(libraryId), pageable),
      )
      this.loaderLatestBooks = new PageLoader<BookDto>(
        {sort: ['createdDate,desc']},
        (pageable: PageRequest) => this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageable),
      )
      this.loaderRecentlyReleasedBooks = new PageLoader<BookDto>(
        {sort: ['metadata.releaseDate,desc']},
        (pageable: PageRequest) => this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageable, undefined, undefined, undefined, subMonths(new Date(), 1)),
      )
      this.loaderRecentlyReadBooks = new PageLoader<BookDto>(
        {sort: ['readProgress.readDate,desc']},
        (pageable: PageRequest) => this.$komgaBooks.getBooks(this.getRequestLibraryId(libraryId), pageable, undefined, undefined, [ReadStatus.READ]),
      )

      this.loaderNewSeries = new PageLoader<SeriesDto>(
        {},
        (pageable: PageRequest) => this.$komgaSeries.getNewSeries(this.getRequestLibraryId(libraryId), pageable),
      )
      this.loaderUpdatedSeries = new PageLoader<SeriesDto>(
        {},
        (pageable: PageRequest) => this.$komgaSeries.getUpdatedSeries(this.getRequestLibraryId(libraryId), pageable),
      )
    },
    loadAll(libraryId: string, reload: boolean = false) {
      this.loading = true
      this.library = this.getLibraryLazy(libraryId)
      this.selectedSeries = []
      this.selectedBooks = []

      if (reload) {
        Promise.all([
          this.loaderInProgressBooks.reload(),
          this.loaderOnDeckBooks.reload(),
          this.loaderRecentlyReleasedBooks.reload(),
          this.loaderLatestBooks.reload(),
          this.loaderNewSeries.reload(),
          this.loaderUpdatedSeries.reload(),
          this.loaderRecentlyReadBooks.reload(),
        ]).then(() => {
          this.loading = false
        })
      } else {
        Promise.all([
          this.loaderInProgressBooks.loadNext(),
          this.loaderOnDeckBooks.loadNext(),
          this.loaderRecentlyReleasedBooks.loadNext(),
          this.loaderLatestBooks.loadNext(),
          this.loaderNewSeries.loadNext(),
          this.loaderUpdatedSeries.loadNext(),
          this.loaderRecentlyReadBooks.loadNext(),
        ]).then(() => {
          this.loading = false
        })
      }
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
    bulkEditMultipleBooks() {
      this.$store.dispatch('dialogUpdateBulkBooks', this.selectedBooks)
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
