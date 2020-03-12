export enum LoadState {
  Loaded,
  NotLoaded,
  Loading
}

export enum ImageFit {
  WIDTH = 'width',
  HEIGHT = 'height',
  ORIGINAL = 'original'
}

export enum ReaderReadingDirection {
  LeftToRight = 'ltr',
  RightToLeft = 'rtl'
}

export enum MediaStatus {
  READY = 'READY',
  UNKNOWN = 'UNKNOWN',
  ERROR = 'ERROR',
  UNSUPPORTED = 'UNSUPPORTED'
}

export enum SeriesStatus {
  ENDED = 'ENDED',
  ONGOING = 'ONGOING',
  ABANDONED = 'ABANDONED',
  HIATUS = 'HIATUS'
}
