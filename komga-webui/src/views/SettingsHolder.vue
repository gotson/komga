<template>
  <div>
    <v-tabs grow>
      <v-tab :to="{name: 'settings-users'}">{{ $t('users.users') }}</v-tab>
      <v-tab :to="{name: 'settings-server'}">{{ $t('server.tab_title') }}</v-tab>
      <v-tab :to="{name: 'metrics'}">{{ $t('metrics.title') }}</v-tab>
    </v-tabs>
    <router-view/>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SettingsHolder',
})
</script>

<style scoped>

</style>
