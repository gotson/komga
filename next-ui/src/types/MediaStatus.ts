import { defineMessages } from 'vue-intl'

export enum MediaStatus {
  READY = 'READY',
  UNKNOWN = 'UNKNOWN',
  ERROR = 'ERROR',
  UNSUPPORTED = 'UNSUPPORTED',
  OUTDATED = 'OUTDATED',
}

export function getMediaStatusFromString(value: string): MediaStatus | undefined {
  if (Object.values(MediaStatus).includes(value as MediaStatus)) return value as MediaStatus
  return undefined
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
