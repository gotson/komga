<template>
  <v-bottom-navigation grow color="primary"
                       :fixed="$vuetify.breakpoint.name === 'xs'"
  >
    <v-btn :to="{name: 'browse-libraries', params: {libraryId: libraryId}}" exact>
      <span>Browse</span>
      <v-icon>mdi-bookshelf</v-icon>
    </v-btn>

    <v-btn :to="{name: 'browse-collections', params: {libraryId: libraryId}}">
      <span>Collections</span>
      <v-icon>mdi-layers-triple</v-icon>
    </v-btn>

  </v-bottom-navigation>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'LibraryNavigation',
  props: {
    libraryId: {
      type: Number,
      required: true,
    },
  },
})
</script>

<style scoped>

</style>
