<template>
  <div>
    <v-tabs grow>
      <v-tab :to="{name: 'settings-duplicate-pages-known'}">{{ $t('duplicate_pages.known') }}</v-tab>
      <v-tab :to="{name: 'settings-duplicate-pages-unknown'}">{{ $t('duplicate_pages.new') }}</v-tab>
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
