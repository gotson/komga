import { ThumbnailSize } from '@/types/ThumbnailSize'
import { handleGetServerSettings } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

export const settings = {
  deleteEmptyCollections: true,
  deleteEmptyReadLists: false,
  rememberMeDurationDays: 365,
  thumbnailSize: ThumbnailSize.XLARGE,
  taskPoolSize: 8,
  serverPort: { configurationSource: 8090, effectiveValue: 8090 },
  serverContextPath: { effectiveValue: '' },
  koboProxy: false,
  kepubifyPath: {
    configurationSource: '/usr/bin/kepubify',
    effectiveValue: '/usr/bin/kepubify',
  },
}

export const settingsHandlers = [handleGetServerSettings(() => response200OK(settings))]
