<template>
  <div class="ma-3">

    <empty-state v-if="allEmpty"
                 title="Nothing to show"
                 icon="mdi-help-circle"
                 icon-color="secondary"
    >
    </empty-state>

    <horizontal-scroller v-if="inProgressBooks.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Keep Reading</div>
      </template>
      <template v-slot:content>
        <item-browser :items="inProgressBooks" nowrap :edit-function="singleEditBook" :selectable="false"/>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="onDeckBooks.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">On Deck</div>
      </template>
      <template v-slot:content>
        <item-browser :items="onDeckBooks" nowrap :edit-function="singleEditBook" :selectable="false"/>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="newSeries.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Recently Added Series</div>
      </template>
      <template v-slot:content>
        <item-browser :items="newSeries" nowrap :edit-function="singleEditSeries" :selectable="false"/>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="updatedSeries.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Recently Updated Series</div>
      </template>
      <template v-slot:content>
        <item-browser :items="updatedSeries" nowrap :edit-function="singleEditSeries" :selectable="false"/>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="latestBooks.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Recently Added Books</div>
      </template>
      <template v-slot:content>
        <item-browser :items="latestBooks" nowrap :edit-function="singleEditBook" :selectable="false"/>
      </template>
    </horizontal-scroller>

  </div>
</template>

<script lang="ts">
import EmptyState from '@/components/EmptyState.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import { ReadStatus } from '@/types/enum-books'
import { BOOK_CHANGED, LIBRARY_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'
import ItemBrowser from '@/components/ItemBrowser.vue'

export default Vue.extend({
  name: 'Dashboard',
  components: { HorizontalScroller, EmptyState, ItemBrowser },
  data: () => {
    return {
      newSeries: [] as SeriesDto[],
      updatedSeries: [] as SeriesDto[],
      latestBooks: [] as BookDto[],
      inProgressBooks: [] as BookDto[],
      onDeckBooks: [] as BookDto[],
    }
  },
  created () {
    this.$eventHub.$on(LIBRARY_DELETED, this.loadAll)
    this.$eventHub.$on(SERIES_CHANGED, this.loadAll)
    this.$eventHub.$on(BOOK_CHANGED, this.loadAll)
  },
  beforeDestroy () {
    this.$eventHub.$off(LIBRARY_DELETED, this.loadAll)
    this.$eventHub.$off(SERIES_CHANGED, this.loadAll)
    this.$eventHub.$off(BOOK_CHANGED, this.loadAll)
  },
  mounted () {
    this.loadAll()
  },
  watch: {
    editSeriesSingle (val: SeriesDto) {
      this.replaceSeries(val)
    },
    editBookSingle (val: BookDto) {
      this.replaceBook(val)
    },
  },
  computed: {
    allEmpty (): boolean {
      return this.newSeries.length === 0 &&
        this.updatedSeries.length === 0 &&
        this.latestBooks.length === 0 &&
        this.inProgressBooks.length === 0 &&
        this.onDeckBooks.length === 0
    },
  },
  methods: {
    loadAll () {
      this.loadNewSeries()
      this.loadUpdatedSeries()
      this.loadLatestBooks()
      this.loadInProgressBooks()
      this.loadOnDeckBooks()
    },
    replaceSeries (series: SeriesDto) {
      let index = this.newSeries.findIndex(x => x.id === series.id)
      if (index !== -1) {
        this.newSeries.splice(index, 1, series)
      }
      index = this.updatedSeries.findIndex(x => x.id === series.id)
      if (index !== -1) {
        this.updatedSeries.splice(index, 1, series)
      }
    },
    replaceBook (book: BookDto) {
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
    async loadNewSeries () {
      this.newSeries = (await this.$komgaSeries.getNewSeries()).content
    },
    async loadUpdatedSeries () {
      this.updatedSeries = (await this.$komgaSeries.getUpdatedSeries()).content
    },
    async loadLatestBooks () {
      const pageRequest = {
        sort: ['createdDate,desc'],
      } as PageRequest

      this.latestBooks = (await this.$komgaBooks.getBooks(undefined, pageRequest)).content
    },
    async loadInProgressBooks () {
      const pageRequest = {
        sort: ['readProgress.lastModified,desc'],
      } as PageRequest

      this.inProgressBooks = (await this.$komgaBooks.getBooks(undefined, pageRequest, undefined, undefined, [ReadStatus.IN_PROGRESS])).content
    },
    async loadOnDeckBooks () {
      this.onDeckBooks = (await this.$komgaBooks.getBooksOnDeck()).content
    },
    singleEditSeries (series: SeriesDto) {
      this.$store.dispatch('dialogUpdateSeries', series)
    },
    singleEditBook (book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
  },
})
</script>

<style scoped>

</style>
