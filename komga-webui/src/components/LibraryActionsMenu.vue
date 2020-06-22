<template>
  <div>
    <v-menu offset-y v-if="isAdmin">
      <template v-slot:activator="{ on }">
        <v-btn icon v-on="on" @click.prevent="">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list>
        <v-list-item @click="scan">
          <v-list-item-title>Scan library files</v-list-item-title>
        </v-list-item>
        <v-list-item @click="analyze">
          <v-list-item-title>Analyze</v-list-item-title>
        </v-list-item>
        <v-list-item @click="refreshMetadata">
          <v-list-item-title>Refresh metadata</v-list-item-title>
        </v-list-item>
        <v-list-item @click="promptDeleteLibrary"
                     class="list-warning">
          <v-list-item-title>Delete</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>

    <library-delete-dialog v-model="modalDeleteLibrary"
                           :library="library"
                           @deleted="navigateHome"
    />
  </div>
</template>
<script lang="ts">
import LibraryDeleteDialog from '@/components/LibraryDeleteDialog.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'LibraryActionsMenu',
  components: { LibraryDeleteDialog },
  data: function () {
    return {
      modalDeleteLibrary: false,
    }
  },
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
    promptDeleteLibrary () {
      this.modalDeleteLibrary = true
    },
    navigateHome () {
      this.$router.push({ name: 'home' })
    },
  },
})
</script>
<style scoped>
.list-warning:hover {
  background: #F44336;
}

.list-warning:hover .v-list-item__title {
  color: white;
}
</style>
