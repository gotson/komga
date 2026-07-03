import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const MediaStatus = {
  Ready: 'READY',
  Unknown: 'UNKNOWN',
  Error: 'ERROR',
  Unsupported: 'UNSUPPORTED',
  Outdated: 'OUTDATED',
} as const

export type MediaStatus = (typeof MediaStatus)[keyof typeof MediaStatus]

export const MediaStatusValues = Object.values(MediaStatus)

export const mediaStatusMessages: Record<MediaStatus, MessageDescriptor> = defineMessages({
  UNKNOWN: {
    description: 'Media status: unknown',
    defaultMessage: 'Unknown',
    id: 'enum.MediaStatus.UNKNOWN',
  },
  ERROR: {
    description: 'Media status: error',
    defaultMessage: 'Error',
    id: 'enum.MediaStatus.ERROR',
  },
  READY: {
    description: 'Media status: ready',
    defaultMessage: 'Ready',
    id: 'enum.MediaStatus.READY',
  },
  UNSUPPORTED: {
    description: 'Media status: unsupported',
    defaultMessage: 'Unsupported',
    id: 'enum.MediaStatus.UNSUPPORTED',
  },
  OUTDATED: {
    description: 'Media status: outdated',
    defaultMessage: 'Outdated',
    id: 'enum.MediaStatus.OUTDATED',
  },
})
