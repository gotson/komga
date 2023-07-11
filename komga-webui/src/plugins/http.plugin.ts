import urls from '@/functions/urls'
import axios, {AxiosInstance, AxiosRequestConfig} from 'axios'
import _Vue from 'vue'
import {parseISO} from 'date-fns'

export default {
  install(Vue: typeof _Vue) {
    const client = axios.create({
      baseURL: urls.origin,
      withCredentials: true,
      headers: {'X-Requested-With': 'XMLHttpRequest'},
    } as AxiosRequestConfig)
    client.interceptors.response.use(originalResponse => {
      handleDates(originalResponse.data)
      return originalResponse
    })
    Vue.prototype.$http = client
  },
}

const isoDateFormat = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d*)?(?:[-+]\d{2}:?\d{2}|Z)?$/

function isIsoDateString(value: any): boolean {
  return value && typeof value === 'string' && isoDateFormat.test(value)
}

export function handleDates(body: any) {
  if (body === null || body === undefined || typeof body !== 'object')
    return body

  for (const key of Object.keys(body)) {
    const value = body[key]
    if (isIsoDateString(value)) body[key] = parseISO(value)
    else if (typeof value === 'object') handleDates(value)
  }
}

declare module 'vue/types/vue' {
  interface Vue {
    $http: AxiosInstance;
  }
}
