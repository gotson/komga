<template>
  <div>
    <v-toolbar flat
               color="grey lighten-4"
               class="sticky-bar"
               :style="barStyle"
    >
      <v-toolbar-title>{{ $_.get(series, 'name', '') }}</v-toolbar-title>
    </v-toolbar>

    <v-container fluid class="mx-3">
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
      lastPage: false,
      page: null as number | null,
      infiniteId: +new Date()
    }
  },
  async created () {
    this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
  },
  computed: {
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
  async beforeRouteUpdate (to, from, next) {
    if (to.params.seriesId !== from.params.seriesId) {
      this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
      this.books = []
      this.lastPage = false
      this.page = null
      this.infiniteId += 1
    }

    next()
  },
  methods: {
    async infiniteHandler ($state: any) {
      await this.loadNextPage()
      if (this.lastPage) {
        $state.complete()
      } else {
        $state.loaded()
      }
    },
    async loadNextPage () {
      if (!this.lastPage) {
        let updateRoute = true
        const pageSize = 50
        const pageRequest = {
          page: 0,
          size: pageSize
        } as PageRequest

        if (this.page != null) {
          pageRequest.page = this.page! + 1
        } else if (this.$route.params.page) {
          pageRequest.size = (Number(this.$route.params.page) + 1) * pageSize
          updateRoute = false
        }

        const newPage = await this.$komgaSeries.getBooks(this.seriesId, pageRequest)
        this.lastPage = newPage.last
        this.books = this.books.concat(newPage.content)

        if (updateRoute) {
          this.page = newPage.number
          this.$router.replace({
            name: this.$route.name,
            params: { seriesId: this.$route.params.seriesId, page: newPage.number.toString() }
          })
        } else {
          this.page = Number(this.$route.params.page)
        }
      }
    }
  }
})
</script>

<style scoped>
.sticky-bar {
  position: -webkit-sticky;
  position: sticky;
  z-index: 2
}

</style>
