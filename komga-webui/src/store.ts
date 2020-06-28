import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    addToCollectionSeries: {} as SeriesDto | SeriesDto[],
    addToCollectionDialog: false,
    deleteCollection: {} as CollectionDto,
    deleteCollectionDialog: false,
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
    setDeleteCollection (state, collection) {
      state.deleteCollection = collection
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
    dialogDeleteCollection ({ commit }, collection) {
      commit('setDeleteCollection', collection)
      commit('setDeleteCollectionDialog', true)
    },
    dialogDeleteCollectionDisplay ({ commit }, value) {
      commit('setDeleteCollectionDialog', value)
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
