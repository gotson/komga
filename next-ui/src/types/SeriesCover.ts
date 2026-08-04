import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const SeriesCoverValues = [
  'FIRST',
  'FIRST_UNREAD_OR_FIRST',
  'FIRST_UNREAD_OR_LAST',
  'LAST',
] as const

export type SeriesCover = (typeof SeriesCoverValues)[number]

export const seriesCoverMessages: Record<SeriesCover, MessageDescriptor> = defineMessages({
  FIRST: {
    description: 'Series cover: FIRST',
    defaultMessage: 'First',
    id: 'enum.SeriesCover.FIRST',
  },
  FIRST_UNREAD_OR_FIRST: {
    description: 'Series cover: FIRST_UNREAD_OR_FIRST',
    defaultMessage: 'First unread, else first',
    id: 'enum.SeriesCover.FIRST_UNREAD_OR_FIRST',
  },
  FIRST_UNREAD_OR_LAST: {
    description: 'Series cover: FIRST_UNREAD_OR_LAST',
    defaultMessage: 'First unread, else last',
    id: 'enum.SeriesCover.FIRST_UNREAD_OR_LAST',
  },
  LAST: {
    description: 'Series cover: LAST',
    defaultMessage: 'Last',
    id: 'enum.SeriesCover.LAST',
  },
})
