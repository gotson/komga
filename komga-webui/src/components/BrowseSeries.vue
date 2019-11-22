<template>
  <div>
    <v-toolbar flat
               color="grey lighten-4"
               class="sticky-bar"
               :style="barStyle"
    >
      <v-toolbar-title>
        <span v-if="series.name">{{ series.name }}</span>
        <span class="ml-4 badge-count"
              v-if="totalElements"
        >
          {{ totalElements }}
        </span>
      </v-toolbar-title>

      <v-spacer></v-spacer>

      <v-toolbar-items>
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
      </v-toolbar-items>
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
      totalElements: null as number | null,
      infiniteId: +new Date(),
      sortOptions: [{ name: 'Number', key: 'number' }, { name: 'Date added', key: 'createdDate' }] as SortOption[],
      sortActive: { key: 'number', order: 'asc' } as SortActive as SortActive,
      sortDefault: { key: 'number', order: 'asc' } as SortActive as SortActive
    }
  },
  async created () {
    this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
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
  async beforeRouteUpdate (to, from, next) {
    if (to.params.seriesId !== from.params.seriesId) {
      this.series = await this.$komgaSeries.getOneSeries(this.seriesId)
      this.sortActive = this.$_.clone(this.sortDefault)
      this.reloadData()
    }

    next()
  },
  methods: {
    reloadData () {
      this.books = []
      this.lastPage = false
      this.totalElements = null
      this.page = null
      this.infiniteId += 1
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
      this.reloadData()
    },
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

        if (this.sortActive != null) {
          pageRequest.sort = [`${this.sortActive.key},${this.sortActive.order}`]
        }

        const newPage = await this.$komgaSeries.getBooks(this.seriesId, pageRequest)
        this.lastPage = newPage.last
        this.totalElements = newPage.totalElements
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
@import "../assets/css/badge.css";
@import "../assets/css/sticky-bar.css";
</style>
