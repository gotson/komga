import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaOauth2Service from '@/services/komga-oauth2.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaOauth2 = new KomgaOauth2Service(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaOauth2: KomgaOauth2Service;
  }
}
