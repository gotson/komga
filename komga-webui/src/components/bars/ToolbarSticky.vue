<template>
  <v-toolbar flat
             :color="barColor"
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
      if (['xs', 'sm'].includes(this.$vuetify.breakpoint.name as any)) {
        return { 'top': '56px' }
      } else {
        return { 'top': '64px' }
      }
    },
    barColor (): any {
      if (this.$vuetify.theme.dark) {
        return 'grey darken-4'
      } else {
        return 'grey lighten-4'
      }
    },
  },
  props: {
    elevation: {
      type: Number,
      default: undefined,
    },
  },
})
</script>

<style scoped>
.sticky-bar {
  position: -webkit-sticky;
  position: sticky;
  z-index: 2
}
</style>
