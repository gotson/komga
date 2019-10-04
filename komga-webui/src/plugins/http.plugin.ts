import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'
import _Vue from 'vue'

export default {
  install (Vue: typeof _Vue) {
    const config = {
      baseURL: process.env.VUE_APP_KOMGA_API_URL ? process.env.VUE_APP_KOMGA_API_URL : window.location.origin
    } as AxiosRequestConfig

    if (process.env.VUE_APP_LOGIN && process.env.VUE_APP_PASSWORD) {
      config.auth = {
        username: process.env.VUE_APP_LOGIN,
        password: process.env.VUE_APP_PASSWORD
      }
    }

    Vue.prototype.$http = axios.create(config)
  }
}

declare module 'vue/types/vue' {
  interface Vue {
    $http: AxiosInstance;
  }
}
