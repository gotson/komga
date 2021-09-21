import _Vue from 'vue'
import log from 'loglevel'
import _, {isArray, isObject} from 'lodash'
import {format} from 'date-fns'

export default {
  install(Vue: typeof _Vue) {
    if (process.env.VUE_APPlog_LEVEL)
      log.setLevel(process.env.VUE_APPlog_LEVEL as any)

    Vue.prototype.$logTrace = function (...msg: any[]) {
      const loggerName = _.get(this, 'logger') || _.get(this, '$options.name')
      const logger = loggerName ? log.getLogger(loggerName) : log
      return logger.trace.bind(logger, getPrefix('TRACE', loggerName), ...(stringArgs(msg)))
    }

    Vue.prototype.$logDebug = function (...msg: any[]) {
      const loggerName = _.get(this, 'logger') || _.get(this, '$options.name')
      const logger = loggerName ? log.getLogger(loggerName) : log
      return logger.debug.bind(logger, getPrefix('DEBUG', loggerName), ...(stringArgs(msg)))
    }

    Vue.prototype.$logInfo = function (...msg: any[]) {
      const loggerName = _.get(this, 'logger') || _.get(this, '$options.name')
      const logger = loggerName ? log.getLogger(loggerName) : log
      return logger.info.bind(logger, getPrefix('INFO', loggerName), ...(stringArgs(msg)))
    }

    Vue.prototype.$logWarn = function (...msg: any[]) {
      const loggerName = _.get(this, 'logger') || _.get(this, '$options.name')
      const logger = loggerName ? log.getLogger(loggerName) : log
      return logger.warn.bind(logger, getPrefix('WARN', loggerName), ...(stringArgs(msg)))
    }

    Vue.prototype.$logError = function (...msg: any[]) {
      const loggerName = _.get(this, 'logger') || _.get(this, '$options.name')
      const logger = loggerName ? log.getLogger(loggerName) : log
      return logger.error.bind(logger, getPrefix('ERROR', loggerName), ...(stringArgs(msg)))
    }
  },
}

function stringArgs(msg: any[]): any[] {
  return msg.map(m => isObject(m) || isArray(m) ? JSON.stringify(m) : m)
}

function getPrefix(level: string, loggerName: string): string {
  const timestamp = format(new Date(), 'yyyy-MM-dd HH:mm:ss.SSS')
  return `${timestamp} ${level} --- ${loggerName} :`
}

declare module 'vue/types/vue' {
  interface Vue {
    $logTrace(...msg: any[]): (...msg: any[]) => void;
    $logDebug(...msg: any[]): (...msg: any[]) => void;
    $logInfo(...msg: any[]): (...msg: any[]) => void;
    $logWarn(...msg: any[]): (...msg: any[]) => void;
    $logError(...msg: any[]): (...msg: any[]) => void;
  }
}
