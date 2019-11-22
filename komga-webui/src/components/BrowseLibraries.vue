<template>
  <div>
    <v-toolbar flat
               color="grey lighten-4"
               class="sticky-bar"
               :style="barStyle"
    >
      <v-toolbar-title>
        <span>{{ libraryName }}</span>
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

        <card-series v-for="s in series"
                     :key="s.id"
                     justify-self="start"
                     :series="s"
                     class="ma-3 ml-2 mr-2"
        ></card-series>

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
import CardSeries from '@/components/CardSeries.vue'
import Vue from 'vue'
import InfiniteLoading from 'vue-infinite-loading'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: { CardSeries, InfiniteLoading },
  data: () => {
    return {
      libraryName: '',
      series: [] as SeriesDto[],
      lastPage: false,
      page: null as number | null,
      totalElements: null as number | null,
      infiniteId: +new Date(),
      sortOptions: [{ name: 'Name', key: 'name' }, { name: 'Date added', key: 'createdDate' }] as SortOption[],
      sortActive: { key: 'name', order: 'asc' } as SortActive as SortActive,
      sortDefault: { key: 'name', order: 'asc' } as SortActive as SortActive
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
    libraryId: {
      type: Number,
      default: 0
    }
  },
  async created () {
    this.libraryName = await this.getLibraryName()
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.libraryName = this.getLibraryNameLazy(Number(to.params.libraryId))
      this.sortActive = this.$_.clone(this.sortDefault)
      this.reloadData()
    }

    next()
  },
  methods: {
    reloadData () {
      this.series = []
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

        let libraryId
        if (this.libraryId !== 0) {
          libraryId = this.libraryId
        }
        const newPage = await this.$komgaSeries.getSeries(libraryId, pageRequest)
        this.lastPage = newPage.last
        this.totalElements = newPage.totalElements
        this.series = this.series.concat(newPage.content)

        if (updateRoute) {
          this.page = newPage.number
          this.$router.replace({
            name: this.$route.name,
            params: { libraryId: this.$route.params.libraryId, page: newPage.number.toString() }
          })
        } else {
          this.page = Number(this.$route.params.page)
        }
      }
    },
    async getLibraryName (): Promise<string> {
      if (this.libraryId !== 0) {
        return (await this.$komgaLibraries.getLibrary(this.libraryId)).name
      } else {
        return 'All libraries'
      }
    },
    getLibraryNameLazy (libraryId: any): string {
      if (libraryId !== 0) {
        return (this.$store.getters.getLibraryById(libraryId)).name
      } else {
        return 'All libraries'
      }
    }
  }
})
</script>

<style scoped>
@import "../assets/css/badge.css";
@import "../assets/css/sticky-bar.css";
</style>
