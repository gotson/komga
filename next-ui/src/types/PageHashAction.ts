import { defineMessages } from 'vue-intl'

export type PageHashAction = 'DELETE_AUTO' | 'DELETE_MANUAL' | 'IGNORE'

export enum PageHashActionEnum {
  DELETE_AUTO = 'DELETE_AUTO',
  DELETE_MANUAL = 'DELETE_MANUAL',
  IGNORE = 'IGNORE',
}

export const pageHashActionMessages = defineMessages({
  [PageHashActionEnum.DELETE_AUTO]: {
    description: 'Page Hash Action: DELETE_AUTO',
    defaultMessage: 'Auto delete',
    id: 'enum.pageHashAction.DELETE_AUTO',
  },
  [PageHashActionEnum.DELETE_MANUAL]: {
    description: 'Page Hash Action: DELETE_MANUAL',
    defaultMessage: 'Manual delete',
    id: 'enum.pageHashAction.DELETE_MANUAL',
  },
  [PageHashActionEnum.IGNORE]: {
    description: 'Page Hash Action: IGNORE',
    defaultMessage: 'Ignore',
    id: 'enum.pageHashAction.IGNORE',
  },
})
