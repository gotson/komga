<template>
  <div>
    <v-tabs>
      <v-tab :to="{name: 'settings-analysis'}">Media analysis</v-tab>
      <v-tab :to="{name: 'settings-users'}">Users</v-tab>
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
