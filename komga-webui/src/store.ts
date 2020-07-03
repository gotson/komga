import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    addToCollectionSeries: {} as SeriesDto | SeriesDto[],
    addToCollectionDialog: false,
    editCollection: {} as CollectionDto,
    editCollectionDialog: false,
    deleteCollection: {} as CollectionDto,
    deleteCollectionDialog: false,
    editLibrary: {} as LibraryDto | undefined,
    editLibraryDialog: false,
    deleteLibrary: {} as LibraryDto,
    deleteLibraryDialog: false,
    updateBooks: {} as BookDto | BookDto[],
    updateBooksDialog: false,
    updateSeries: {} as SeriesDto | SeriesDto[],
    updateSeriesDialog: false,
  },
  mutations: {
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
    setEditLibrary (state, library) {
      state.editLibrary = library
    },
    setEditLibraryDialog (state, dialog) {
      state.editLibraryDialog = dialog
    },
    setDeleteCollectionDialog (state, dialog) {
      state.deleteCollectionDialog = dialog
    },
    setDeleteLibrary (state, library) {
      state.deleteLibrary = library
    },
    setDeleteLibraryDialog (state, dialog) {
      state.deleteLibraryDialog = dialog
    },
    setUpdateBooks (state, books) {
      state.updateBooks = books
    },
    setUpdateBooksDialog (state, dialog) {
      state.updateBooksDialog = dialog
    },
    setUpdateSeries (state, series) {
      state.updateSeries = series
    },
    setUpdateSeriesDialog (state, dialog) {
      state.updateSeriesDialog = dialog
    },
  },
  actions: {
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
    dialogUpdateBooks ({ commit }, books) {
      commit('setUpdateBooks', books)
      commit('setUpdateBooksDialog', true)
    },
    dialogUpdateBooksDisplay ({ commit }, value) {
      commit('setUpdateBooksDialog', value)
    },
    dialogUpdateSeries ({ commit }, series) {
      commit('setUpdateSeries', series)
      commit('setUpdateSeriesDialog', true)
    },
    dialogUpdateSeriesDisplay ({ commit }, value) {
      commit('setUpdateSeriesDialog', value)
    },
  },
})
