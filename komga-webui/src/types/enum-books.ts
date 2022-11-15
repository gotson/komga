import {without} from 'lodash'

export enum ReadingDirection {
  LEFT_TO_RIGHT = 'LEFT_TO_RIGHT',
  RIGHT_TO_LEFT = 'RIGHT_TO_LEFT',
  VERTICAL = 'VERTICAL',
  WEBTOON = 'WEBTOON',
  PDF = 'PDF'
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
  READ = 'READ',
  UNREAD_AND_IN_PROGRESS = 'UNREAD_AND_IN_PROGRESS'
}

export function replaceCompositeReadStatus(list: string[]): string[] {
  if(list?.includes(ReadStatus.UNREAD_AND_IN_PROGRESS)){
    return [...without(list, ReadStatus.UNREAD_AND_IN_PROGRESS), ReadStatus.UNREAD, ReadStatus.IN_PROGRESS]
  }
  else return list
}

export enum CopyMode {
  MOVE = 'MOVE',
  COPY = 'COPY',
  HARDLINK = 'HARDLINK',
}
