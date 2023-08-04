<template>
  <div>
    <collection-add-to-dialog
      v-model="addToCollectionDialog"
      :series-ids="addToCollectionSeriesIds"
    />

    <collection-edit-dialog
      v-model="editCollectionDialog"
      :collection="editCollection"
    />

    <confirmation-dialog
      v-model="deleteCollectionDialog"
      :title="collectionsToDeleteSingle ? $t('dialog.delete_collection.dialog_title') : $t('dialog.delete_collection.dialog_title_multiple')"
      :body-html="collectionsToDeleteSingle ? $t('dialog.delete_collection.warning_html', { name: collectionsToDelete.name}) : $t('dialog.delete_collection.warning_multiple_html', { count: collectionsToDelete.length})"
      :confirm-text="collectionsToDeleteSingle ? $t('dialog.delete_collection.confirm_delete', {name: collectionsToDelete.name}) : $t('dialog.delete_collection.confirm_delete_multiple', {count: collectionsToDelete.length})"
      :button-confirm="$t('dialog.delete_collection.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteCollections"
    />

    <read-list-add-to-dialog
      v-model="addToReadListDialog"
      :bookIds="addToReadListBookIds"
    />

    <read-list-edit-dialog
      v-model="editReadListDialog"
      :read-list="editReadList"
    />

    <confirmation-dialog
      v-model="deleteReadListDialog"
      :title="readListsToDeleteSingle ? $t('dialog.delete_readlist.dialog_title') : $t('dialog.delete_readlist.dialog_title_multiple')"
      :body-html="readListsToDeleteSingle ? $t('dialog.delete_readlist.warning_html', {name: readListsToDelete.name}) : $t('dialog.delete_readlist.warning_multiple_html', {count: readListsToDelete.length})"
      :confirm-text="readListsToDeleteSingle ? $t('dialog.delete_readlist.confirm_delete', {name: readListsToDelete.name}) : $t('dialog.delete_readlist.confirm_delete_multiple', {count: readListsToDelete.length})"
      :button-confirm="$t('dialog.delete_readlist.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteReadLists"
    />

    <library-edit-dialog
      v-model="editLibraryDialog"
      :library="editLibrary"
    />

    <confirmation-dialog
      v-model="deleteLibraryDialog"
      :title="$t('dialog.delete_library.title')"
      :body-html="$t('dialog.delete_library.warning_html', {name: libraryToDelete.name})"
      :confirm-text="$t('dialog.delete_library.confirm_delete', {name: libraryToDelete.name})"
      :button-confirm="$t('dialog.delete_library.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteLibrary"
    />

    <edit-books-dialog
      v-model="updateBooksDialog"
      :books="updateBooks"
    />

    <bulk-edit-books-dialog
      v-model="updateBulkBooksDialog"
      :books="updateBulkBooks"
    />

    <edit-oneshot-dialog
      v-model="updateOneshotsDialog"
      :oneshots="updateOneshots"
    />

    <edit-series-dialog
      v-model="updateSeriesDialog"
      :series="updateSeries"
    />

    <confirmation-dialog
      v-model="deleteSeriesDialog"
      :title="$t('dialog.delete_series.dialog_title')"
      :body-html="seriesToDeleteSingle ? $t('dialog.delete_series.warning_html', {name: seriesToDelete.name}) : $t('dialog.delete_series.warning_multiple_html', {count: seriesToDelete.length})"
      :confirm-text="seriesToDeleteSingle ? $t('dialog.delete_series.confirm_delete', {name: seriesToDelete.name}) : $t('dialog.delete_series.confirm_delete_multiple', {count: seriesToDelete.length})"
      :button-confirm="$t('dialog.delete_series.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteSeries"
    />

    <confirmation-dialog
      v-model="deleteBookDialog"
      :title="booksToDeleteSingle ? $t('dialog.delete_book.dialog_title') : $t('dialog.delete_book.dialog_title_multiple')"
      :body-html="booksToDeleteSingle ? $t('dialog.delete_book.warning_html', {name: booksToDelete.name}) : $t('dialog.delete_book.warning_multiple_html', {count: booksToDelete.length})"
      :confirm-text="booksToDeleteSingle ? $t('dialog.delete_book.confirm_delete', {name: booksToDelete.name}) : $t('dialog.delete_book.confirm_delete_multiple', {count: booksToDelete.length})"
      :button-confirm="$t('dialog.delete_book.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteBooks"
    />

  </div>
