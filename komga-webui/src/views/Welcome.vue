<template>
  <div class="ma-3">
    <v-row align="center" justify="center">
      <v-img src="../assets/logo.svg"
             max-width="400"
      />
    </v-row>
    <v-row align="center" justify="center">
      <div class="text-center">
        <h1 class="headline mt-4">Welcome to Komga</h1>
        <p class="body-1">No libraries have been added yet!</p>
        <v-btn color="primary" :to="{name: 'addlibrary'}" v-if="isAdmin">Add library</v-btn>
      </div>
    </v-row>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Welcome',
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  mounted () {
    if (this.$store.state.komgaLibraries.libraries.length !== 0) {
      this.$router.push({ name: 'dashboard' })
    }
  },
})
</script>

<style scoped>

</style>
