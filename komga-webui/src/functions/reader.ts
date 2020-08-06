import { ScaleType } from '@/types/enum-reader'
import { ReadingDirection } from '@/types/enum-books'

export const ScaleTypeText = {
  [ScaleType.SCREEN]: 'Fit screen',
  [ScaleType.HEIGHT]: 'Fit height',
  [ScaleType.WIDTH]: 'Fit width',
  [ScaleType.ORIGINAL]: 'Original',
}

export const ReadingDirectionText = {
  [ReadingDirection.LEFT_TO_RIGHT]: 'Left to right',
  [ReadingDirection.RIGHT_TO_LEFT]: 'Right to left',
  [ReadingDirection.VERTICAL]: 'Vertical',
  [ReadingDirection.WEBTOON]: 'Webtoon',
}
