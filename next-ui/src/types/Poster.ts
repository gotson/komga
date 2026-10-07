import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const Poster = {
  AnyType: 'ANYTYPE',
  Generated: 'GENERATED',
  Sidecar: 'SIDECAR',
  UserUploaded: 'USER_UPLOADED',
} as const

export type Poster = (typeof Poster)[keyof typeof Poster]

export const PosterValues = Object.values(Poster)

export const posterMessages: Record<Poster, MessageDescriptor> = defineMessages({
  ANYTYPE: {
    description: 'Poster type - any type',
    defaultMessage: 'Any type',
    id: 'jm9oYh',
  },
  GENERATED: {
    description: 'Poster card: type - generated artwork',
    defaultMessage: 'Generated artwork',
    id: 'vtBARB',
  },
  SIDECAR: {
    description: 'Poster card: type - local artwork',
    defaultMessage: 'Local artwork',
    id: 'USJuJ1',
  },
  USER_UPLOADED: {
    description: 'Poster card: type - user uploaded',
    defaultMessage: 'User uploaded',
    id: '9dNd68',
  },
})
