import { httpTyped } from '@/mocks/api/httpTyped'
import { ThumbnailSize } from '@/types/ThumbnailSize'

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

export const settingsHandlers = [
  httpTyped.get('/api/v1/settings', ({ response }) => response(200).json(settings)),
  httpTyped.patch('/api/v1/settings', ({ response }) => response(204).empty()),
]
