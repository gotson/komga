<template>
  <div>
    <v-tabs>
      <v-tab :to="{name: 'settings-analysis'}">
        <v-badge
          dot
          :value="booksToCheck"
          color="accent"
        >
          {{ $t('media_analysis.media_analysis') }}
        </v-badge>
      </v-tab>
      <v-tab :to="{name: 'settings-users'}">{{ $t('users.users') }}</v-tab>
      <v-tab :to="{name: 'settings-server'}">{{ $t('server.tab_title') }}</v-tab>
      <v-tab :to="{name: 'settings-data-import'}">{{ $t('data_import.tab_title') }}</v-tab>
    </v-tabs>
    <router-view/>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Settings',
  computed: {
    booksToCheck(): number {
      return this.$store.state.booksToCheck
    },
  },
})
</script>

<style scoped>

</style>
