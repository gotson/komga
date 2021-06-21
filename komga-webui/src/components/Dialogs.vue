<template>
  <div>
    <collection-add-to-dialog
      v-model="addToCollectionDialog"
      :series="addToCollectionSeries"
    />

    <collection-edit-dialog
      v-model="editCollectionDialog"
      :collection="editCollection"
    />

    <collection-delete-dialog
      v-model="deleteCollectionDialog"
      :collection="deleteCollection"
    />

    <read-list-add-to-dialog
      v-model="addToReadListDialog"
      :books="addToReadListBooks"
    />

    <read-list-edit-dialog
      v-model="editReadListDialog"
      :read-list="editReadList"
    />

    <read-list-delete-dialog
      v-model="deleteReadListDialog"
      :read-list="deleteReadList"
    />

    <library-edit-dialog
      v-model="editLibraryDialog"
      :library="editLibrary"
    />

    <library-delete-dialog
      v-model="deleteLibraryDialog"
      :library="deleteLibrary"
    />

    <edit-books-dialog
      v-model="updateBooksDialog"
      :books="updateBooks"
    />

    <edit-series-dialog
      v-model="updateSeriesDialog"
      :series="updateSeries"
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
import Vue from 'vue'
import ReadListAddToDialog from '@/components/dialogs/ReadListAddToDialog.vue'
import ReadListDeleteDialog from '@/components/dialogs/ReadListDeleteDialog.vue'
import ReadListEditDialog from '@/components/dialogs/ReadListEditDialog.vue'
import {BookDto} from '@/types/komga-books'
import {SeriesDto} from "@/types/komga-series";

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
})
</script>

<style scoped>

</style>
