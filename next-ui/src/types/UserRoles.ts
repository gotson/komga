import { defineMessages } from 'vue-intl'

export enum UserRoles {
  ADMIN = 'ADMIN',
  FILE_DOWNLOAD = 'FILE_DOWNLOAD',
  PAGE_STREAMING = 'PAGE_STREAMING',
  KOBO_SYNC = 'KOBO_SYNC',
  KOREADER_SYNC = 'KOREADER_SYNC',
  USER = 'USER',
}

export const userRolesMessages = defineMessages({
  [UserRoles.ADMIN]: {
    description: 'User role: admin',
    defaultMessage: 'Administrator',
    id: '8N1oqm',
  },
  [UserRoles.FILE_DOWNLOAD]: {
    description: 'User role: file_download',
    defaultMessage: 'File download',
    id: 'c00+tT',
  },
  [UserRoles.PAGE_STREAMING]: {
    description: 'User role: page_streaming',
    defaultMessage: 'Page streaming',
    id: 'aa6dBv',
  },
  [UserRoles.KOBO_SYNC]: {
    description: 'User role: kobo_sync',
    defaultMessage: 'Kobo sync',
    id: 'Jm5RAA',
  },
  [UserRoles.KOREADER_SYNC]: {
    description: 'User role: koreader_sync',
    defaultMessage: 'KOReader sync',
    id: 'BwX4xr',
  },
  [UserRoles.USER]: {
    description: 'User role: user',
    defaultMessage: 'User',
    id: '2+oQ1g',
  },
})
