<template>
  <div class="pa-6">
    <v-row align="center" justify="center">
      <v-img src="../assets/logo.svg"
             max-width="400"
      />
    </v-row>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Startup',
  async mounted () {
    try {
      await this.$store.dispatch('getMe')
      await this.$store.dispatch('getLibraries')
      this.$router.back()
    } catch (e) {
      this.$router.push({ name: 'login', query: { redirect: this.$route.query.redirect } })
    }
  },
})
</script>

<style scoped>

</style>
