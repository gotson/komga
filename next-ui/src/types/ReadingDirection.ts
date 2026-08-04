import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const ReadingDirectionValues = [
  'LEFT_TO_RIGHT',
  'RIGHT_TO_LEFT',
  'VERTICAL',
  'WEBTOON',
] as const

export type ReadingDirection = (typeof ReadingDirectionValues)[number]

export const readingDirectionMessages: Record<ReadingDirection, MessageDescriptor> = defineMessages(
  {
    LEFT_TO_RIGHT: {
      description: 'Series status: LEFT_TO_RIGHT',
      defaultMessage: 'Left to right',
      id: 'enum.ReadingDirection.LEFT_TO_RIGHT',
    },
    RIGHT_TO_LEFT: {
      description: 'Series status: RIGHT_TO_LEFT',
      defaultMessage: 'Right to left',
      id: 'enum.ReadingDirection.RIGHT_TO_LEFT',
    },
    VERTICAL: {
      description: 'Series status: VERTICAL',
      defaultMessage: 'Vertical',
      id: 'enum.ReadingDirection.VERTICAL',
    },
    WEBTOON: {
      description: 'Series status: WEBTOON',
      defaultMessage: 'Webtoon',
      id: 'enum.ReadingDirection.WEBTOON',
    },
  },
)
