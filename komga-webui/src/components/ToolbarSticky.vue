<template>
  <v-toolbar flat
             color="grey lighten-4"
             class="sticky-bar"
             :style="barStyle"
  >
    <slot/>
  </v-toolbar>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'ToolbarSticky',
  computed: {
    barStyle (): any {
      if (this.$vuetify.breakpoint.name === 'xs') {
        return { 'top': '56px' }
      } else {
        return { 'top': '64px' }
      }
    }
  }
})
</script>

<style scoped>
.sticky-bar {
  position: -webkit-sticky;
  position: sticky;
  z-index: 2
}
</style>
