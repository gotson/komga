import { httpTyped } from '@/mocks/api/httpTyped'

export const userAdmin = {
  id: '0JEDA00AV4Z7G',
  email: 'admin@example.org',
  roles: ['ADMIN', 'FILE_DOWNLOAD', 'KOBO_SYNC', 'KOREADER_SYNC', 'PAGE_STREAMING', 'USER'],
  sharedAllLibraries: true,
  sharedLibrariesIds: [],
  labelsAllow: [],
  labelsExclude: [],
}
export const userRegular = {
  id: '0JEDA00AZ4QXH',
  email: 'user@example.org',
  roles: ['KOBO_SYNC', 'PAGE_STREAMING', 'USER'],
  sharedAllLibraries: true,
  sharedLibrariesIds: [],
  labelsAllow: [],
  labelsExclude: ['book'],
}
export const users = [userAdmin, userRegular]

const latestActivity = {
  userId: '0JEDA00AV4Z7G',
  email: 'admin@example.org',
  ip: '127.0.0.1',
  userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
  success: true,
  dateTime: new Date('2025-06-30T06:56:33Z'),
  source: 'Password',
}

export const usersHandlers = [
  httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userAdmin)),
  httpTyped.get('/api/v2/users', ({ response }) => response(200).json(users)),
  httpTyped.get('/api/v2/users/{id}/authentication-activity/latest', ({ response }) =>
    response(200).json(latestActivity),
  ),
]
