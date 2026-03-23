import { defineMessages } from 'vue-intl'

export enum ReadStatus {
  UNREAD = 'UNREAD',
  IN_PROGRESS = 'IN_PROGRESS',
  READ = 'READ',
}

export const readStatusMessages = defineMessages({
  [ReadStatus.UNREAD]: {
    description: 'Read status: UNREAD',
    defaultMessage: 'Unread',
    id: 'XUgQvn',
  },
  [ReadStatus.IN_PROGRESS]: {
    description: 'Read status: IN_PROGRESS',
    defaultMessage: 'In progress',
    id: '8DRgrr',
  },
  [ReadStatus.READ]: {
    description: 'Read status: READ',
    defaultMessage: 'Read',
    id: 'HhmZaG',
  },
})
