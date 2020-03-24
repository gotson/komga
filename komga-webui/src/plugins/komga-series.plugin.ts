import KomgaSeriesService from '@/services/komga-series.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaSeries = new KomgaSeriesService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaSeries: KomgaSeriesService;
  }
}
