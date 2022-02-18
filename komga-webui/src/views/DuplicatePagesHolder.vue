<template>
  <div>
    <v-tabs grow>
      <v-tab :to="{name: 'settings-duplicate-pages-known'}">Known</v-tab>
      <v-tab :to="{name: 'settings-duplicate-pages-unknown'}">New</v-tab>
    </v-tabs>
    <router-view/>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'DuplicatePagesHolder',
})
</script>

<style scoped>

</style>
