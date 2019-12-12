<template>
  <div>
    <v-toolbar flat
               color="grey lighten-4"
               class="sticky-bar"
               :style="barStyle"
    >
      <v-btn icon
             title="Go to library"
             :to="{name:'browse-libraries', params: {libraryId: series.libraryId ? series.libraryId : 0 }}"
      >
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>

      <v-toolbar-title>
        <span v-if="series.name">{{ series.name }}</span>
        <span class="ml-4 badge-count"
              v-if="totalElements"
        >
          {{ totalElements }}
        </span>
      </v-toolbar-title>

      <v-spacer/>

      <v-menu offset-y>
        <template v-slot:activator="{on}">
          <v-btn icon v-on="on">
            <v-icon :color="sortCustom ? 'secondary' : null"
            >mdi-sort-variant
            </v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item v-for="(item, index) in sortOptions"
                       :key="index"
                       @click="setSort(item)"
          >
            <v-list-item-icon>
              <v-icon color="secondary" v-if="item.key === sortActive.key && sortActive.order === 'asc'">
                mdi-chevron-up
              </v-icon>
              <v-icon color="secondary" v-if="item.key === sortActive.key && sortActive.order === 'desc'">
                mdi-chevron-down
              </v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ item.name }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-toolbar>

    <v-container fluid class="mx-3">
      <v-row justify="start">

        <v-skeleton-loader v-for="(b, i) in books"
                           :key="i"
                           width="150"
                           height="328.13"
                           justify-self="start"
                           :loading="b === null"
                           type="card, text"
                           class="ma-3 ml-2 mr-2"
                           v-intersect="onCardIntersect"
                           :data-index="i"
        >
          <card-book :book="b"/>
        </v-skeleton-loader>

      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import CardBook from '@/components/CardBook.vue'
import { LoadState } from '@/types/common'
import Vue from 'vue'

export default Vue.extend({
  name: 'BrowseSeries',
  components: { CardBook },
  data: () => {
    return {
      series: {} as SeriesDto,
      books: [] as BookDto[],
      pagesState: [] as LoadState[],
      pageSize: 20,
      visibleCards: [] as number[],
      totalElements: null as number | null,
      sortOptions: [{ name: 'Number', key: 'number' }, { name: 'Date added', key: 'createdDate' }] as SortOption[],
      sortActive: {} as SortActive as SortActive,
      sortDefault: { key: 'number', order: 'asc' } as SortActive as SortActive
    }
  },
  computed: {
    sortCustom (): boolean {
      return this.sortActive.key !== this.sortDefault.key || this.sortActive.order !== this.sortDefault.order
    },
    barStyle (): any {
      if (this.$vuetify.breakpoint.name === 'xs') {
        return { 'top': '56px' }
      } else {
        return { 'top': '64px' }
      }
    }
  },
  props: {
    seriesId: {
      type: Number,
      required: true
    }
  },
  async created () {
    this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
  },
  mounted () {
    // fill books skeletons if an index is provided, so scroll position can be restored
    if (this.$route.params.index) {
      this.books = Array(Number(this.$route.params.index)).fill(null)
    } else { // else fill one page of skeletons
      this.books = Array(this.pageSize).fill(null)
    }

    // restore sort from query param
    this.sortActive = this.parseQuerySortOrDefault(this.$route.query.sort)
  },
  async beforeRouteUpdate (to, from, next) {
    if (to.params.seriesId !== from.params.seriesId) {
      this.series = await this.$komgaSeries.getOneSeries(Number(to.params.seriesId))
      this.sortActive = this.parseQuerySortOrDefault(to.query.sort)
      this.reloadData(Number(to.params.seriesId))
    }

    next()
  },
  methods: {
    parseQuerySortOrDefault (querySort: any): SortActive {
      let customSort = null
      if (querySort) {
        const split = querySort.split(',')
        if (split.length === 2 && this.$_.map(this.sortOptions, 'key').includes(split[0]) && ['asc', 'desc'].includes(split[1])) {
          customSort = { key: split[0], order: split[1] }
        }
      }
      if (customSort !== null) {
        return customSort
      } else {
        return this.$_.clone(this.sortDefault)
      }
    },
    async onCardIntersect (entries: any, observer: any, isIntersecting: boolean) {
      const elementIndex = Number(entries[0].target.dataset['index'])
      if (isIntersecting) {
        this.visibleCards.push(elementIndex)
        const pageNumber = Math.floor(elementIndex / this.pageSize)
        if (this.pagesState[pageNumber] === undefined || this.pagesState[pageNumber] === LoadState.NotLoaded) {
          this.processPage(await this.loadPage(pageNumber, this.seriesId))
        }
      } else {
        this.$_.pull(this.visibleCards, elementIndex)
      }

      const max = this.$_.max(this.visibleCards)
      const index = (max === undefined ? 0 : max).toString()

      if (this.$route.params.index !== index) {
        this.$router.replace({
          name: this.$route.name,
          params: { seriesId: this.$route.params.seriesId, index: index },
          query: { sort: `${this.sortActive.key},${this.sortActive.order}` }
        })
      }
    },
    reloadData (seriesId: number) {
      this.totalElements = null
      this.pagesState = []
      this.visibleCards = []
      this.books = Array(this.pageSize).fill(null)
      this.loadInitialData(seriesId)
    },
    setSort (sort: SortOption) {
      if (this.sortActive.key === sort.key) {
        if (this.sortActive.order === 'desc') {
          this.sortActive.order = 'asc'
        } else {
          this.sortActive.order = 'desc'
        }
      } else {
        this.sortActive = { key: sort.key, order: 'desc' }
      }
      this.$router.replace({
        name: this.$route.name,
        params: { seriesId: this.$route.params.seriesId, index: this.$route.params.index },
        query: { sort: `${this.sortActive.key},${this.sortActive.order}` }
      })
      this.reloadData(this.seriesId)
    },
    async loadInitialData (seriesId: number, pageToLoad: number = 0) {
      this.processPage(await this.loadPage(pageToLoad, seriesId))
    },
    async loadPage (page: number, seriesId: number): Promise<Page<BookDto>> {
      this.pagesState[page] = LoadState.Loading
      const pageRequest = {
        page: page,
        size: this.pageSize
      } as PageRequest

      if (this.sortActive != null) {
        pageRequest.sort = [`${this.sortActive.key},${this.sortActive.order}`]
      }
      return this.$komgaSeries.getBooks(seriesId, pageRequest)
    },
    processPage (page: Page<BookDto>) {
      if (this.totalElements === null) {
        // initialize page data
        this.totalElements = page.totalElements
        this.books = Array(this.totalElements).fill(null)
        this.pagesState = Array(page.totalPages).fill(LoadState.NotLoaded)
      }
      this.books.splice(page.number * page.size, page.size, ...page.content)
      this.pagesState[page.number] = LoadState.Loaded
    }
  }
})
</script>

<style scoped>
@import "../assets/css/badge.css";
@import "../assets/css/sticky-bar.css";
</style>
