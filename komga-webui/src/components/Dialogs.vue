<template>
  <div>
    <collection-add-to-dialog
      v-model="addToCollectionDialog"
      :series="addToCollectionSeries"
      @added="collectionAdded"
      @created="collectionAdded"
    />

    <collection-edit-dialog
      v-model="editCollectionDialog"
      :collection="editCollection"
      @updated="collectionUpdated"
    />

    <collection-delete-dialog
      v-model="deleteCollectionDialog"
      :collection="deleteCollection"
      @deleted="collectionDeleted"
    />

    <library-edit-dialog
      v-model="editLibraryDialog"
      :library="editLibrary"
    />

    <library-delete-dialog
      v-model="deleteLibraryDialog"
      :library="deleteLibrary"
      @deleted="libraryDeleted"
    />

    <edit-books-dialog
      v-model="updateBooksDialog"
      :books="updateBooks"
      @updated="bookUpdated"
    />

    <edit-series-dialog
      v-model="updateSeriesDialog"
      :series="updateSeries"
      @updated="seriesUpdated"
    />

  </div>
</template>

<script lang="ts">
import CollectionAddToDialog from '@/components/dialogs/CollectionAddToDialog.vue'
import CollectionDeleteDialog from '@/components/dialogs/CollectionDeleteDialog.vue'
import CollectionEditDialog from '@/components/dialogs/CollectionEditDialog.vue'
import EditBooksDialog from '@/components/dialogs/EditBooksDialog.vue'
import EditSeriesDialog from '@/components/dialogs/EditSeriesDialog.vue'
import LibraryDeleteDialog from '@/components/dialogs/LibraryDeleteDialog.vue'
import LibraryEditDialog from '@/components/dialogs/LibraryEditDialog.vue'
import {
  BOOK_CHANGED,
  bookToEventBookChanged,
  COLLECTION_CHANGED,
  COLLECTION_DELETED,
  collectionToEventCollectionChanged,
  collectionToEventCollectionDeleted,
  LIBRARY_DELETED,
  libraryToEventLibraryDeleted,
  SERIES_CHANGED,
  seriesToEventSeriesChanged,
} from '@/types/events'
import Vue from 'vue'

export default Vue.extend({
  name: 'Dialogs',
  components: {
    CollectionAddToDialog,
    CollectionEditDialog,
    CollectionDeleteDialog,
    LibraryEditDialog,
    LibraryDeleteDialog,
    EditBooksDialog,
    EditSeriesDialog,
  },
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
    editCollectionDialog: {
      get (): boolean {
        return this.$store.state.editCollectionDialog
      },
      set (val) {
        this.$store.dispatch('dialogEditCollectionDisplay', val)
      },
    },
    editCollection (): CollectionDto {
      return this.$store.state.editCollection
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
    editLibraryDialog: {
      get (): boolean {
        return this.$store.state.editLibraryDialog
      },
      set (val) {
        this.$store.dispatch('dialogEditLibraryDisplay', val)
      },
    },
    editLibrary (): LibraryDto | undefined {
      return this.$store.state.editLibrary
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
    updateBooksDialog: {
      get (): boolean {
        return this.$store.state.updateBooksDialog
      },
      set (val) {
        this.$store.dispatch('dialogUpdateBooksDisplay', val)
      },
    },
    updateBooks (): BookDto | BookDto[] {
      return this.$store.state.updateBooks
    },
    updateSeriesDialog: {
      get (): boolean {
        return this.$store.state.updateSeriesDialog
      },
      set (val) {
        this.$store.dispatch('dialogUpdateSeriesDisplay', val)
      },
    },
    updateSeries (): SeriesDto | SeriesDto[] {
      return this.$store.state.updateSeries
    },
  },
  methods: {
    collectionAdded (collection: CollectionDto) {
      if (Array.isArray(this.addToCollectionSeries)) {
        this.addToCollectionSeries.forEach(s => {
          this.$eventHub.$emit(SERIES_CHANGED, seriesToEventSeriesChanged(s))
        })
      } else {
        this.$eventHub.$emit(SERIES_CHANGED, seriesToEventSeriesChanged(this.addToCollectionSeries))
      }
      this.$eventHub.$emit(COLLECTION_CHANGED, collectionToEventCollectionChanged(collection))
    },
    collectionUpdated () {
      this.$eventHub.$emit(COLLECTION_CHANGED, collectionToEventCollectionChanged(this.editCollection))
    },
    collectionDeleted () {
      this.$eventHub.$emit(COLLECTION_DELETED, collectionToEventCollectionDeleted(this.deleteCollection))
    },
    libraryDeleted () {
      this.$eventHub.$emit(LIBRARY_DELETED, libraryToEventLibraryDeleted(this.deleteLibrary))
    },
    bookUpdated (book: BookDto) {
      this.$eventHub.$emit(BOOK_CHANGED, bookToEventBookChanged(book))
    },
    seriesUpdated (series: SeriesDto) {
      this.$eventHub.$emit(SERIES_CHANGED, seriesToEventSeriesChanged(series))
    },
  },
})
</script>

<style scoped>

</style>
