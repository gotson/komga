import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const ScanIntervalValues = [
  'DISABLED',
  'HOURLY',
  'EVERY_6H',
  'EVERY_12H',
  'DAILY',
  'WEEKLY',
] as const

export type ScanInterval = (typeof ScanIntervalValues)[number]

export const scanIntervalMessages: Record<ScanInterval, MessageDescriptor> = defineMessages({
  DISABLED: {
    description: 'Scan interval: DISABLED',
    defaultMessage: 'Disabled',
    id: 'enum.ScanInterval.DISABLED',
  },
  HOURLY: {
    description: 'Scan interval: HOURLY',
    defaultMessage: 'Hourly',
    id: 'enum.ScanInterval.HOURLY',
  },
  EVERY_6H: {
    description: 'Scan interval: EVERY_6H',
    defaultMessage: 'Every 6 hours',
    id: 'enum.ScanInterval.EVERY_6H',
  },
  EVERY_12H: {
    description: 'Scan interval: EVERY_12H',
    defaultMessage: 'Every 12 hours',
    id: 'enum.ScanInterval.EVERY_12H',
  },
  DAILY: {
    description: 'Scan interval: DAILY',
    defaultMessage: 'Daily',
    id: 'enum.ScanInterval.DAILY',
  },
  WEEKLY: {
    description: 'Scan interval: WEEKLY',
    defaultMessage: 'Weekly',
    id: 'enum.ScanInterval.WEEKLY',
  },
})
