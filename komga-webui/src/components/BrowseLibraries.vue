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

      <v-menu offset-y v-if="libraryId !== 0">
        <template v-slot:activator="{ on }">
          <v-btn icon v-on="on">
            <v-icon>mdi-dots-vertical</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item @click="analyze()">
            <v-list-item-title>Analyze</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-toolbar>

    <v-container fluid class="px-6">
      <v-row justify="start" ref="content" v-resize="updateCardWidth">

        <v-skeleton-loader v-for="(s, i) in series"
                           :key="i"
                           :width="cardWidth"
                           height="306.14"
                           justify-self="start"
                           :loading="s === null"
                           type="card, text"
                           class="ma-3 mx-2"
                           v-intersect="onCardIntersect"
                           :data-index="i"
        >
          <card-series :series="s" :width="cardWidth"/>
        </v-skeleton-loader>

      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import CardSeries from '@/components/CardSeries.vue'
import { LoadState } from '@/types/common'
import Vue from 'vue'

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
      sortOptions: [{ name: 'Name', key: 'name' }, { name: 'Date added', key: 'createdDate' }, {
        name: 'Date updated',
        key: 'lastModifiedDate'
      }] as SortOption[],
      sortActive: {} as SortActive as SortActive,
      sortDefault: { key: 'name', order: 'asc' } as SortActive as SortActive,
      cardWidth: 150
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
    this.libraryName = await this.getLibraryNameLazy(this.libraryId)
  },
  mounted () {
    // fill series skeletons if an index is provided, so scroll position can be restored
    if (this.$route.params.index) {
      this.series = Array(Number(this.$route.params.index)).fill(null)
    } else { // else fill one page of skeletons
      this.series = Array(this.pageSize).fill(null)
    }

    // restore sort from query param
    this.sortActive = this.parseQuerySortOrDefault(this.$route.query.sort)
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.libraryId !== from.params.libraryId) {
      this.libraryName = this.getLibraryNameLazy(Number(to.params.libraryId))
      this.sortActive = this.parseQuerySortOrDefault(to.query.sort)
      this.reloadData(Number(to.params.libraryId))
    }

    next()
  },
  methods: {
    updateCardWidth () {
      const content = this.$refs.content as HTMLElement
      switch (this.$vuetify.breakpoint.name) {
        case 'xs':
          this.cardWidth = (content.clientWidth - (16 * 2)) / 2
          break
        case 'sm':
          this.cardWidth = (content.clientWidth - (16 * 3)) / 3
          break
        default:
          this.cardWidth = 150
      }
    },
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
          params: { libraryId: this.$route.params.libraryId, index: index },
          query: { sort: `${this.sortActive.key},${this.sortActive.order}` }
        })
      }
    },
    reloadData (libraryId: number) {
      this.totalElements = null
      this.pagesState = []
      this.visibleCards = []
      this.series = Array(this.pageSize).fill(null)
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
      this.$router.replace({
        name: this.$route.name,
        params: { libraryId: this.$route.params.libraryId, index: this.$route.params.index },
        query: { sort: `${this.sortActive.key},${this.sortActive.order}` }
      })
      this.reloadData(this.libraryId)
    },
    async loadInitialData (libraryId: number, pageToLoad: number = 0) {
      this.processPage(await this.loadPage(pageToLoad, libraryId))
    },
    async loadPage (page: number, libraryId: number): Promise<Page<SeriesDto>> {
      this.pagesState[page] = LoadState.Loading
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
      if (this.totalElements === null) {
        // initialize page data
        this.totalElements = page.totalElements
        this.series = Array(this.totalElements).fill(null)
        this.pagesState = Array(page.totalPages).fill(LoadState.NotLoaded)
      }
      this.series.splice(page.number * page.size, page.size, ...page.content)
      this.pagesState[page.number] = LoadState.Loaded
    },
    getLibraryNameLazy (libraryId: any): string {
      if (libraryId !== 0) {
        return (this.$store.getters.getLibraryById(libraryId)).name
      } else {
        return 'All libraries'
      }
    },
    analyze () {
      this.$komgaLibraries.analyzeLibrary(this.libraryId)
    }
  }
})
</script>

<style scoped>
@import "../assets/css/badge.css";
@import "../assets/css/sticky-bar.css";
</style>
