import { SelectItem } from '@/functions/forms'
// TODO eslint-disable needed for declaration merging, need to decide if want to globally disable?

// eslint-disable-next-line import/export
export enum ReadingDirection {
  LEFT_TO_RIGHT = 'LEFT_TO_RIGHT',
  RIGHT_TO_LEFT = 'RIGHT_TO_LEFT',
  VERTICAL = 'VERTICAL',
  WEBTOON = 'WEBTOON'
}
export const READING_DIRECTIONS = Object.values(ReadingDirection)

export const READING_DIR_ITEMS = [
  { text: 'Left to right', value: ReadingDirection.LEFT_TO_RIGHT },
  { text: 'Right to left', value: ReadingDirection.RIGHT_TO_LEFT },
  { text: 'Vertical', value: ReadingDirection.VERTICAL },
] as SelectItem<ReadingDirection>[]

// eslint-disable-next-line no-redeclare,import/export
export namespace ReadingDirection {
  // eslint-disable-next-line no-inner-declarations
  export function toString (o: ReadingDirection): string {
    let i = READING_DIRECTIONS.indexOf(o)
    if (i >= 0 && i < READING_DIR_ITEMS.length) {
      return READING_DIR_ITEMS[i].text
    }
    return 'Unknown'
  }
}

export enum MediaStatus {
  READY = 'READY',
  UNKNOWN = 'UNKNOWN',
  ERROR = 'ERROR',
  UNSUPPORTED = 'UNSUPPORTED',
  OUTDATED = 'OUTDATED'
}
export const MEDIA_STATUES = Object.values(MediaStatus)

export enum ReadStatus {
  UNREAD = 'UNREAD',
  IN_PROGRESS = 'IN_PROGRESS',
  READ = 'READ'
}
export const READ_STATUSES = Object.values(ReadStatus)

// eslint-disable-next-line import/export
export enum ImageFit {
  WIDTH = 'width',
  HEIGHT = 'height',
  ORIGINAL = 'original'
}
export const IMAGE_FITS = Object.values(ImageFit)

export const IMAGE_FIT_ITEMS = [
  { text: 'Fit to width', value: ImageFit.WIDTH },
  { text: 'Fit to height', value: ImageFit.HEIGHT },
  { text: 'Original', value: ImageFit.ORIGINAL },
] as SelectItem<ImageFit>[]

// eslint-disable-next-line no-redeclare,import/export
export namespace ImageFit {
  // eslint-disable-next-line no-inner-declarations
  export function toString (o: ImageFit): string {
    let i = IMAGE_FITS.indexOf(o)
    return IMAGE_FIT_ITEMS[i].text
  }
  // eslint-disable-next-line no-inner-declarations
  export function next (o: ImageFit): ImageFit {
    let i = (IMAGE_FITS.indexOf(o) + 1) % (IMAGE_FITS.length)
    return IMAGE_FITS[i] as ImageFit
  }
}

// eslint-disable-next-line import/export
export enum BackgroundColors {
  WHITE = 'white',
  BLACK = 'black',
}
export const BACKGROUND_COLORS = Object.values(BackgroundColors)

export const BACKGROUND_COLOR_ITEMS = [
  { text: 'White', value: BackgroundColors.WHITE },
  { text: 'Black', value: BackgroundColors.BLACK },
] as SelectItem<BackgroundColors>[]
