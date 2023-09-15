import {AxiosInstance} from 'axios'
import _Vue from 'vue'
import KomgaAnnouncementsService from '@/services/komga-announcements.service'

export default {
  install(
    Vue: typeof _Vue,
    {http}: { http: AxiosInstance }) {
    Vue.prototype.$komgaAnnouncements = new KomgaAnnouncementsService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaAnnouncements: KomgaAnnouncementsService;
  }
}
