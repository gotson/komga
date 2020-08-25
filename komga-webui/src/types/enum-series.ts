import { capitalize } from 'lodash'

export enum SeriesStatus {
  ENDED = 'ENDED',
  ONGOING = 'ONGOING',
  ABANDONED = 'ABANDONED',
  HIATUS = 'HIATUS'
}

export const SeriesStatusKeyValue = Object.values(SeriesStatus).map(x => ({
  name: capitalize(x),
  value: x,
} as NameValue))
