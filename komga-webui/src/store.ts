import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    addToCollectionSeries: {} as SeriesDto | SeriesDto[],
    addToCollectionDialog: false,
    deleteCollection: {} as CollectionDto,
    deleteCollectionDialog: false,
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
  },
})
