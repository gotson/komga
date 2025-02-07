import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaSettingsService from '@/services/komga-settings.service'
import {Module} from 'vuex'
import {
  CLIENT_SETTING,
  ClientSettingDto,
  ClientSettingLibrary,
  ClientSettingLibraryUpdate,
  ClientSettingUserUpdateDto,
} from '@/types/komga-clientsettings'

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
    getClientSettingsLibraries(state): Record<string, ClientSettingLibrary> {
      let settings: Record<string, ClientSettingLibrary> = {}
      try {
        settings = JSON.parse(state.clientSettingsUser[CLIENT_SETTING.WEBUI_LIBRARIES]?.value)
      } catch (e) {
      }
      return settings
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
    async updateLibrarySetting({dispatch, getters}, update: ClientSettingLibraryUpdate) {
      const all = getters.getClientSettingsLibraries
      all[update.libraryId] = Object.assign({}, all[update.libraryId], update.patch)
      const newSettings = {} as Record<string, ClientSettingUserUpdateDto>
      newSettings[CLIENT_SETTING.WEBUI_LIBRARIES] = {
        value: JSON.stringify(all),
      }
      await service.updateClientSettingUser(newSettings)
      dispatch('getClientSettingsUser')
    },
    async updateLibrariesSettings({dispatch, getters}, updates: ClientSettingLibraryUpdate[]) {
      const all = getters.getClientSettingsLibraries
      updates.forEach(u => all[u.libraryId] = Object.assign({}, all[u.libraryId], u.patch))

      const newSettings = {} as Record<string, ClientSettingUserUpdateDto>
      newSettings[CLIENT_SETTING.WEBUI_LIBRARIES] = {
        value: JSON.stringify(all),
      }
      await service.updateClientSettingUser(newSettings)
      dispatch('getClientSettingsUser')
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
