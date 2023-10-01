import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaSettingsService from '@/services/komga-settings.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaSettings = new KomgaSettingsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaSettings: KomgaSettingsService;
  }
}
