import KomgaFilesystemService from '@/services/komga-filesystem.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$komgaFileSystem = new KomgaFilesystemService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaFileSystem: KomgaFilesystemService;
  }
}
