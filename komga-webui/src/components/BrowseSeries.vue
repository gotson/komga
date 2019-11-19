<template>
  <div>
    <div class="display-1">{{ $_.get(series, 'name', '') }}</div>

    <v-container fluid>
      <v-row justify="start">

        <card-book v-for="b in books"
                   :key="b.id"
                   justify-self="start"
                   :book="b"
                   class="ma-3 ml-2 mr-2"
        ></card-book>

      </v-row>
    </v-container>

    <infinite-loading @infinite="infiniteHandler"
                      :identifier="infiniteId"
    >
      <div slot="spinner">
        <v-progress-circular
          indeterminate
          color="primary"
        ></v-progress-circular>
      </div>
      <div slot="no-more"></div>
      <div slot="no-results"></div>
    </infinite-loading>
  </div>
</template>

<script lang="ts">
import CardBook from '@/components/CardBook.vue'
import Vue from 'vue'
import InfiniteLoading from 'vue-infinite-loading'

export default Vue.extend({
  name: 'BrowseSeries',
  components: { CardBook, InfiniteLoading },
  data: () => {
    return {
      series: {} as SeriesDto,
      books: [] as BookDto[],
      booksPage: {} as Page<BookDto>,
      infiniteId: +new Date()
    }
  },
  async created () {
    this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
  },
  props: {
    seriesId: {
      type: Number,
      required: true
    }
  },
  watch: {
    async seriesId (val) {
      this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
      this.books = []
      this.booksPage = {} as Page<BookDto>
      this.infiniteId += 1
    }
  },
  mounted (): void {
  },
  methods: {
    async infiniteHandler ($state: any) {
      await this.loadNextPage()
      if (this.booksPage.last) {
        $state.complete()
      } else {
        $state.loaded()
      }
    },
    async loadNextPage () {
      if (this.$_.get(this.booksPage, 'last', false) !== true) {
        const pageRequest = {
          page: this.$_.get(this.booksPage, 'number', -1) + 1,
          size: 20
        } as PageRequest

        this.booksPage = await this.$komgaSeries.getBooks(this.seriesId, pageRequest)
        this.books = this.books.concat(this.booksPage.content)
      }
    }
  }
})
</script>

<style scoped>

</style>
