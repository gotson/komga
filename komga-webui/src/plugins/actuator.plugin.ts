import ActuatorService from '@/services/actuator.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'

export default {
  install (
    Vue: typeof _Vue,
    { http }: { http: AxiosInstance }) {
    Vue.prototype.$actuator = new ActuatorService(http)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $actuator: ActuatorService;
  }
}
