<template>
  <v-layout justify-center>
    <div class="text-center">
      <div>
        <v-img src="../assets/logo.svg"
               max-width="400"
        ></v-img>
        <h1 class="headline mt-4">Welcome to Komga</h1>
        <p class="body-1">The user interface is quite new an still a little scarce.</p>
        <v-btn color="primary" :to="{name: 'addlibrary'}">Add library</v-btn>
      </div>
    </div>
  </v-layout>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Welcome'
})
</script>

<style scoped>

</style>
