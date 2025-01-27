import Vue from 'vue'
import Vuex from 'vuex'
import {BookDto} from '@/types/komga-books'
import {Oneshot, SeriesDto} from '@/types/komga-series'
import createPersistedState from 'vuex-persistedstate'
import {persistedModule} from './plugins/persisted-state'
import {LibraryDto} from '@/types/komga-libraries'
import {ReadListDto} from '@/types/komga-readlists'
import {ItemDto, JsonFeedDto} from '@/types/json-feed'
import {isEmpty} from 'lodash'

Vue.use(Vuex)

const persistedState = createPersistedState({
  paths: ['persistedState'],
})

export default new Vuex.Store({
  state: {
    // collections
    addToCollectionSeriesIds: [] as string[],
    addToCollectionDialog: false,
    editCollection: {} as CollectionDto,
    editCollectionDialog: false,
    deleteCollections: {} as CollectionDto | CollectionDto[],
    deleteCollectionDialog: false,
    // read lists
    addToReadListBookIds: [] as string[],
    addToReadListDialog: false,
    editReadList: {} as ReadListDto,
    editReadListDialog: false,
    deleteReadLists: {} as ReadListDto | ReadListDto[],
    deleteReadListDialog: false,
    // libraries
    editLibrary: {} as LibraryDto | undefined,
    editLibraryDialog: false,
    deleteLibrary: {} as LibraryDto,
    deleteLibraryDialog: false,
    // books
    updateBooks: {} as BookDto | BookDto[],
    updateBooksDialog: false,
    deleteBooks: {} as BookDto | BookDto[],
    deleteBookDialog: false,
    // books bulk
    updateBulkBooks: [] as BookDto[],
    updateBulkBooksDialog: false,

    // oneshots
    updateOneshots: {} as Oneshot | Oneshot[],
    updateOneshotsDialog: false,

    // series
    updateSeries: {} as SeriesDto | SeriesDto[],
    updateSeriesDialog: false,
    deleteSeries: {} as SeriesDto | SeriesDto[],
    deleteSeriesDialog: false,

    booksToCheck: 0,

    announcements: {} as JsonFeedDto,

    actuatorInfo: {} as ActuatorInfo,

    releases: [] as ReleaseDto[],
  },
  getters: {
    getUnreadAnnouncementsCount: (state) => (): number => {
      return state.announcements?.items
        ?.filter((value: ItemDto) => false == value._komga?.read)
        ?.length || 0
    },
    isLatestVersion: (state) => (): number => {
      if(isEmpty(state.actuatorInfo)) return -1
      if(state.releases.length == 0) return -1
      if(state.actuatorInfo.build.version == state.releases.find((x: ReleaseDto) => x.latest)?.version) return 1
      else return 0
    },
  },
  mutations: {
    // Collections
    setAddToCollectionSeriesIds(state, seriesIds: string[]) {
      state.addToCollectionSeriesIds = seriesIds
    },
    setAddToCollectionDialog(state, dialog) {
      state.addToCollectionDialog = dialog
    },
    setEditCollection(state, collection) {
      state.editCollection = collection
    },
    setEditCollectionDialog(state, dialog) {
      state.editCollectionDialog = dialog
    },
    setDeleteCollections(state, collections) {
      state.deleteCollections = collections
    },
    setDeleteCollectionDialog(state, dialog) {
      state.deleteCollectionDialog = dialog
    },
    // Read Lists
    setAddToReadListBookIds(state, bookIds: string[]) {
      state.addToReadListBookIds = bookIds
    },
    setAddToReadListDialog(state, dialog) {
      state.addToReadListDialog = dialog
    },
    setEditReadList(state, readList) {
      state.editReadList = readList
    },
    setEditReadListDialog(state, dialog) {
      state.editReadListDialog = dialog
    },
    setDeleteReadLists(state, readLists) {
      state.deleteReadLists = readLists
    },
    setDeleteReadListDialog(state, dialog) {
      state.deleteReadListDialog = dialog
    },
    // Libraries
    setEditLibrary(state, library) {
      state.editLibrary = library
    },
    setEditLibraryDialog(state, dialog) {
      state.editLibraryDialog = dialog
    },
    setDeleteLibrary(state, library) {
      state.deleteLibrary = library
    },
    setDeleteLibraryDialog(state, dialog) {
      state.deleteLibraryDialog = dialog
    },
    // Books
    setUpdateBooks(state, books) {
      state.updateBooks = books
    },
    setUpdateBooksDialog(state, dialog) {
      state.updateBooksDialog = dialog
    },
    setDeleteBooks(state, books) {
      state.deleteBooks = books
    },
    setDeleteBookDialog(state, dialog) {
      state.deleteBookDialog = dialog
    },
    // Books bulk
    setUpdateBulkBooks(state, books) {
      state.updateBulkBooks = books
    },
    setUpdateBulkBooksDialog(state, dialog) {
      state.updateBulkBooksDialog = dialog
    },
    // One-shots
    setUpdateOneshots(state, oneshots) {
      state.updateOneshots = oneshots
    },
    setUpdateOneshotsDialog(state, dialog) {
      state.updateOneshotsDialog = dialog
    },
    // Series
    setUpdateSeries(state, series) {
      state.updateSeries = series
    },
    setUpdateSeriesDialog(state, dialog) {
      state.updateSeriesDialog = dialog
    },
    setBooksToCheck(state, count) {
      state.booksToCheck = count
    },
    setDeleteSeries(state, series) {
      state.deleteSeries = series
    },
    setDeleteSeriesDialog(state, dialog) {
      state.deleteSeriesDialog = dialog
    },
    setAnnouncements(state, announcements) {
      state.announcements = announcements
    },
    setActuatorInfo(state, info) {
      state.actuatorInfo = info
    },
    setReleases(state, releases) {
      state.releases = releases
    },
  },
  actions: {
    // collections
    dialogAddSeriesToCollection({commit}, seriesIds: string[]) {
      commit('setAddToCollectionSeriesIds', seriesIds)
      commit('setAddToCollectionDialog', true)
    },
    dialogAddSeriesToCollectionDisplay({commit}, value) {
      commit('setAddToCollectionDialog', value)
    },
    dialogEditCollection({commit}, collection) {
      commit('setEditCollection', collection)
      commit('setEditCollectionDialog', true)
    },
    dialogEditCollectionDisplay({commit}, value) {
      commit('setEditCollectionDialog', value)
    },
    dialogDeleteCollection({commit}, collections) {
      commit('setDeleteCollections', collections)
      commit('setDeleteCollectionDialog', true)
    },
    dialogDeleteCollectionDisplay({commit}, value) {
      commit('setDeleteCollectionDialog', value)
    },
    // read lists
    dialogAddBooksToReadList({commit}, bookIds: string[]) {
      commit('setAddToReadListBookIds', bookIds)
      commit('setAddToReadListDialog', true)
    },
    dialogAddBooksToReadListDisplay({commit}, value) {
      commit('setAddToReadListDialog', value)
    },
    dialogEditReadList({commit}, readList) {
      commit('setEditReadList', readList)
      commit('setEditReadListDialog', true)
    },
    dialogEditReadListDisplay({commit}, value) {
      commit('setEditReadListDialog', value)
    },
    dialogDeleteReadList({commit}, readLists) {
      commit('setDeleteReadLists', readLists)
      commit('setDeleteReadListDialog', true)
    },
    dialogDeleteReadListDisplay({commit}, value) {
      commit('setDeleteReadListDialog', value)
    },
    // libraries
    dialogAddLibrary({commit}) {
      commit('setEditLibrary', undefined)
      commit('setEditLibraryDialog', true)
    },
    dialogEditLibrary({commit}, value) {
      commit('setEditLibrary', value)
      commit('setEditLibraryDialog', true)
    },
    dialogEditLibraryDisplay({commit}, value) {
      commit('setEditLibraryDialog', value)
    },
    dialogDeleteLibrary({commit}, library) {
      commit('setDeleteLibrary', library)
      commit('setDeleteLibraryDialog', true)
    },
    dialogDeleteLibraryDisplay({commit}, value) {
      commit('setDeleteLibraryDialog', value)
    },
    // books
    dialogUpdateBooks({commit}, books) {
      commit('setUpdateBooks', books)
      commit('setUpdateBooksDialog', true)
    },
    dialogUpdateBooksDisplay({commit}, value) {
      commit('setUpdateBooksDialog', value)
    },
    dialogDeleteBook({commit}, books) {
      commit('setDeleteBooks', books)
      commit('setDeleteBookDialog', true)
    },
    dialogDeleteBookDisplay({commit}, value) {
      commit('setDeleteBookDialog', value)
    },
    // books bulk
    dialogUpdateBulkBooks({commit}, books) {
      commit('setUpdateBulkBooks', books)
      commit('setUpdateBulkBooksDialog', true)
    },
    dialogUpdateBulkBooksDisplay({commit}, value) {
      commit('setUpdateBulkBooksDialog', value)
    },
    // oneshots
    dialogUpdateOneshots({commit}, oneshots) {
      commit('setUpdateOneshots', oneshots)
      commit('setUpdateOneshotsDialog', true)
    },
    dialogUpdateOneshotsDisplay({commit}, value) {
      commit('setUpdateOneshotsDialog', value)
    },

    // series
    dialogUpdateSeries({commit}, series) {
      commit('setUpdateSeries', series)
      commit('setUpdateSeriesDialog', true)
    },
    dialogUpdateSeriesDisplay({commit}, value) {
      commit('setUpdateSeriesDialog', value)
    },
    dialogDeleteSeries({commit}, series) {
      commit('setDeleteSeries', series)
      commit('setDeleteSeriesDialog', true)
    },
    dialogDeleteSeriesDisplay({commit}, value) {
      commit('setDeleteSeriesDialog', value)
    },
  },
  modules: {
    persistedState: persistedModule,
  },
  plugins: [persistedState],
})
