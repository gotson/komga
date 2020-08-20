import { AxiosInstance } from 'axios'
import _Vue from 'vue'
import KomgaReadListsService from '@/services/komga-readlists.service'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaReadLists = new KomgaReadListsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaReadLists: KomgaReadListsService;
  }
}
