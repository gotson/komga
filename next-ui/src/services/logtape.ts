import { configure, getConsoleSink, getLogger } from '@logtape/logtape'

export async function initLogger() {
  await configure({
    sinks: { console: getConsoleSink() },
    loggers: [{ category: ['app'], lowestLevel: 'debug', sinks: ['console'] }],
  })
}

export const logger = getLogger(['app'])
