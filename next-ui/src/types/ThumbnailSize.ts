import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const ThumbnailSizeValues = ['DEFAULT', 'MEDIUM', 'LARGE', 'XLARGE'] as const

export type ThumbnailSize = (typeof ThumbnailSizeValues)[number]

export const thumbnailSizeMessages: Record<ThumbnailSize, MessageDescriptor> = defineMessages({
  DEFAULT: {
    description: 'Thumbnail size: default',
    defaultMessage: 'Default (300px)',
    id: 'enum.ThumbnailSize.DEFAULT',
  },
  MEDIUM: {
    description: 'Thumbnail size: medium',
    defaultMessage: 'Medium (600px)',
    id: 'enum.ThumbnailSize.MEDIUM',
  },
  LARGE: {
    description: 'Thumbnail size: large',
    defaultMessage: 'Large (900px)',
    id: 'enum.ThumbnailSize.LARGE',
  },
  XLARGE: {
    description: 'Thumbnail size: x-large',
    defaultMessage: 'X-Large (1200px)',
    id: 'enum.ThumbnailSize.XLARGE',
  },
})
