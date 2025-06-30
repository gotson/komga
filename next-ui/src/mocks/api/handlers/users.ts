import { httpTyped } from '@/mocks/api/httpTyped'

export const users = [
  {
    id: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    roles: ['ADMIN', 'FILE_DOWNLOAD', 'KOBO_SYNC', 'KOREADER_SYNC', 'PAGE_STREAMING', 'USER'],
    sharedAllLibraries: true,
    sharedLibrariesIds: [],
    labelsAllow: [],
    labelsExclude: [],
    ageRestriction: null,
  },
  {
    id: '0JEDA00AZ4QXH',
    email: 'user@example.org',
    roles: ['KOBO_SYNC', 'PAGE_STREAMING', 'USER'],
    sharedAllLibraries: true,
    sharedLibrariesIds: [],
    labelsAllow: [],
    labelsExclude: ['book'],
    ageRestriction: null,
  },
]

const latestActivity = {
  userId: '0JEDA00AV4Z7G',
  email: 'admin@example.org',
  apiKeyId: null,
  apiKeyComment: null,
  ip: '127.0.0.1',
  userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
  success: true,
  error: null,
  dateTime: new Date('2025-06-30T06:56:33Z'),
  source: 'Password',
}

export const usersHandlers = [
  httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(users[0])),
  httpTyped.get('/api/v2/users', ({ response }) => response(200).json(users)),
  httpTyped.get('/api/v2/users/{userId}/authentication-activity/latest', ({ response }) =>
    response(200).json(latestActivity),
  ),
]
