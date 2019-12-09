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

      <v-spacer/>

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

        <v-skeleton-loader v-for="(s, i) in series"
                           :key="i"
                           width="150"
                           :loading="s === null"
                           type="card, text"
                           class="ma-3 ml-2 mr-2"
                           v-intersect="onCardIntersect"
                           :data-index="i"
        >
          <card-series justify-self="start"
                       :series="s"
          />
        </v-skeleton-loader>

      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import CardSeries from '@/components/CardSeries.vue'
import Vue from 'vue'

enum LoadState {
  Loaded,
  NotLoaded,
  Loading
}

export default Vue.extend({
  name: 'BrowseLibraries',
  components: { CardSeries },
  data: () => {
    return {
      libraryName: '',
      series: [] as SeriesDto[],
      pagesState: [] as LoadState[],
      pageSize: 20,
      visibleCards: [] as number[],
      totalElements: null as number | null,
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
  mounted () {
    // fill series skeletons if an index is provided, so scroll position can be restored
    if (this.$route.params.index) {
      this.series = Array(Number(this.$route.params.index)).fill(null)
    } else { // else fill one page of skeletons
      this.series = Array(this.pageSize).fill(null)
    }

    let pageToLoad = 0
    if (this.$route.params.index) {
      // floor down to get a zero-based page number
      pageToLoad = Math.floor(Number(this.$route.params.index) / this.pageSize)
    }
    this.loadInitialData(this.libraryId, pageToLoad)
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.libraryName = this.getLibraryNameLazy(Number(to.params.libraryId))
      this.sortActive = this.$_.clone(this.sortDefault)
      this.reloadData(Number(to.params.libraryId))
    }

    next()
  },
  methods: {
    async onCardIntersect (entries: any, observer: any, isIntersecting: boolean) {
      const elementIndex = Number(entries[0].target.dataset['index'])
      if (isIntersecting) {
        this.visibleCards.push(elementIndex)
        const pageNumber = Math.floor(elementIndex / this.pageSize)
        if (this.pagesState[pageNumber] === undefined || this.pagesState[pageNumber] === LoadState.NotLoaded) {
          this.pagesState[pageNumber] = LoadState.Loading
          this.processPage(await this.loadPage(pageNumber, this.libraryId))
        }
      } else {
        this.$_.pull(this.visibleCards, elementIndex)
      }

      const max = this.$_.max(this.visibleCards)
      const index = (max === undefined ? 0 : max).toString()

      if (this.$route.params.index !== index) {
        this.$router.replace({
          name: this.$route.name,
          params: { libraryId: this.$route.params.libraryId, index: index }
        })
      }
    },
    reloadData (libraryId: number) {
      this.series = Array(this.pageSize).fill(null)
      this.totalElements = null
      this.pagesState = []
      this.visibleCards = []
      this.loadInitialData(libraryId)
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
      this.reloadData(this.libraryId)
    },
    async loadInitialData (libraryId: number, pageToLoad: number = 0) {
      const page = await this.loadPage(pageToLoad, libraryId)

      // initialize page data
      this.totalElements = page.totalElements
      this.series = Array(this.totalElements).fill(null)
      this.pagesState = Array(page.totalPages).fill(LoadState.NotLoaded)

      // process page data
      this.processPage(page)
    },
    async loadPage (page: number, libraryId: number): Promise<Page<SeriesDto>> {
      const pageRequest = {
        page: page,
        size: this.pageSize
      } as PageRequest

      if (this.sortActive != null) {
        pageRequest.sort = [`${this.sortActive.key},${this.sortActive.order}`]
      }

      let requestLibraryId
      if (libraryId !== 0) {
        requestLibraryId = libraryId
      }
      return this.$komgaSeries.getSeries(requestLibraryId, pageRequest)
    },
    processPage (page: Page<SeriesDto>) {
      this.series.splice(page.number * page.size, page.size, ...page.content)
      this.pagesState[page.number] = LoadState.Loaded
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
