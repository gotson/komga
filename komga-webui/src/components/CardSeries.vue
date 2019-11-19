<template>
  <v-card :width="width"
  >
    <v-img
      :src="getThumbnailUrl()"
      lazy-src="../assets/cover.svg"
      aspect-ratio="0.7071"
    >
    </v-img>

    <v-card-subtitle class="text-truncate d-inline-block pa-2 pb-1"
                     :style="{'max-width': width-16 + 'px'}"
    >
      {{ series.name }}
    </v-card-subtitle>

  </v-card>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'CardSeries',
  data: () => {
    return {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin
    }
  },
  props: {
    series: {
      type: Object as () => SeriesDto,
      required: true
    },
    width: {
      type: [String, Number],
      required: false,
      default: 125
    }
  },
  methods: {
    getThumbnailUrl () {
      return `${this.baseURL}/api/v1/series/${this.series.id}/thumbnail`
    }
  }
})
</script>

<style scoped>
</style>
