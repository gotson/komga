import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaSettingsService from '@/services/komga-settings.service'
import {Module} from 'vuex'
import {LibraryDto} from '@/types/komga-libraries'
import {CLIENT_SETTING, ClientSettingDto} from '@/types/komga-clientsettings'

let service = KomgaSettingsService

const vuexModule: Module<any, any> = {
  state: {
    clientSettings: [] as ClientSettingDto[],
  },
  getters: {
    getClientSettingByKey: (state) => (key: string) => {
      return state.clientSettings.find((it: ClientSettingDto) => it.key === key)
    },
    getClientSettingPosterStretch(state): boolean {
      return state.clientSettings.find((it: ClientSettingDto) => it.key === CLIENT_SETTING.WEBUI_POSTER_STRETCH)?.value === 'true'
    },
  },
  mutations: {
    setClientSettings(state, settings) {
      state.clientSettings = settings
    },
  },
  actions: {
    async getClientSettings({commit}) {
      commit('setClientSettings', await service.getClientSettings())
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
