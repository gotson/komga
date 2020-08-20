import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    // collections
    addToCollectionSeries: {} as SeriesDto | SeriesDto[],
    addToCollectionDialog: false,
    editCollection: {} as CollectionDto,
    editCollectionDialog: false,
    deleteCollection: {} as CollectionDto,
    deleteCollectionDialog: false,
    // read lists
    addToReadListBooks: {} as BookDto | BookDto[],
    addToReadListDialog: false,
    editReadList: {} as ReadListDto,
    editReadListDialog: false,
    deleteReadList: {} as ReadListDto,
    deleteReadListDialog: false,
    // libraries
    editLibrary: {} as LibraryDto | undefined,
    editLibraryDialog: false,
    deleteLibrary: {} as LibraryDto,
    deleteLibraryDialog: false,
    // books
    updateBooks: {} as BookDto | BookDto[],
    updateBooksDialog: false,
    // series
    updateSeries: {} as SeriesDto | SeriesDto[],
    updateSeriesDialog: false,
  },
  mutations: {
    // Collections
    setAddToCollectionSeries (state, series) {
      state.addToCollectionSeries = series
    },
    setAddToCollectionDialog (state, dialog) {
      state.addToCollectionDialog = dialog
    },
    setEditCollection (state, collection) {
      state.editCollection = collection
    },
    setEditCollectionDialog (state, dialog) {
      state.editCollectionDialog = dialog
    },
    setDeleteCollection (state, collection) {
      state.deleteCollection = collection
    },
    setDeleteCollectionDialog (state, dialog) {
      state.deleteCollectionDialog = dialog
    },
    // Read Lists
    setAddToReadListBooks (state, Book) {
      state.addToReadListBooks = Book
    },
    setAddToReadListDialog (state, dialog) {
      state.addToReadListDialog = dialog
    },
    setEditReadList (state, ReadList) {
      state.editReadList = ReadList
    },
    setEditReadListDialog (state, dialog) {
      state.editReadListDialog = dialog
    },
    setDeleteReadList (state, ReadList) {
      state.deleteReadList = ReadList
    },
    setDeleteReadListDialog (state, dialog) {
      state.deleteReadListDialog = dialog
    },
    // Libraries
    setEditLibrary (state, library) {
      state.editLibrary = library
    },
    setEditLibraryDialog (state, dialog) {
      state.editLibraryDialog = dialog
    },
    setDeleteLibrary (state, library) {
      state.deleteLibrary = library
    },
    setDeleteLibraryDialog (state, dialog) {
      state.deleteLibraryDialog = dialog
    },
    // Books
    setUpdateBooks (state, books) {
      state.updateBooks = books
    },
    setUpdateBooksDialog (state, dialog) {
      state.updateBooksDialog = dialog
    },
    // Series
    setUpdateSeries (state, series) {
      state.updateSeries = series
    },
    setUpdateSeriesDialog (state, dialog) {
      state.updateSeriesDialog = dialog
    },
  },
  actions: {
    // collections
    dialogAddSeriesToCollection ({ commit }, series) {
      commit('setAddToCollectionSeries', series)
      commit('setAddToCollectionDialog', true)
    },
    dialogAddSeriesToCollectionDisplay ({ commit }, value) {
      commit('setAddToCollectionDialog', value)
    },
    dialogEditCollection ({ commit }, collection) {
      commit('setEditCollection', collection)
      commit('setEditCollectionDialog', true)
    },
    dialogEditCollectionDisplay ({ commit }, value) {
      commit('setEditCollectionDialog', value)
    },
    dialogDeleteCollection ({ commit }, collection) {
      commit('setDeleteCollection', collection)
      commit('setDeleteCollectionDialog', true)
    },
    dialogDeleteCollectionDisplay ({ commit }, value) {
      commit('setDeleteCollectionDialog', value)
    },
    // read lists
    dialogAddBooksToReadList ({ commit }, books) {
      commit('setAddToReadListBooks', books)
      commit('setAddToReadListDialog', true)
    },
    dialogAddBooksToReadListDisplay ({ commit }, value) {
      commit('setAddToReadListDialog', value)
    },
    dialogEditReadList ({ commit }, readList) {
      commit('setEditReadList', readList)
      commit('setEditReadListDialog', true)
    },
    dialogEditReadListDisplay ({ commit }, value) {
      commit('setEditReadListDialog', value)
    },
    dialogDeleteReadList ({ commit }, readList) {
      commit('setDeleteReadList', readList)
      commit('setDeleteReadListDialog', true)
    },
    dialogDeleteReadListDisplay ({ commit }, value) {
      commit('setDeleteReadListDialog', value)
    },
    // libraries
    dialogAddLibrary ({ commit }) {
      commit('setEditLibrary', undefined)
      commit('setEditLibraryDialog', true)
    },
    dialogEditLibrary ({ commit }, value) {
      commit('setEditLibrary', value)
      commit('setEditLibraryDialog', true)
    },
    dialogEditLibraryDisplay ({ commit }, value) {
      commit('setEditLibraryDialog', value)
    },
    dialogDeleteLibrary ({ commit }, library) {
      commit('setDeleteLibrary', library)
      commit('setDeleteLibraryDialog', true)
    },
    dialogDeleteLibraryDisplay ({ commit }, value) {
      commit('setDeleteLibraryDialog', value)
    },
    // books
    dialogUpdateBooks ({ commit }, books) {
      commit('setUpdateBooks', books)
      commit('setUpdateBooksDialog', true)
    },
    dialogUpdateBooksDisplay ({ commit }, value) {
      commit('setUpdateBooksDialog', value)
    },
    // series
    dialogUpdateSeries ({ commit }, series) {
      commit('setUpdateSeries', series)
      commit('setUpdateSeriesDialog', true)
    },
    dialogUpdateSeriesDisplay ({ commit }, value) {
      commit('setUpdateSeriesDialog', value)
    },
  },
})
