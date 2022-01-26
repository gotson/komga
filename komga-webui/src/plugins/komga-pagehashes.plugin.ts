import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaPageHashesService from '@/services/komga-pagehashes.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaPageHashes = new KomgaPageHashesService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaPageHashes: KomgaPageHashesService;
  }
}
