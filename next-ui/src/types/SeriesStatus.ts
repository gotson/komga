import { defineMessages } from 'vue-intl'

export enum SeriesStatus {
  ENDED = 'ENDED',
  ONGOING = 'ONGOING',
  HIATUS = 'HIATUS',
  ABANDONED = 'ABANDONED',
}

export const seriesStatusMessages = defineMessages({
  [SeriesStatus.ENDED]: {
    description: 'Series status: ENDED',
    defaultMessage: 'Ended',
    id: 'waBpAI',
  },
  [SeriesStatus.ONGOING]: {
    description: 'Series status: ONGOING',
    defaultMessage: 'Ongoing',
    id: 'k0iQcZ',
  },
  [SeriesStatus.HIATUS]: {
    description: 'Series status: HIATUS',
    defaultMessage: 'Hiatus',
    id: '+hyKAd',
  },
  [SeriesStatus.ABANDONED]: {
    description: 'Series status: ABANDONED',
    defaultMessage: 'Abandoned',
    id: 'NQctWq',
  },
})
