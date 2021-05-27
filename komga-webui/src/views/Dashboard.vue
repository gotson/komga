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

    <library-navigation v-if="individualLibrary && $vuetify.breakpoint.name === 'xs'" :libraryId="libraryId" bottom-navigation/>

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
                        :edit-function="singleEditBook"
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
                        :edit-function="singleEditBook"
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
                        :edit-function="singleEditBook"
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
                        :edit-function="singleEditSeries"
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
                        :edit-function="singleEditSeries"
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
import BooksMultiSelectBar from '@/components/bars/BooksMultiSelectBar.vue'
import SeriesMultiSelectBar from '@/components/bars/SeriesMultiSelectBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import LibraryNavigation from '@/components/LibraryNavigation.vue'
import {ReadStatus} from '@/types/enum-books'
import {BookDto} from '@/types/komga-books'
import {BOOK_CHANGED, LIBRARY_DELETED, SERIES_CHANGED} from '@/types/events'
import Vue from 'vue'
import {SeriesDto} from "@/types/komga-series";
import {LIBRARIES_ALL, LIBRARY_ROUTE} from "@/types/library";

export default Vue.extend({
  name: 'Dashboard',
  components: {
    HorizontalScroller,
    EmptyState,
    ToolbarSticky,
    LibraryNavigation,
    ItemBrowser,
    BooksMultiSelectBar,
    SeriesMultiSelectBar,
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
      selectedSeries: [] as SeriesDto[],
      selectedBooks: [] as BookDto[],
    }
  },
  created() {
    this.$eventHub.$on(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$on(SERIES_CHANGED, this.reload)
    this.$eventHub.$on(BOOK_CHANGED, this.reload)
  },
  beforeDestroy() {
    this.$eventHub.$off(LIBRARY_DELETED, this.libraryDeleted)
    this.$eventHub.$off(SERIES_CHANGED, this.reload)
    this.$eventHub.$off(BOOK_CHANGED, this.reload)
  },
  mounted() {
    if(this.individualLibrary) this.$store.commit('setLibraryRoute', {id: this.libraryId, route: LIBRARY_ROUTE.RECOMMENDED})
    this.reload()
  },
  props: {
    libraryId: {
      type: String,
      default: LIBRARIES_ALL,
    },
  },
  watch: {
    selectedSeries(val: SeriesDto[]) {
      val.forEach(i => this.replaceSeries(i))
    },
    selectedBooks(val: BookDto[]) {
      val.forEach(i => this.replaceBook(i))
    },
    libraryId(val) {
      this.loadAll(val)
    },
  },
  computed: {
    fixedCardWidth(): number {
      return this.$vuetify.breakpoint.name === 'xs' ? 120 : 150
    },
    allEmpty(): boolean {
      return this.newSeries.length === 0 &&
        this.updatedSeries.length === 0 &&
        this.latestBooks.length === 0 &&
        this.inProgressBooks.length === 0 &&
        this.onDeckBooks.length === 0
    },
    individualLibrary(): boolean {
      return this.libraryId !== LIBRARIES_ALL
    },
  },
  methods: {
    getRequestLibraryId(libraryId: string): string | undefined {
      return libraryId !== LIBRARIES_ALL ? libraryId : undefined
    },
    libraryDeleted() {
      if (this.$store.state.komgaLibraries.libraries.length === 0) {
        this.$router.push({name: 'welcome'})
      } else {
        this.reload()
      }
    },
    reload() {
      this.loadAll(this.libraryId)
    },
    loadAll(libraryId: string) {
      this.library = this.getLibraryLazy(libraryId)
      this.selectedSeries = []
      this.selectedBooks = []
      this.loadInProgressBooks(libraryId)
      this.loadOnDeckBooks(libraryId)
      this.loadLatestBooks(libraryId)
      this.loadNewSeries(libraryId)
      this.loadUpdatedSeries(libraryId)
    },
    replaceSeries(series: SeriesDto) {
      let index = this.newSeries.findIndex(x => x.id === series.id)
      if (index !== -1) {
        this.newSeries.splice(index, 1, series)
      }
      index = this.updatedSeries.findIndex(x => x.id === series.id)
      if (index !== -1) {
        this.updatedSeries.splice(index, 1, series)
      }
    },
    replaceBook(book: BookDto) {
      let index = this.latestBooks.findIndex(x => x.id === book.id)
      if (index !== -1) {
        this.latestBooks.splice(index, 1, book)
      }
      index = this.inProgressBooks.findIndex(x => x.id === book.id)
      if (index !== -1) {
        this.inProgressBooks.splice(index, 1, book)
      }
      index = this.onDeckBooks.findIndex(x => x.id === book.id)
      if (index !== -1) {
        this.onDeckBooks.splice(index, 1, book)
      }
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
      this.selectedSeries = await Promise.all(this.selectedSeries.map(s =>
        this.$komgaSeries.getOneSeries(s.id),
      ))
    },
    async markSelectedSeriesUnread() {
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
    editMultipleSeries() {
      this.$store.dispatch('dialogUpdateSeries', this.selectedSeries)
    },
    editMultipleBooks() {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    addToReadList() {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks)
    },
    async markSelectedBooksRead() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, {completed: true}),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
    },
    async markSelectedBooksUnread() {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
      this.selectedBooks = await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.getBook(b.id),
      ))
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
