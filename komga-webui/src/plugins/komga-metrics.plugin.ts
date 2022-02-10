import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaMetricsService from '@/services/komga-metrics.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaMetrics = new KomgaMetricsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaMetrics: KomgaMetricsService;
  }
}
