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
  name: 'StartupView',
  async mounted() {
    try {
      if (this.$route.query.xAuthToken) {
        try {
          await this.$komgaLogin.setCookie(this.$route.query.xAuthToken.toString())
        } catch (e) {
          this.$debug(e.message)
        }
      }

      await this.$store.dispatch('getMe')
      await this.$store.dispatch('getLibraries')
      this.$router.back()
    } catch (e) {
      this.$router.push({name: 'login', query: {redirect: this.$route.query.redirect}})
    }
  },
})
</script>

<style scoped>

</style>
