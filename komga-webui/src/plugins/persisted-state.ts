import {Module} from 'vuex'
import {Theme} from '@/types/themes'

export const persistedModule: Module<any, any> = {
  state: {
    locale: '',
    theme: Theme.SYSTEM,
    webreader: {
      paged: {
        scale: '',
        pageLayout: '',
      },
      continuous: {
        scale: '',
        padding: '',
      },
      readingDirection: '',
      swipe: false,
      alwaysFullscreen: false,
      animations: true,
      background: '',
    },
    epubreader: {},
    browsingPageSize: undefined as unknown as number,
    collection: {
      filter: {},
    },
    readList: {
      filter: {},
    },
    library: {
      filter: {},
      sort: {},
      bookFilter: {},
      bookSort: {},
      route: {},
    },
    importPath: '',
    duplicatesNewPageSize: 10,
    rememberMe: false,
  },
  getters: {
    getLocaleFirstDay: (state) => () => {
      try {
        // @ts-ignore
        const loc = new Intl.Locale(state.locale)
        try {
          // @ts-ignore
          return loc.getWeekInfo().firstDay
        } catch (e) {
        }
        try {
          // @ts-ignore
          return loc.weekInfo.firstDay
        } catch (e) {
        }
      } catch (e) {
      }
      return 1
    },
    getCollectionFilter: (state) => (id: string) => {
      return state.collection.filter[id]
    },
    getReadListFilter: (state) => (id: string) => {
      return state.readList.filter[id]
    },
    getLibraryFilter: (state) => (id: string) => {
      return state.library.filter[id]
    },
    getLibrarySort: (state) => (id: string) => {
      return state.library.sort[id]
    },
    getLibraryBookFilter: (state) => (id: string) => {
      return state.library.bookFilter[id]
    },
    getLibraryBookSort: (state) => (id: string) => {
      return state.library.bookSort[id]
    },
    getLibraryRoute: (state) => (id: string) => {
      return state.library.route[id]
    },
  },
  mutations: {
    setLocale(state, val) {
      state.locale = val
    },
    setTheme(state, val) {
      state.theme = val
    },
    setWebreaderPagedScale(state, val) {
      state.webreader.paged.scale = val
    },
    setWebreaderPagedPageLayout(state, val) {
      state.webreader.paged.pageLayout = val
    },
    setWebreaderContinuousScale(state, val) {
      state.webreader.continuous.scale = val
    },
    setWebreaderContinuousPadding(state, val) {
      state.webreader.continuous.padding = val
    },
    setWebreaderReadingDirection(state, val) {
      state.webreader.readingDirection = val
    },
    setWebreaderSwipe(state, val) {
      state.webreader.swipe = val
    },
    setWebreaderAlwaysFullscreen(state, val) {
      state.webreader.alwaysFullscreen = val
    },
    setWebreaderAnimations(state, val) {
      state.webreader.animations = val
    },
    setWebreaderBackground(state, val) {
      state.webreader.background = val
    },
    setEpubreaderSettings(state, val) {
      state.epubreader = val
    },
    setBrowsingPageSize(state, val) {
      state.browsingPageSize = val
    },
    setCollectionFilter(state, {id, filter}) {
      state.collection.filter[id] = filter
    },
    setReadListFilter(state, {id, filter}) {
      state.readList.filter[id] = filter
    },
    setLibraryFilter(state, {id, filter}) {
      state.library.filter[id] = filter
    },
    setLibrarySort(state, {id, sort}) {
      state.library.sort[id] = sort
    },
    setLibraryBookFilter(state, {id, filter}) {
      state.library.bookFilter[id] = filter
    },
    setLibraryBookSort(state, {id, sort}) {
      state.library.bookSort[id] = sort
    },
    setLibraryRoute(state, {id, route}) {
      state.library.route[id] = route
    },
    setImportPath(state, val) {
      state.importPath = val ?? ''
    },
    setDuplicatesNewPageSize(state, val) {
      state.duplicatesNewPageSize = val
    },
    setRememberMe(state, val) {
      state.rememberMe = val
    },
  },
}
