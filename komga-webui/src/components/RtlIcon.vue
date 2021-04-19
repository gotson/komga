<template>
  <v-icon v-if="$vuetify.rtl">{{ rtl }}</v-icon>
  <v-icon v-else>{{ icon }}</v-icon>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'RtlIcon',
  props: {
    icon: {
      type: String,
      required: true,
    },
    rtl: {
      type: String,
      required: true,
    },
  },
})
</script>

<style scoped>

</style>
