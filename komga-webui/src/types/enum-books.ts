export enum ReadingDirection {
  LEFT_TO_RIGHT = 'LEFT_TO_RIGHT',
  RIGHT_TO_LEFT = 'RIGHT_TO_LEFT',
  VERTICAL = 'VERTICAL',
  WEBTOON = 'WEBTOON'
}

export enum MediaStatus {
  READY = 'READY',
  UNKNOWN = 'UNKNOWN',
  ERROR = 'ERROR',
  UNSUPPORTED = 'UNSUPPORTED',
  OUTDATED = 'OUTDATED'
}

export enum ReadStatus {
  UNREAD = 'UNREAD',
  IN_PROGRESS = 'IN_PROGRESS',
  READ = 'READ'
}

export enum CopyMode {
  MOVE = 'MOVE',
  COPY = 'COPY',
  HARDLINK = 'HARDLINK',
}
