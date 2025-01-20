<template>
  <div>
    <v-tabs grow>
      <v-tab :to="{name: 'media-analysis'}">
        <v-badge
          dot
          :value="booksToCheck"
          color="accent"
        >
          {{ $t('media_analysis.media_analysis') }}
        </v-badge>
      </v-tab>
      <v-tab :to="{name: 'missing-posters'}">{{ $t('missing_posters.title') }}</v-tab>
      <v-tab :to="{name: 'duplicate-files'}">{{ $t('duplicates.title') }}</v-tab>
      <v-tab :to="{name: 'duplicate-pages'}">{{ $t('duplicate_pages.title') }}</v-tab>
    </v-tabs>
    <router-view/>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'MediaManagement',
  computed: {
    booksToCheck(): number {
      return this.$store.state.booksToCheck
    },
  },
})
</script>

<style scoped>

</style>
