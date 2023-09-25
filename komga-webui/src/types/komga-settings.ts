export interface SettingsDto {
  deleteEmptyCollections: boolean,
  deleteEmptyReadLists: boolean,
  rememberMeDurationDays: number,
}

export interface SettingsUpdateDto {
  deleteEmptyCollections?: boolean,
  deleteEmptyReadLists?: boolean,
  rememberMeDurationDays?: number,
  renewRememberMeKey?: boolean,
}
