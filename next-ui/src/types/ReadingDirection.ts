import { defineMessages } from 'vue-intl'

export enum ReadingDirection {
  LEFT_TO_RIGHT = 'LEFT_TO_RIGHT',
  RIGHT_TO_LEFT = 'RIGHT_TO_LEFT',
  VERTICAL = 'VERTICAL',
  WEBTOON = 'WEBTOON',
}

export const readingDirectionMessages = defineMessages({
  [ReadingDirection.LEFT_TO_RIGHT]: {
    description: 'Series status: LEFT_TO_RIGHT',
    defaultMessage: 'Left to right',
    id: '4+VDM1',
  },
  [ReadingDirection.RIGHT_TO_LEFT]: {
    description: 'Series status: RIGHT_TO_LEFT',
    defaultMessage: 'Right to left',
    id: 'QRKM3u',
  },
  [ReadingDirection.VERTICAL]: {
    description: 'Series status: VERTICAL',
    defaultMessage: 'Vertical',
    id: '6eg4Ch',
  },
  [ReadingDirection.WEBTOON]: {
    description: 'Series status: WEBTOON',
    defaultMessage: 'Webtoon',
    id: 'xiua0/',
  },
})
