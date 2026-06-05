import { defineMessages } from 'vue-intl'

export enum MediaProfile {
  DIVINA = 'DIVINA',
  EPUB = 'EPUB',
  PDF = 'PDF',
}

export function getMediaStatusFromString(value: string): MediaProfile | undefined {
  if (Object.values(MediaProfile).includes(value as MediaProfile)) return value as MediaProfile
  return undefined
}

export const mediaProfileMessages = defineMessages({
  [MediaProfile.DIVINA]: {
    description: 'Media profile: divina',
    defaultMessage: 'DiViNa',
    id: 'T8f81v',
  },
  [MediaProfile.EPUB]: {
    description: 'Media profile: epub',
    defaultMessage: 'Epub',
    id: 'bfoSR4',
  },
  [MediaProfile.PDF]: {
    description: 'Media profile: pdf',
    defaultMessage: 'Pdf',
    id: 'EN0jJT',
  },
})
