<template>
  <v-card :width="width"
          :to="{name:'browse-series', params: {seriesId: series.id}}"
  >

    <v-img
      :src="thumbnailUrl"
      lazy-src="../assets/cover.svg"
      aspect-ratio="0.7071"
    >
      <span class="white--text pa-1 px-2 subtitle-2"
            style="background: darkorange; position: absolute; right: 0"
      >
        {{ series.booksCount }}
      </span>
    </v-img>

    <v-card-subtitle class="pa-2 pb-1 text--primary"
                     v-line-clamp="2"
                     style="word-break: normal !important; height: 4em"
                     :title="series.name"
    >
      {{ series.name }}
    </v-card-subtitle>

    <v-card-text class="px-2"
    >
      <span v-if="series.booksCount === 1">{{ series.booksCount }} book</span>
      <span v-else>{{ series.booksCount }} books</span>
    </v-card-text>

  </v-card>
</template>

<script lang="ts">
import Vue, { PropType } from 'vue'

export default Vue.extend({
  name: 'CardSeries',
  data: () => {
    return {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin
    }
  },
  props: {
    series: {
      type: Object as PropType<SeriesDto>,
      required: true
    },
    width: {
      type: [String, Number],
      required: false,
      default: 150
    }
  },
  computed: {
    thumbnailUrl (): string {
      return `${this.baseURL}/api/v1/series/${this.series.id}/thumbnail`
    }
  }
})
</script>

<style scoped>
</style>
