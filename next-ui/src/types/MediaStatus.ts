import { defineMessages } from 'vue-intl'

export enum MediaStatus {
  UNKNOWN = 'UNKNOWN',
  ERROR = 'ERROR',
  READY = 'READY',
  UNSUPPORTED = 'UNSUPPORTED',
  OUTDATED = 'OUTDATED',
}

export const mediaStatusMessages = defineMessages({
  [MediaStatus.UNKNOWN]: {
    description: 'Media status: unknown',
    defaultMessage: 'Unknown',
    id: 'vBi53Y',
  },
  [MediaStatus.ERROR]: {
    description: 'Media status: error',
    defaultMessage: 'Error',
    id: 'G49aNP',
  },
  [MediaStatus.READY]: {
    description: 'Media status: ready',
    defaultMessage: 'Ready',
    id: 'k0XIsB',
  },
  [MediaStatus.UNSUPPORTED]: {
    description: 'Media status: unsupported',
    defaultMessage: 'Unsupported',
    id: '7iAvhC',
  },
  [MediaStatus.OUTDATED]: {
    description: 'Media status: outdated',
    defaultMessage: 'Outdated',
    id: 'xba3Ob',
  },
})
