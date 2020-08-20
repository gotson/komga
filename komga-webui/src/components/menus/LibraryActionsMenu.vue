<template>
  <div>
    <v-menu offset-y v-if="isAdmin">
      <template v-slot:activator="{ on }">
        <v-btn icon v-on="on" @click.prevent="">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item @click="scan">
          <v-list-item-title>Scan library files</v-list-item-title>
        </v-list-item>
        <v-list-item @click="analyze">
          <v-list-item-title>Analyze</v-list-item-title>
        </v-list-item>
        <v-list-item @click="refreshMetadata">
          <v-list-item-title>Refresh metadata</v-list-item-title>
        </v-list-item>
        <v-list-item @click="edit">
          <v-list-item-title>Edit</v-list-item-title>
        </v-list-item>
        <v-list-item @click="promptDeleteLibrary"
                     class="list-warning">
          <v-list-item-title>Delete</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'LibraryActionsMenu',
  props: {
    library: {
      type: Object as () => LibraryDto,
      required: true,
    },
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    scan () {
      this.$komgaLibraries.scanLibrary(this.library)
    },
    analyze () {
      this.$komgaLibraries.analyzeLibrary(this.library)
    },
    refreshMetadata () {
      this.$komgaLibraries.refreshMetadata(this.library)
    },
    edit () {
      this.$store.dispatch('dialogEditLibrary', this.library)
    },
    promptDeleteLibrary () {
      this.$store.dispatch('dialogDeleteLibrary', this.library)
    },
  },
})
</script>
<style scoped>
@import "../../styles/list-warning.css";
</style>
