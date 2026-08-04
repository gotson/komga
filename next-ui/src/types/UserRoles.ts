import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const UserRole = {
  Admin: 'ADMIN',
  FileDownload: 'FILE_DOWNLOAD',
  PageStreaming: 'PAGE_STREAMING',
  KoboSync: 'KOBO_SYNC',
  KoreaderSync: 'KOREADER_SYNC',
  User: 'USER',
} as const

export type UserRole = (typeof UserRole)[keyof typeof UserRole]

export const UserRoleValues = Object.values(UserRole)

export const userRolesMessages: Record<UserRole, MessageDescriptor> = defineMessages({
  ADMIN: {
    description: 'User role: admin',
    defaultMessage: 'Administrator',
    id: 'enum.UserRole.ADMIN',
  },
  FILE_DOWNLOAD: {
    description: 'User role: file_download',
    defaultMessage: 'File download',
    id: 'enum.UserRole.FILE_DOWNLOAD',
  },
  PAGE_STREAMING: {
    description: 'User role: page_streaming',
    defaultMessage: 'Page streaming',
    id: 'enum.UserRole.PAGE_STREAMING',
  },
  KOBO_SYNC: {
    description: 'User role: kobo_sync',
    defaultMessage: 'Kobo sync',
    id: 'enum.UserRole.KOBO_SYNC',
  },
  KOREADER_SYNC: {
    description: 'User role: koreader_sync',
    defaultMessage: 'KOReader sync',
    id: 'enum.UserRole.KOREADER_SYNC',
  },
  USER: {
    description: 'User role: user',
    defaultMessage: 'User',
    id: 'enum.UserRole.USER',
  },
})
