export enum LoadState {
  Loaded,
  NotLoaded,
  Loading
}

export enum ImageFit {
  Width = 'width',
  Height = 'height',
  Original = 'original'
}

export enum MediaStatus {
  Ready = 'READY',
  Unknown = 'UNKNOWN',
  Error = 'ERROR',
  Unsupported = 'UNSUPPORTED'
}

export enum SeriesStatus {
  ENDED = 'ENDED',
  ONGOING = 'ONGOING',
  ABANDONED = 'ABANDONED',
  HIATUS = 'HIATUS'
}
