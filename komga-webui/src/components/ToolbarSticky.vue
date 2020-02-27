<template>
  <v-toolbar flat
             :color="color"
             class="sticky-bar"
             :style="barStyle"
             :elevation="elevation"
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
      if (['xs', 'sm'].includes(this.$vuetify.breakpoint.name)) {
        return { 'top': '56px' }
      } else {
        return { 'top': '64px' }
      }
    }
  },
  props: {
    elevation: {
      type: Number,
      default: undefined
    },
    color: {
      type: String,
      default: 'grey lighten-4'
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
