import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaLoginService from '@/services/komga-login.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaLogin = new KomgaLoginService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaLogin: KomgaLoginService;
  }
}
