export const ClientSettingUser = {
  NextUILibraries: 'komga.nextui.libraries',
} as const

export type ClientSettingUser = (typeof ClientSettingUser)[keyof typeof ClientSettingUser]

export type ClientSettingUserLibrary = {
  unpinned?: boolean
  order?: number
}
