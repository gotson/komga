import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaSyncPointsService from '@/services/komga-syncpoints.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaSyncPoints = new KomgaSyncPointsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaSyncPoints: KomgaSyncPointsService;
  }
}
