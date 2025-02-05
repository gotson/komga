export interface ClientSettingDto {
  key: string,
  value: string,
  allowUnauthorized?: boolean,
  userId?: string,
}

export interface ClientSettingGlobalUpdateDto {
  key: string,
  value: string,
  allowUnauthorized: boolean,
}

export interface ClientSettingUserUpdateDto {
  key: string,
  value: string,
}

export enum CLIENT_SETTING {
  WEBUI_OAUTH2_HIDE_LOGIN = 'webui.oauth2.hide_login',
  WEBUI_OAUTH2_AUTO_LOGIN = 'webui.oauth2.auto_login',
  WEBUI_POSTER_STRETCH = 'webui.poster.stretch'
}
