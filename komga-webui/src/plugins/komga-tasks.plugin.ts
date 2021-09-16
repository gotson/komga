import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaTasksService from '@/services/komga-tasks.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaTasks = new KomgaTasksService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaTasks: KomgaTasksService;
  }
}
