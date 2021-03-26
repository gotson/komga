import {Module} from "vuex"
import {Theme} from "@/types/themes";

export const persistedModule: Module<any, any> = {
  state: {
    locale: '',
    theme: Theme.LIGHT,
    webreader: {
      fit: '',
      continuousReaderFit: '',
      continuousReaderPadding: '',
      readingDirection: '',
      pageLayout: '',
      swipe: false,
      animations: true,
      background: '',
    },
  },
  mutations: {
    setLocale (state, val) {
      state.locale = val
    },
    setTheme (state, val) {
      state.theme = val
    },
    setWebreaderFit (state, val) {
      state.webreader.fit = val
    },
    setWebreaderContinuousReaderFit (state, val) {
      state.webreader.continuousReaderFit = val
    },
    setWebreaderContinuousReaderPadding (state, val) {
      state.webreader.continuousReaderPadding = val
    },
    setWebreaderReadingDirection (state, val) {
      state.webreader.readingDirection = val
    },
    setWebreaderPageLayout (state, val) {
      state.webreader.pageLayout = val
    },
    setWebreaderSwipe (state, val) {
      state.webreader.swipe = val
    },
    setWebreaderAnimations (state, val) {
      state.webreader.animations = val
    },
    setWebreaderBackground (state, val) {
      state.webreader.background = val
    },
  },
}
