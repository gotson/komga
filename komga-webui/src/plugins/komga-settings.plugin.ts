import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaSettingsService from '@/services/komga-settings.service'
import {Module} from 'vuex'
import {ClientSettingDto} from '@/types/komga-clientsettings'

let service: KomgaSettingsService

const vuexModule: Module<any, any> = {
  state: {
    clientSettingsGlobal: {} as Record<string, ClientSettingDto>,
    clientSettingsUser: {} as Record<string, ClientSettingDto>,
  },
  getters: {
    getClientSettings(state): Record<string, ClientSettingDto> {
      return {...state.clientSettingsGlobal, ...state.clientSettingsUser}
    },
  },
  mutations: {
    setClientSettingsGlobal(state, settings) {
      state.clientSettingsGlobal = settings
    },
    setClientSettingsUser(state, settings) {
      state.clientSettingsUser = settings
    },
  },
  actions: {
    async getClientSettingsGlobal({commit}) {
      commit('setClientSettingsGlobal', await service.getClientSettingsGlobal())
    },
    async getClientSettingsUser({commit}) {
      commit('setClientSettingsUser', await service.getClientSettingsUser())
    },
  },
}

export default {
  install(
    Vue: typeof _Vue,
    {store, http}: { store: any, http: AxiosInstance }) {
    service = new KomgaSettingsService(http)
    Vue.prototype.$komgaSettings = new KomgaSettingsService(http)

    store.registerModule('komgaSettings', vuexModule)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaSettings: KomgaSettingsService;
  }
}
