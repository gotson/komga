import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaAnnouncementsService from '@/services/komga-announcements.service'
import KomgaReleasesService from '@/services/komga-releases.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaReleases = new KomgaReleasesService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaReleases: KomgaReleasesService;
  }
}
