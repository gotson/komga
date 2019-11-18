<template>
  <v-container fluid>
    <v-row justify="start">

      <card-series v-for="s in series"
                   :key="s.id"
                   justify-self="start"
                   :series="s"
                   class="ma-3"
      ></card-series>

    </v-row>
  </v-container>
</template>

<script lang="ts">
import CardSeries from '@/components/CardSeries.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'BrowseLibraries',
  components: { CardSeries },
  data: () => {
    return {
      series: [] as SeriesDto[],
      seriesPage: {} as Page<SeriesDto>
    }
  },
  props: {
    libraryId: {
      type: Number,
      required: true
    }
  },
  watch: {
    libraryId (val) {
      this.series = []
      this.seriesPage = {} as Page<SeriesDto>

      this.loadSeries()
    }
  },
  mounted (): void {
    this.loadSeries()
  },
  methods: {
    async loadSeries () {
      this.seriesPage = await this.$komgaSeries.getSeries(this.libraryId)
      this.series = this.seriesPage.content
    },
    async loadNextPage () {
      if (!this.seriesPage.last) {
        const pageRequest = {
          page: this.seriesPage.number + 1
        } as PageRequest

        this.seriesPage = await this.$komgaSeries.getSeries(this.libraryId, pageRequest)
        this.series = this.series.concat(this.seriesPage.content)
      }
    }
  }
})
</script>

<style scoped>

</style>