</template>

<script lang="ts">
import CollectionAddToDialog from '@/components/dialogs/CollectionAddToDialog.vue'
import CollectionEditDialog from '@/components/dialogs/CollectionEditDialog.vue'
import EditBooksDialog from '@/components/dialogs/EditBooksDialog.vue'
import EditSeriesDialog from '@/components/dialogs/EditSeriesDialog.vue'
import LibraryEditDialog from '@/components/dialogs/LibraryEditDialog.vue'
import Vue from 'vue'
import ReadListAddToDialog from '@/components/dialogs/ReadListAddToDialog.vue'
import ReadListEditDialog from '@/components/dialogs/ReadListEditDialog.vue'
import {BookDto} from '@/types/komga-books'
import {Oneshot, SeriesDto} from '@/types/komga-series'
import {ERROR} from '@/types/events'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'
import {LibraryDto} from '@/types/komga-libraries'
import BulkEditBooksDialog from '@/components/dialogs/BulkEditBooksDialog.vue'
import {ReadListDto} from '@/types/komga-readlists'
import EditOneshotDialog from '@/components/dialogs/EditOneshotDialog.vue'

export default Vue.extend({
  name: 'ReusableDialogs',
  components: {
    EditOneshotDialog,
    BulkEditBooksDialog,
    ConfirmationDialog,
    CollectionAddToDialog,
    CollectionEditDialog,
    ReadListAddToDialog,
    ReadListEditDialog,
    LibraryEditDialog,
    EditBooksDialog,
    EditSeriesDialog,
  },
  computed: {
    // collections
    addToCollectionDialog: {
      get(): boolean {
        return this.$store.state.addToCollectionDialog
      },
      set(val) {
        this.$store.dispatch('dialogAddSeriesToCollectionDisplay', val)
      },
    },
    addToCollectionSeriesIds(): string[] {
      return this.$store.state.addToCollectionSeriesIds
    },
    editCollectionDialog: {
      get(): boolean {
        return this.$store.state.editCollectionDialog
      },
      set(val) {
        this.$store.dispatch('dialogEditCollectionDisplay', val)
      },
    },
    editCollection(): CollectionDto {
      return this.$store.state.editCollection
    },
    deleteCollectionDialog: {
      get(): boolean {
        return this.$store.state.deleteCollectionDialog
      },
      set(val) {
        this.$store.dispatch('dialogDeleteCollectionDisplay', val)
      },
    },
    collectionsToDelete(): CollectionDto | CollectionDto[] {
      return this.$store.state.deleteCollections
    },
    collectionsToDeleteSingle(): boolean {
      return !Array.isArray(this.collectionsToDelete)
    },
    // read lists
    addToReadListDialog: {
      get(): boolean {
        return this.$store.state.addToReadListDialog
      },
      set(val) {
        this.$store.dispatch('dialogAddBooksToReadListDisplay', val)
      },
    },
    addToReadListBookIds(): string[] {
      return this.$store.state.addToReadListBookIds
    },
    editReadListDialog: {
      get(): boolean {
        return this.$store.state.editReadListDialog
      },
      set(val) {
        this.$store.dispatch('dialogEditReadListDisplay', val)
      },
    },
    editReadList(): ReadListDto {
      return this.$store.state.editReadList
    },
    deleteReadListDialog: {
      get(): boolean {
        return this.$store.state.deleteReadListDialog
      },
      set(val) {
        this.$store.dispatch('dialogDeleteReadListDisplay', val)
      },
    },
    readListsToDelete(): ReadListDto | ReadListDto[] {
      return this.$store.state.deleteReadLists
    },
    readListsToDeleteSingle(): boolean {
      return !Array.isArray(this.readListsToDelete)
    },
    // libraries
    editLibraryDialog: {
      get(): boolean {
        return this.$store.state.editLibraryDialog
      },
      set(val) {
        this.$store.dispatch('dialogEditLibraryDisplay', val)
      },
    },
    editLibrary(): LibraryDto | undefined {
      return this.$store.state.editLibrary
    },
    deleteLibraryDialog: {
      get(): boolean {
        return this.$store.state.deleteLibraryDialog
      },
      set(val) {
        this.$store.dispatch('dialogDeleteLibraryDisplay', val)
      },
    },
    libraryToDelete(): LibraryDto {
      return this.$store.state.deleteLibrary
    },
    // books
    updateBooksDialog: {
      get(): boolean {
        return this.$store.state.updateBooksDialog
      },
      set(val) {
        this.$store.dispatch('dialogUpdateBooksDisplay', val)
      },
    },
    updateBooks(): BookDto | BookDto[] {
      return this.$store.state.updateBooks
    },
    // books bulk
    updateBulkBooksDialog: {
      get(): boolean {
        return this.$store.state.updateBulkBooksDialog
      },
      set(val) {
        this.$store.dispatch('dialogUpdateBulkBooksDisplay', val)
      },
    },
    updateBulkBooks(): BookDto[] {
      return this.$store.state.updateBulkBooks
    },
    deleteBookDialog: {
      get(): boolean {
        return this.$store.state.deleteBookDialog
      },
      set(val) {
        this.$store.dispatch('dialogDeleteBookDisplay', val)
      },
    },
    booksToDelete(): BookDto | BookDto[] {
      return this.$store.state.deleteBooks
    },
    booksToDeleteSingle(): boolean {
      return !Array.isArray(this.booksToDelete)
    },
    // oneshots
    updateOneshotsDialog: {
      get(): boolean {
        return this.$store.state.updateOneshotsDialog
      },
      set(val) {
        this.$store.dispatch('dialogUpdateOneshotsDisplay', val)
      },
    },
    updateOneshots(): Oneshot | Oneshot[] {
      return this.$store.state.updateOneshots
    },
    // series
    updateSeriesDialog: {
      get(): boolean {
        return this.$store.state.updateSeriesDialog
      },
      set(val) {
        this.$store.dispatch('dialogUpdateSeriesDisplay', val)
      },
    },
    updateSeries(): SeriesDto | SeriesDto[] {
      return this.$store.state.updateSeries
    },
    deleteSeriesDialog: {
      get(): boolean {
        return this.$store.state.deleteSeriesDialog
      },
      set(val) {
        this.$store.dispatch('dialogDeleteSeriesDisplay', val)
      },
    },
    seriesToDelete(): SeriesDto | SeriesDto[] {
      return this.$store.state.deleteSeries
    },
    seriesToDeleteSingle(): boolean {
      return !Array.isArray(this.seriesToDelete)
    },
  },
  methods: {
    async deleteLibrary() {
      try {
        await this.$store.dispatch('deleteLibrary', this.libraryToDelete)
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    async deleteReadLists() {
      const toUpdate = (this.readListsToDeleteSingle ? [this.readListsToDelete] : this.readListsToDelete) as ReadListDto[]
      for (const b of toUpdate) {
        try {
          await this.$komgaReadLists.deleteReadList(b.id)
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
    async deleteCollections() {
      const toUpdate = (this.collectionsToDeleteSingle ? [this.collectionsToDelete] : this.collectionsToDelete) as CollectionDto[]
      for (const b of toUpdate) {
        try {
          await this.$komgaCollections.deleteCollection(b.id)
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
    async deleteSeries() {
      const toUpdate = (this.seriesToDeleteSingle ? [this.seriesToDelete] : this.seriesToDelete) as SeriesDto[]
      for (const b of toUpdate) {
        try {
          await this.$komgaSeries.deleteSeries(b.id)
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
    async deleteBooks() {
      const toUpdate = (this.booksToDeleteSingle ? [this.booksToDelete] : this.booksToDelete) as BookDto[]
      for (const b of toUpdate) {
        try {
          await this.$komgaBooks.deleteBook(b.id)
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
  },
})
</script>

<style scoped>

</style>
