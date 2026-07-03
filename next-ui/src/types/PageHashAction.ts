import { defineMessages, type MessageDescriptor } from 'vue-intl'

export type PageHashAction = 'DELETE_AUTO' | 'DELETE_MANUAL' | 'IGNORE'

export const pageHashActionMessages: Record<PageHashAction, MessageDescriptor> = defineMessages({
  DELETE_AUTO: {
    description: 'Page Hash Action: DELETE_AUTO',
    defaultMessage: 'Auto delete',
    id: 'enum.pageHashAction.DELETE_AUTO',
  },
  DELETE_MANUAL: {
    description: 'Page Hash Action: DELETE_MANUAL',
    defaultMessage: 'Manual delete',
    id: 'enum.pageHashAction.DELETE_MANUAL',
  },
  IGNORE: {
    description: 'Page Hash Action: IGNORE',
    defaultMessage: 'Ignore',
    id: 'enum.pageHashAction.IGNORE',
  },
})
