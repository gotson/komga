export enum CLIENT_SETTING_USER {
  NEXTUI_LIBRARIES = 'komga.nextui.libraries',
}

export type ClientSettingUserLibrary = {
  unpinned?: boolean
  order?: number
}
