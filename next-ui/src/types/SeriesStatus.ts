import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const SeriesStatus = {
  Ended: 'ENDED',
  Ongoing: 'ONGOING',
  Abandoned: 'ABANDONED',
  Hiatus: 'HIATUS',
} as const

export type SeriesStatus = (typeof SeriesStatus)[keyof typeof SeriesStatus]

export const SeriesStatusValues = Object.values(SeriesStatus)

export const seriesStatusMessages: Record<SeriesStatus, MessageDescriptor> = defineMessages({
  ENDED: {
    description: 'Series status: ENDED',
    defaultMessage: 'Ended',
    id: 'enum.SeriesStatus.ENDED',
  },
  ONGOING: {
    description: 'Series status: ONGOING',
    defaultMessage: 'Ongoing',
    id: 'enum.SeriesStatus.ONGOING',
  },
  HIATUS: {
    description: 'Series status: HIATUS',
    defaultMessage: 'Hiatus',
    id: 'enum.SeriesStatus.HIATUS',
  },
  ABANDONED: {
    description: 'Series status: ABANDONED',
    defaultMessage: 'Abandoned',
    id: 'enum.SeriesStatus.ABANDONED',
  },
})
