import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaHistoryService from '@/services/komga-history.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaHistory = new KomgaHistoryService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaHistory: KomgaHistoryService;
  }
}
