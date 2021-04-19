import KomgaTransientBooksService from '@/services/komga-transientbooks.service'
import {AxiosInstance} from 'axios'
import _Vue from 'vue'

let service: KomgaTransientBooksService

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    service = new KomgaTransientBooksService(http)
    Vue.prototype.$komgaTransientBooks = service
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaTransientBooks: KomgaTransientBooksService;
  }
}
