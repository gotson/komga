import { actuatorHandlers } from '@/mocks/api/handlers/actuator'
import { announcementHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'
import { librariesHandlers } from '@/mocks/api/handlers/libraries'
import { referentialHandlers } from '@/mocks/api/handlers/referential'
import { usersHandlers } from '@/mocks/api/handlers/users'
import { settingsHandlers } from '@/mocks/api/handlers/settings'
import { claimHandlers } from '@/mocks/api/handlers/claim'
import { historyHandlers } from '@/mocks/api/handlers/history'
import { seriesHandlers } from '@/mocks/api/handlers/series'
import { booksHandlers } from '@/mocks/api/handlers/books'
import { filesystemHandlers } from '@/mocks/api/handlers/filesystem'
import { transientBooksHandlers } from '@/mocks/api/handlers/transient-books'
import { readListsHandlers } from '@/mocks/api/handlers/readlists'
import { pageHashesHandlers } from '@/mocks/api/handlers/page-hashes'
import { clientSettingsHandlers } from '@/mocks/api/handlers/client-settings'
import { collectionsHandlers } from '@/mocks/api/handlers/collections'
import { createMswHandlers } from '@/generated/openapi/msw.gen'

const mswHandlers = createMswHandlers()

export const handlers = [
  ...actuatorHandlers,
  ...announcementHandlers,
  ...booksHandlers,
  ...claimHandlers,
  ...clientSettingsHandlers,
  ...collectionsHandlers,
  ...filesystemHandlers,
  ...historyHandlers,
  ...librariesHandlers,
  ...pageHashesHandlers,
  ...readListsHandlers,
  ...referentialHandlers,
  ...releasesHandlers,
  ...seriesHandlers,
  ...settingsHandlers,
  ...transientBooksHandlers,
  ...usersHandlers,
  ...mswHandlers.all(),
]
