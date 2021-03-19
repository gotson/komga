<template>
  <div>
    <v-tabs>
      <v-tab :to="{name: 'settings-analysis'}">{{ $t('media_analysis.media_analysis') }}</v-tab>
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
})
</script>

<style scoped>

</style>
