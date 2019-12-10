<template>
  <v-row justify="center">
    <div class="text-center">
      <v-avatar color="grey lighten-3" size="400">
        <div>
          <v-icon class="secondary--text" size="140">mdi-help-circle</v-icon>
          <h1 class="headline">Page not found</h1>
          <p class="body-1">The page you are looking for doesn't exist.</p>
          <v-btn color="primary" :to="{name: 'home'}">Go back to home page</v-btn>
        </div>
      </v-avatar>
    </div>
  </v-row>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'PageNotFound'
})
</script>

<style scoped>

</style>
