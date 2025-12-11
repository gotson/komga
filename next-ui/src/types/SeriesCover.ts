import { defineMessages } from 'vue-intl'

export enum SeriesCover {
  FIRST = 'FIRST',
  FIRST_UNREAD_OR_FIRST = 'FIRST_UNREAD_OR_FIRST',
  FIRST_UNREAD_OR_LAST = 'FIRST_UNREAD_OR_LAST',
  LAST = 'LAST',
}

export const seriesCoverMessages = defineMessages({
  [SeriesCover.FIRST]: {
    description: 'Series cover: FIRST',
    defaultMessage: 'First',
    id: 'j7cvLm',
  },
  [SeriesCover.FIRST_UNREAD_OR_FIRST]: {
    description: 'Series cover: FIRST_UNREAD_OR_FIRST',
    defaultMessage: 'First unread, else first',
    id: 'woVEgl',
  },
  [SeriesCover.FIRST_UNREAD_OR_LAST]: {
    description: 'Series cover: FIRST_UNREAD_OR_LAST',
    defaultMessage: 'First unread, else last',
    id: 'kLu/vI',
  },
  [SeriesCover.LAST]: {
    description: 'Series cover: LAST',
    defaultMessage: 'Last',
    id: 'pkqPAO',
  },
})
