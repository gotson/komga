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

const newApiKey = {
  id: '0JJJHMRR586H7',
  userId: '0JEDA00AV4Z7G',
  key: '8dad7cbdb4e3420b9536f726c9123346',
  comment: 'Kobo Libra',
  createdDate: new Date('2025-01-21T07:04:42Z'),
  lastModifiedDate: new Date('2025-01-21T07:04:42Z'),
}

export const apiKeys = [
  {
    id: '0JJJHMRR586H7',
    userId: '0JEDA00AV4Z7G',
    key: '******',
    comment: 'Kobo Libra',
    createdDate: new Date('2025-01-21T07:04:42Z'),
    lastModifiedDate: new Date('2025-01-21T07:04:42Z'),
  },
  {
    id: '0JQCGQV2Z6NVD',
    userId: '0JEDA00AV4Z7G',
    key: '******',
    comment: 'Komf',
    createdDate: new Date('2025-02-05T05:51:31Z'),
    lastModifiedDate: new Date('2025-02-05T05:51:31Z'),
  },
  {
    id: '0K2PJZMP0Q6WA',
    userId: '0JEDA00AV4Z7G',
    key: '******',
    comment: 'Kobo Sage',
    createdDate: new Date('2025-03-12T09:32:35Z'),
    lastModifiedDate: new Date('2025-03-12T09:32:35Z'),
  },
]

export const usersHandlers = [
  httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userAdmin)),
  httpTyped.get('/api/v2/users', ({ response }) => response(200).json(users)),
  httpTyped.get('/api/v2/users/{id}/authentication-activity/latest', ({ response }) =>
    response(200).json(latestActivity),
  ),
  httpTyped.get('/api/v2/users/me/api-keys', ({ response }) => response(200).json(apiKeys)),
  httpTyped.post('/api/v2/users/me/api-keys', ({ response }) => response(200).json(newApiKey)),
]
