<template>
  <v-card width="210px"
  >
    <v-img
      :src="getThumbnailUrl()"
      lazy-src="../assets/cover.png"
      max-width="210px"
      height="300px"
    >
    </v-img>

    <v-card-title>{{ series.name }}</v-card-title>

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
