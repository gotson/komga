import { defineMessages } from 'vue-intl'

export enum ScanInterval {
  DISABLED = 'DISABLED',
  HOURLY = 'HOURLY',
  EVERY_6H = 'EVERY_6H',
  EVERY_12H = 'EVERY_12H',
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
}

export const scanIntervalMessages = defineMessages({
  [ScanInterval.DISABLED]: {
    description: 'Scan interval: DISABLED',
    defaultMessage: 'Disabled',
    id: '8M9T3g',
  },
  [ScanInterval.HOURLY]: {
    description: 'Scan interval: HOURLY',
    defaultMessage: 'Hourly',
    id: 'rBFh/c',
  },
  [ScanInterval.EVERY_6H]: {
    description: 'Scan interval: EVERY_6H',
    defaultMessage: 'Every 6 hours',
    id: '4d2F5w',
  },
  [ScanInterval.EVERY_12H]: {
    description: 'Scan interval: EVERY_12H',
    defaultMessage: 'Every 12 hours',
    id: '5yu0g9',
  },
  [ScanInterval.DAILY]: {
    description: 'Scan interval: DAILY',
    defaultMessage: 'Daily',
    id: 'qLk+cl',
  },
  [ScanInterval.WEEKLY]: {
    description: 'Scan interval: WEEKLY',
    defaultMessage: 'Weekly',
    id: 'T6pXCK',
  },
})
