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

    <read-list-add-to-dialog
      v-model="addToReadListDialog"
      :books="addToReadListBooks"
      @added="readListAdded"
      @created="readListAdded"
    />

    <read-list-edit-dialog
      v-model="editReadListDialog"
      :read-list="editReadList"
      @updated="readListUpdated"
    />

    <read-list-delete-dialog
      v-model="deleteReadListDialog"
      :read-list="deleteReadList"
      @deleted="readListDeleted"
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
  READLIST_CHANGED,
  READLIST_DELETED,
  readListToEventReadListChanged,
  readListToEventReadListDeleted,
  SERIES_CHANGED,
  seriesToEventSeriesChanged,
} from '@/types/events'
import Vue from 'vue'
import ReadListAddToDialog from '@/components/dialogs/ReadListAddToDialog.vue'
import ReadListDeleteDialog from '@/components/dialogs/ReadListDeleteDialog.vue'
import ReadListEditDialog from '@/components/dialogs/ReadListEditDialog.vue'
import { BookDto } from '@/types/komga-books'

export default Vue.extend({
  name: 'Dialogs',
  components: {
    CollectionAddToDialog,
    CollectionEditDialog,
    CollectionDeleteDialog,
    ReadListAddToDialog,
    ReadListEditDialog,
    ReadListDeleteDialog,
    LibraryEditDialog,
    LibraryDeleteDialog,
    EditBooksDialog,
    EditSeriesDialog,
  },
  computed: {
    // collections
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
    // read lists
    addToReadListDialog: {
      get (): boolean {
        return this.$store.state.addToReadListDialog
      },
      set (val) {
        this.$store.dispatch('dialogAddBooksToReadListDisplay', val)
      },
    },
    addToReadListBooks (): BookDto | BookDto[] {
      return this.$store.state.addToReadListBooks
    },
    editReadListDialog: {
      get (): boolean {
        return this.$store.state.editReadListDialog
      },
      set (val) {
        this.$store.dispatch('dialogEditReadListDisplay', val)
      },
    },
    editReadList (): ReadListDto {
      return this.$store.state.editReadList
    },
    deleteReadListDialog: {
      get (): boolean {
        return this.$store.state.deleteReadListDialog
      },
      set (val) {
        this.$store.dispatch('dialogDeleteReadListDisplay', val)
      },
    },
    deleteReadList (): ReadListDto {
      return this.$store.state.deleteReadList
    },
    // libraries
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
    // books
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
    // series
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
    readListAdded (readList: ReadListDto) {
      if (Array.isArray(this.addToReadListBooks)) {
        this.addToReadListBooks.forEach(b => {
          this.$eventHub.$emit(BOOK_CHANGED, bookToEventBookChanged(b))
        })
      } else {
        this.$eventHub.$emit(BOOK_CHANGED, bookToEventBookChanged(this.addToReadListBooks))
      }
      this.$eventHub.$emit(READLIST_CHANGED, readListToEventReadListChanged(readList))
    },
    readListUpdated () {
      this.$eventHub.$emit(READLIST_CHANGED, readListToEventReadListChanged(this.editReadList))
    },
    readListDeleted () {
      this.$eventHub.$emit(READLIST_DELETED, readListToEventReadListDeleted(this.deleteReadList))
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
