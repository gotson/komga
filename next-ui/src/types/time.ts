export const SECOND = 1000
export const MINUTE = 60 * SECOND
export const HOUR = 60 * MINUTE

export const STALE_TIME = {
  INSTANT: 0,
  DYNAMIC: 30 * SECOND,
  DEFAULT: 5 * MINUTE,
  LONG: HOUR,
  STATIC: Infinity,
} as const
