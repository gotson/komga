<template>
  <div class="ma-3">
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
import {LIBRARY_ADDED} from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Welcome',
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  created () {
    this.$eventHub.$on(LIBRARY_ADDED, this.libraryAdded)
  },
  beforeDestroy () {
    this.$eventHub.$off(LIBRARY_ADDED, this.libraryAdded)
  },
  mounted () {
    if (this.$store.state.komgaLibraries.libraries.length !== 0) {
      this.$router.push({ name: 'dashboard' })
    }
  },
  methods: {
    libraryAdded () {
      this.$router.push({ name: 'dashboard' })
    },
    addLibrary () {
      this.$store.dispatch('dialogAddLibrary')
    },
  },
})
</script>

<style scoped>

</style>
