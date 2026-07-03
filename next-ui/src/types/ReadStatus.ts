import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const ReadStatusValues = ['UNREAD', 'IN_PROGRESS', 'READ'] as const

export type ReadStatus = (typeof ReadStatusValues)[number]

export const readStatusMessages: Record<ReadStatus, MessageDescriptor> = defineMessages({
  UNREAD: {
    description: 'Read status: UNREAD',
    defaultMessage: 'Unread',
    id: 'enum.ReadStatus.UNREAD',
  },
  IN_PROGRESS: {
    description: 'Read status: IN_PROGRESS',
    defaultMessage: 'In progress',
    id: 'enum.ReadStatus.IN_PROGRESS',
  },
  READ: {
    description: 'Read status: READ',
    defaultMessage: 'Read',
    id: 'enum.ReadStatus.READ',
  },
})
