import _Vue from 'vue'
import {format} from 'date-fns'

export default {
  install(Vue: typeof _Vue) {
    let isDebug = false
    if (process.env.VUE_APP_LOG_LEVEL || window.localStorage['loglevel'])
      isDebug = true

    Object.defineProperty(Vue.prototype, '$debug', {
      get: function () {
        // eslint-disable-next-line no-console
        return isDebug ? console.log.bind(console, timestamp(), 'DEBUG ---') : noop
      },
    })

    Object.defineProperty(Vue.prototype, '$info', {
      get: function () {
        // eslint-disable-next-line no-console
        return isDebug ? console.log.bind(console, timestamp(), 'INFO ---') : noop
      },
    })

    Object.defineProperty(Vue.prototype, '$warn', {
      get: function () {
        // eslint-disable-next-line no-console
        return isDebug ? console.warn.bind(console, timestamp(), 'WARN ---') : noop
      },
    })

    Object.defineProperty(Vue.prototype, '$err', {
      get: function () {
        // eslint-disable-next-line no-console
        return isDebug ? console.error.bind(console, timestamp(), 'ERROR ---') : noop
      },
    })
  },
}

function timestamp() {
  return format(new Date(), 'yyyy-MM-dd HH:mm:ss.SSS')
}

function noop() {
}

declare module 'vue/types/vue' {
  interface Vue {
    $debug(...msg: any[]): void;
    $info(...msg: any[]): void;
    $warn(...msg: any[]): void;
    $err(...msg: any[]): void;
  }
}
