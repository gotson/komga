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
  </div>
</template>

<script lang="ts">
import CollectionAddToDialog from '@/components/CollectionAddToDialog.vue'
import CollectionDeleteDialog from '@/components/CollectionDeleteDialog.vue'
import { COLLECTION_CHANGED, SERIES_CHANGED } from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Dialogs',
  components: { CollectionAddToDialog, CollectionDeleteDialog },
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
  },
})
</script>

<style scoped>

</style>
