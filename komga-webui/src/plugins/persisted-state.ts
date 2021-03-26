import {Module} from "vuex"
import {Theme} from "@/types/themes";

export const persistedModule: Module<any, any> = {
  state: {
    locale: '',
    theme: Theme.LIGHT,
  },
  mutations: {
    setLocale (state, val) {
      state.locale = val
    },
    setTheme (state, val) {
      state.theme = val
    },
  },
}
