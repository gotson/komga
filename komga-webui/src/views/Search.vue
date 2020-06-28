<template>
  <div>
    <toolbar-sticky>
      <v-toolbar-title>
        <span>Search results for "{{ $route.query.q }}"</span>
      </v-toolbar-title>
    </toolbar-sticky>

    <v-container fluid class="px-6">
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
        <horizontal-scroller v-if="series.length !== 0" class="my-4">
          <template v-slot:prepend>
            <div class="title">Series</div>
          </template>
          <template v-slot:content>
            <div v-for="(item, index) in series"
                 :key="index">
              <item-card class="ma-2 card" :item="item" :on-edit="singleEditSeries"/>
            </div>
          </template>
        </horizontal-scroller>

        <horizontal-scroller v-if="books.length !== 0" class="my-4">
          <template v-slot:prepend>
            <div class="title">Books</div>
          </template>
          <template v-slot:content>
            <div v-for="(item, index) in books"
                 :key="index">
              <item-card class="ma-2 card" :item="item" :on-edit="singleEditBook"/>
            </div>
          </template>
        </horizontal-scroller>

        <horizontal-scroller v-if="collections.length !== 0" class="my-4">
          <template v-slot:prepend>
            <div class="title">Collections</div>
          </template>
          <template v-slot:content>
            <div v-for="(item, index) in collections"
                 :key="index">
              <item-card class="ma-2 card" :item="item" :on-edit="singleEditCollection"/>
            </div>
          </template>
        </horizontal-scroller>

      </template>
    </v-container>

  </div>
</template>

<script lang="ts">
import EmptyState from '@/components/EmptyState.vue'
import HorizontalScroller from '@/components/HorizontalScroller.vue'
import ItemCard from '@/components/ItemCard.vue'
import ToolbarSticky from '@/components/ToolbarSticky.vue'
import { BOOK_CHANGED, COLLECTION_CHANGED, LIBRARY_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Search',
  components: {
    EmptyState,
    ToolbarSticky,
    HorizontalScroller,
    ItemCard,
  },
  data: () => {
    return {
      series: [] as SeriesDto[],
      books: [] as BookDto[],
      collections: [] as CollectionDto[],
      pageSize: 50,
      loading: false,
    }
  },
  created () {
    this.$eventHub.$on(LIBRARY_DELETED, this.reloadResults)
    this.$eventHub.$on(SERIES_CHANGED, this.reloadResults)
    this.$eventHub.$on(BOOK_CHANGED, this.reloadResults)
    this.$eventHub.$on(COLLECTION_CHANGED, this.reloadResults)
  },
  beforeDestroy () {
    this.$eventHub.$off(LIBRARY_DELETED, this.reloadResults)
    this.$eventHub.$off(SERIES_CHANGED, this.reloadResults)
    this.$eventHub.$off(BOOK_CHANGED, this.reloadResults)
    this.$eventHub.$off(COLLECTION_CHANGED, this.reloadResults)
  },
  watch: {
    '$route.query.q': {
      handler: function (val) {
        this.loadResults(val)
      },
      deep: true,
      immediate: true,
    },
  },
  computed: {
    emptyResults (): boolean {
      return !this.loading && this.series.length === 0 && this.books.length === 0 && this.collections.length === 0
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
    reloadResults () {
      this.loadResults(this.$route.query.q.toString())
    },
    async loadResults (search: string) {
      if (search) {
        this.loading = true

        this.series = (await this.$komgaSeries.getSeries(undefined, { size: this.pageSize }, search)).content
        this.books = (await this.$komgaBooks.getBooks(undefined, { size: this.pageSize }, search)).content
        this.collections = (await this.$komgaCollections.getCollections(undefined, { size: this.pageSize }, search)).content

        this.loading = false
      } else {
        this.series = []
        this.books = []
        this.collections = []
      }
    },
  },
})
</script>
<style scoped>
</style>
