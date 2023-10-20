export interface SettingsDto {
  deleteEmptyCollections: boolean,
  deleteEmptyReadLists: boolean,
  rememberMeDurationDays: number,
  thumbnailSize: ThumbnailSizeDto,
  taskPoolSize: number,
}

export interface SettingsUpdateDto {
  deleteEmptyCollections?: boolean,
  deleteEmptyReadLists?: boolean,
  rememberMeDurationDays?: number,
  renewRememberMeKey?: boolean,
  thumbnailSize?: ThumbnailSizeDto,
  taskPoolSize?: number,
}

export enum ThumbnailSizeDto {
  DEFAULT = 'DEFAULT',
  MEDIUM = 'MEDIUM',
  LARGE = 'LARGE',
  XLARGE = 'XLARGE',
}
