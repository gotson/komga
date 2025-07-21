import { defineMessages } from 'vue-intl'

export enum ThumbnailSize {
  DEFAULT = 'DEFAULT',
  MEDIUM = 'MEDIUM',
  LARGE = 'LARGE',
  XLARGE = 'XLARGE',
}

export const thumbnailSizeMessages = defineMessages({
  [ThumbnailSize.DEFAULT]: {
    description: 'Thumbnail size: default',
    defaultMessage: 'Default (300px)',
    id: '0DGOZl',
  },
  [ThumbnailSize.MEDIUM]: {
    description: 'Thumbnail size: medium',
    defaultMessage: 'Medium (600px)',
    id: 't1LnqS',
  },
  [ThumbnailSize.LARGE]: {
    description: 'Thumbnail size: large',
    defaultMessage: 'Large (900px)',
    id: 'hxUnz6',
  },
  [ThumbnailSize.XLARGE]: {
    description: 'Thumbnail size: x-large',
    defaultMessage: 'X-Large (1200px)',
    id: 'C7iLlR',
  },
})
