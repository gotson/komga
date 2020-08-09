import { ScaleType, PaddingPercentage } from '@/types/enum-reader'
import { ReadingDirection } from '@/types/enum-books'

export const ScaleTypeText = {
  [ScaleType.SCREEN]: 'Fit screen',
  [ScaleType.HEIGHT]: 'Fit height',
  [ScaleType.WIDTH]: 'Fit width',
  [ScaleType.ORIGINAL]: 'Original',
}

export const PaddingPercentageText = {
  [PaddingPercentage.NONE]: 'None',
  [PaddingPercentage.TEN]: '10%',
  [PaddingPercentage.FIFTEEN]: '15%',
  [PaddingPercentage.TWENTY]: '20%',
  [PaddingPercentage.TWENTYFIVE]: '25%',
  [PaddingPercentage.THIRTY]: '30%',
  [PaddingPercentage.THIRTYFIVE]: '35%',
  [PaddingPercentage.FOURTY]: '40%',
  [PaddingPercentage.FOURTYFIVE]: '45%',
  [PaddingPercentage.FIFTY]: '50%',
}

export const ReadingDirectionText = {
  [ReadingDirection.LEFT_TO_RIGHT]: 'Left to right',
  [ReadingDirection.RIGHT_TO_LEFT]: 'Right to left',
  [ReadingDirection.VERTICAL]: 'Vertical',
  [ReadingDirection.WEBTOON]: 'Webtoon',
}
