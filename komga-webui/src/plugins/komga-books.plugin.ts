import KomgaBooksService from '@/services/komga-books.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaBooks = new KomgaBooksService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaBooks: KomgaBooksService;
  }
}
