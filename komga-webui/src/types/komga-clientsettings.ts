export interface ClientSettingDto {
  value: string,
  allowUnauthorized?: boolean,
}

export interface ClientSettingGlobalUpdateDto {
  value: string,
  allowUnauthorized: boolean,
}

export interface ClientSettingUserUpdateDto {
  value: string,
}

export enum CLIENT_SETTING {
  WEBUI_OAUTH2_HIDE_LOGIN = 'webui.oauth2.hide_login',
  WEBUI_OAUTH2_AUTO_LOGIN = 'webui.oauth2.auto_login',
  WEBUI_POSTER_STRETCH = 'webui.poster.stretch',
  WEBUI_POSTER_BLUR_UNREAD = 'webui.poster.blur_unread',
  WEBUI_LIBRARIES = 'webui.libraries',
}

export interface ClientSettingLibrary {
  unpinned?: boolean,
  order?: number,
}

export interface ClientSettingLibraryUpdate {
  libraryId: string,
  patch: ClientSettingLibrary,
}
