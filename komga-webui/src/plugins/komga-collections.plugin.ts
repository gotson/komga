import KomgaCollectionsService from '@/services/komga-collections.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaCollections = new KomgaCollectionsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaCollections: KomgaCollectionsService;
  }
}
