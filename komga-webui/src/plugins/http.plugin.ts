import urls from '@/functions/urls'
import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'
import _Vue from 'vue'

export default {
  install (Vue: typeof _Vue) {
    Vue.prototype.$http = axios.create({
      baseURL: urls.origin,
      withCredentials: true,
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
    } as AxiosRequestConfig)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $http: AxiosInstance;
  }
}
