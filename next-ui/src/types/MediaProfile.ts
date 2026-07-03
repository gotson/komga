import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const MediaProfileValues = ['DIVINA', 'EPUB', 'PDF'] as const

export type MediaProfile = (typeof MediaProfileValues)[number]

export const mediaProfileMessages: Record<MediaProfile, MessageDescriptor> = defineMessages({
  DIVINA: {
    description: 'Media profile: divina',
    defaultMessage: 'DiViNa',
    id: 'enum.MediaProfile.DIVINA',
  },
  EPUB: {
    description: 'Media profile: epub',
    defaultMessage: 'Epub',
    id: 'enum.MediaProfile.EPUB',
  },
  PDF: {
    description: 'Media profile: pdf',
    defaultMessage: 'Pdf',
    id: 'enum.MediaProfile.PDF',
  },
})
