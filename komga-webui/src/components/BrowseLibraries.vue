<template>
  <div>
    <div class="display-1">{{ libraryName }}</div>

    <v-container fluid>
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
      infiniteId: +new Date()
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
      this.series = []
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

        let libraryId
        if (this.libraryId !== 0) {
          libraryId = this.libraryId
        }
        const newPage = await this.$komgaSeries.getSeries(libraryId, pageRequest)
        this.lastPage = newPage.last
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

</style>
