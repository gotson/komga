<template>
  <div>
    <collection-add-to-dialog
      v-model="addToCollectionDialog"
      :series="addToCollectionSeries"
      @added="collectionAdded"
    />

    <collection-delete-dialog
      v-model="deleteCollectionDialog"
      :collection="deleteCollection"
      @deleted="collectionDeleted"
    />

    <library-delete-dialog
      v-model="deleteLibraryDialog"
      :library="deleteLibrary"
      @deleted="libraryDeleted"
    />
  </div>
</template>

<script lang="ts">
import CollectionAddToDialog from '@/components/CollectionAddToDialog.vue'
import CollectionDeleteDialog from '@/components/CollectionDeleteDialog.vue'
import LibraryDeleteDialog from '@/components/LibraryDeleteDialog.vue'
import { COLLECTION_CHANGED, LIBRARY_DELETED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Dialogs',
  components: { CollectionAddToDialog, CollectionDeleteDialog, LibraryDeleteDialog },
  computed: {
    addToCollectionDialog: {
      get (): boolean {
        return this.$store.state.addToCollectionDialog
      },
      set (val) {
        this.$store.dispatch('dialogAddSeriesToCollectionDisplay', val)
      },
    },
    addToCollectionSeries (): SeriesDto | SeriesDto[] {
      return this.$store.state.addToCollectionSeries
    },
    deleteCollectionDialog: {
      get (): boolean {
        return this.$store.state.deleteCollectionDialog
      },
      set (val) {
        this.$store.dispatch('dialogDeleteCollectionDisplay', val)
      },
    },
    deleteCollection (): CollectionDto {
      return this.$store.state.deleteCollection
    },
    deleteLibraryDialog: {
      get (): boolean {
        return this.$store.state.deleteLibraryDialog
      },
      set (val) {
        this.$store.dispatch('dialogDeleteLibraryDisplay', val)
      },
    },
    deleteLibrary (): LibraryDto {
      return this.$store.state.deleteLibrary
    },
  },
  methods: {
    collectionAdded () {
      if (Array.isArray(this.addToCollectionSeries)) {
        this.addToCollectionSeries.forEach(s => {
          this.$eventHub.$emit(SERIES_CHANGED, {
            id: s.id,
            libraryId: s.libraryId,
          } as EventSeriesChanged)
        })
      } else {
        this.$eventHub.$emit(SERIES_CHANGED, {
          id: this.addToCollectionSeries.id,
          libraryId: this.addToCollectionSeries.libraryId,
        } as EventSeriesChanged)
      }
      this.$eventHub.$emit(COLLECTION_CHANGED)
    },
    collectionDeleted () {
      this.$eventHub.$emit(COLLECTION_CHANGED)
    },
    libraryDeleted () {
      this.$eventHub.$emit(LIBRARY_DELETED, {
        id: this.deleteLibrary.id,
      } as EventLibraryDeleted)
    },
  },
})
</script>

<style scoped>

</style>
