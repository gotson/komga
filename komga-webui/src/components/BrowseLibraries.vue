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
      seriesPage: {} as Page<SeriesDto>,
      infiniteId: +new Date()
    }
  },
  props: {
    libraryId: {
      type: Number,
      required: false
    }
  },
  async created () {
    this.libraryName = await this.getLibraryName()
  },
  watch: {
    async libraryId (val) {
      this.libraryName = await this.getLibraryName()
      this.series = []
      this.seriesPage = {} as Page<SeriesDto>
      this.infiniteId += 1
    }
  },
  mounted (): void {
  },
  methods: {
    async infiniteHandler ($state: any) {
      await this.loadNextPage()
      if (this.seriesPage.last) {
        $state.complete()
      } else {
        $state.loaded()
      }
    },
    async loadNextPage () {
      if (this.$_.get(this.seriesPage, 'last', false) !== true) {
        const pageRequest = {
          page: this.$_.get(this.seriesPage, 'number', -1) + 1,
          size: 50
        } as PageRequest

        this.seriesPage = await this.$komgaSeries.getSeries(this.libraryId, pageRequest)
        this.series = this.series.concat(this.seriesPage.content)
      }
    },
    async getLibraryName (): Promise<string> {
      if (this.libraryId) {
        return (await this.$komgaLibraries.getLibrary(this.libraryId)).name
      } else {
        return 'All libraries'
      }
    }
  }
})
</script>

<style scoped>

</style>
