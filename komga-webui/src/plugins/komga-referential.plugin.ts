import KomgaReferentialService from '@/services/komga-referential.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaReferential = new KomgaReferentialService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaReferential: KomgaReferentialService;
  }
}
