import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

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
  dateTime: new Date(new Date('2025-06-30T06:56:33Z')),
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

export const authenticationActivity = [
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: false,
    error: 'Bad credentials',
    dateTime: new Date('2025-07-24T03:29:46Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'jacky@example.org',
    apiKeyId: '0MDR59N06BFJN',
    apiKeyComment: 'Kobo',
    ip: '0:0:0:0:0:0:0:1',
    userAgent: 'HTTPie/3.2.4',
    success: true,
    dateTime: new Date('2025-07-24T03:32:00Z'),
    source: 'ApiKey',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'michel@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Linux; Android 11; SAMSUNG SM-G973U) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/14.2 Chrome/87.0.4280.141 Mobile Safari/537.36',
    success: true,
    dateTime: new Date('2025-07-22T05:14:33Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'jean@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-22T02:31:49Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-21T06:14:54Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-21T06:13:05Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'jacky@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-21T06:09:30Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-16T04:05:25Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-15T07:44:42Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'michel@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-15T07:44:07Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-15T07:40:28Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-15T06:07:31Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'jean@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-14T06:32:27Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-14T06:32:25Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-14T06:03:59Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-14T01:53:05Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:140.0) Gecko/20100101 Firefox/140.0',
    success: true,
    dateTime: new Date('2025-07-07T07:20:46Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-30T06:56:33Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-30T01:16:27Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-27T01:37:31Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-26T03:30:23Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-25T08:58:19Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-25T08:38:13Z'),
    source: 'Password',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-25T07:19:39Z'),
    source: 'RememberMe',
  },
  {
    userId: '0JEDA00AV4Z7G',
    email: 'admin@example.org',
    ip: '127.0.0.1',
    userAgent:
      'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0',
    success: true,
    dateTime: new Date('2025-06-25T03:16:33Z'),
    source: 'Password',
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
  httpTyped.get('/api/v2/users/authentication-activity', ({ query, response }) =>
    response(200).json(
      mockPage(
        authenticationActivity,
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    ),
  ),
  httpTyped.get('/api/v2/users/me/authentication-activity', ({ query, response }) =>
    response(200).json(
      mockPage(
        authenticationActivity.filter((x) => x.email === 'jacky@example.org'),
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    ),
  ),
]
