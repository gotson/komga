<template>
  <div class="ma-3">

    <edit-series-dialog v-model="dialogEditSeriesSingle"
                        :series.sync="editSeriesSingle"
    />

    <edit-books-dialog v-model="dialogEditBookSingle"
                       :books.sync="editBookSingle"
    />

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
        <div v-for="(b, i) in inProgressBooks"
             :key="i"
        >
          <item-card class="ma-2 card" :item="b" :on-edit="singleEditBook"/>
        </div>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="onDeckBooks.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">On Deck</div>
      </template>
      <template v-slot:content>
        <div v-for="(b, i) in onDeckBooks"
             :key="i"
        >
          <item-card class="ma-2 card" :item="b" :on-edit="singleEditBook"/>
        </div>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="newSeries.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Recently Added Series</div>
      </template>
      <template v-slot:content>
        <div v-for="(s, i) in newSeries"
             :key="i">
          <item-card class="ma-2 card" :item="s" :on-edit="singleEditSeries"/>
        </div>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="updatedSeries.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Recently Updated Series</div>
      </template>
      <template v-slot:content>
        <div v-for="(s, i) in updatedSeries"
             :key="i">
          <item-card class="ma-2 card" :item="s" :on-edit="singleEditSeries"/>
        </div>
      </template>
    </horizontal-scroller>

    <horizontal-scroller v-if="latestBooks.length !== 0" class="my-4">
      <template v-slot:prepend>
        <div class="title">Recently Added Books</div>
      </template>
      <template v-slot:content>
        <div v-for="(b, i) in latestBooks"
             :key="i"
        >
          <item-card class="ma-2 card" :item="b" :on-edit="singleEditBook"/>
        </div>
      </template>
    </horizontal-scroller>

  </div>
</template>

<script lang="ts">
import EditBooksDialog from '@/components/EditBooksDialog.vue'
import EditSeriesDialog from '@/components/EditSeriesDialog.vue'
import EmptyState from '@/components/EmptyState.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemCard from '@/components/ItemCard.vue'
import { ReadStatus } from '@/types/enum-books'
import { LIBRARY_DELETED } from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Dashboard',
  components: { ItemCard, HorizontalScroller, EditSeriesDialog, EditBooksDialog, EmptyState },
  data: () => {
    return {
      newSeries: [] as SeriesDto[],
      updatedSeries: [] as SeriesDto[],
      latestBooks: [] as BookDto[],
      inProgressBooks: [] as BookDto[],
      onDeckBooks: [] as BookDto[],
      editSeriesSingle: {} as SeriesDto,
      dialogEditSeriesSingle: false,
      editBookSingle: {} as BookDto,
      dialogEditBookSingle: false,
    }
  },
  created () {
    this.$eventHub.$on(LIBRARY_DELETED, this.loadAll)
  },
  beforeDestroy () {
    this.$eventHub.$off(LIBRARY_DELETED, this.loadAll)
  },
  mounted () {
    this.loadAll()
  },
  watch: {
    editSeriesSingle (val: SeriesDto) {
      let index = this.newSeries.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.newSeries.splice(index, 1, val)
      }
      index = this.updatedSeries.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.updatedSeries.splice(index, 1, val)
      }
    },
    editBookSingle (val: BookDto) {
      let index = this.latestBooks.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.latestBooks.splice(index, 1, val)
      }
      index = this.inProgressBooks.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.inProgressBooks.splice(index, 1, val)
      }
      index = this.onDeckBooks.findIndex(x => x.id === val.id)
      if (index !== -1) {
        this.onDeckBooks.splice(index, 1, val)
      }
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
      this.editSeriesSingle = series
      this.dialogEditSeriesSingle = true
    },
    singleEditBook (book: BookDto) {
      this.editBookSingle = book
      this.dialogEditBookSingle = true
    },
  },
})
</script>

<style scoped>

</style>
