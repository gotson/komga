import KomgaClaimService from '@/services/komga-claim.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaClaim = new KomgaClaimService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaClaim: KomgaClaimService;
  }
}
