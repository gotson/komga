import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'
import _Vue from 'vue'

export default {
  install (Vue: typeof _Vue) {
    Vue.prototype.$http = axios.create({
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin,
      withCredentials: true
    } as AxiosRequestConfig)
  }
}

declare module 'vue/types/vue' {
  interface Vue {
    $http: AxiosInstance;
  }
}
