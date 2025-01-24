import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaFontsService from '@/services/komga-fonts.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaFonts = new KomgaFontsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaFonts: KomgaFontsService;
  }
}
