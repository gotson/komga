import i18n from '@/i18n'

export enum SeriesStatus {
  ENDED = 'ENDED',
  ONGOING = 'ONGOING',
  ABANDONED = 'ABANDONED',
  HIATUS = 'HIATUS'
}

export function SeriesStatusKeyValue(): NameValue[] {
  return Object.values(SeriesStatus).map(x => ({
    name: i18n.t(`enums.series_status.${x}`),
    value: x,
  } as NameValue))
}
