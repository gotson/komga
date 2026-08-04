import { configure, getConsoleSink, getLogger } from '@logtape/logtape'
import { getPrettyFormatter } from '@logtape/pretty'
import * as v from 'valibot'

const LevelSchema = v.fallback(
  v.picklist(['trace', 'debug', 'info', 'warning', 'error', 'fatal']),
  'info',
)

const level = v.parse(LevelSchema, import.meta.env.VITE_LOG_LEVEL)

export async function initLogger() {
  await configure({
    sinks: {
      console: getConsoleSink(),
      consolePP: getConsoleSink({
        formatter: getPrettyFormatter({
          properties: true,
          colors: false,
          icons: false,
          timestamp: 'date-time-timezone',
          wordWrap: false,
          align: false,
        }),
      }),
    },
    loggers: [
      { category: ['logtape', 'meta'], lowestLevel: 'warning', sinks: ['console'] },
      { category: ['app'], lowestLevel: level, sinks: ['consolePP'] },
    ],
  })
  logger.debug(`Logging at level: ${level}`)
}

export const logger = getLogger(['app'])
