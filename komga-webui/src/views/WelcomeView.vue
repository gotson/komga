<template>
  <div class="pa-6">
    <v-row align="center" justify="center">
      <v-img src="../assets/logo.svg"
             max-width="400"
      />
    </v-row>
    <v-row align="center" justify="center">
      <div class="text-center">
        <h1 class="text-h5 mt-4">{{ $t('welcome.welcome_message') }}</h1>
        <p class="text-body-1">{{ $t('welcome.no_libraries_yet') }}</p>
        <v-btn color="primary" @click="addLibrary" v-if="isAdmin">{{ $t('welcome.add_library') }}</v-btn>
      </div>
    </v-row>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'WelcomeView',
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
  watch: {
    '$store.state.komgaLibraries.libraries': {
      handler(val){
        if(val.length !== 0) this.$router.push({name: 'dashboard'})
      },
    },
  },
  methods: {
    addLibrary () {
      this.$store.dispatch('dialogAddLibrary')
    },
  },
})
</script>

<style scoped>

</style>
