import type { MessageDescriptor } from 'vue-intl'
import type { EntityKind } from '@/functions/entity'

export type Action<T> = {
  title: string
  icon?: string
  action: T
  onClick?: () => void
  onMouseenter?: (event: Event) => unknown
  href?: string
  disabled?: boolean
}

// Actions used for entities: Book, Series, Collection, ReadList
// By using a single enum, we can find the common actions between different entities for the selection bulk actions
export const ActionName = {
  Select: 'SELECT',
  EditSeries: 'EDIT_SERIES',
  EditBook: 'EDIT_BOOK',
  EditCollection: 'EDIT_COLLECTION',
  EditReadList: 'EDIT_READLIST',
  Delete: 'DELETE',
  AddToCollection: 'ADD_TO_COLLECTION',
  AddToReadList: 'ADD_TO_READLIST',
  MarkRead: 'MARK_READ',
  MarkUnread: 'MARK_UNREAD',
  Download: 'DOWNLOAD',
  RefreshMetadata: 'REFRESH_METADATA',
  Analyze: 'ANALYZE',
  OpenReader: 'OPEN_READER',
  OpenReaderIncognito: 'OPEN_READER_INCOGNITO',
} as const

export type ActionName = (typeof ActionName)[keyof typeof ActionName]

// available actions per entity kind for selection
export const selectionActions: Record<EntityKind, ActionName[]> = {
  book: [
    ActionName.MarkRead,
    ActionName.MarkUnread,
    ActionName.AddToReadList,
    ActionName.RefreshMetadata,
    ActionName.Analyze,
    ActionName.EditBook,
    ActionName.Delete,
  ],
  series: [
    ActionName.MarkRead,
    ActionName.MarkUnread,
    ActionName.AddToReadList,
    ActionName.AddToCollection,
    ActionName.RefreshMetadata,
    ActionName.Analyze,
    ActionName.EditSeries,
    ActionName.Delete,
  ],
  collection: [ActionName.Delete],
  readlist: [ActionName.Delete],
}

const actionEdit = {
  message: {
    description: 'Generic action: edit',
    defaultMessage: 'Edit',
    id: 'lFGLru',
  },
  icon: 'i-mdi:pencil',
}
export const actionDetails: Record<ActionName, { message: MessageDescriptor; icon?: string }> = {
  [ActionName.Select]: {
    message: {
      description: 'Generic action: add to selection',
      defaultMessage: 'Select',
      id: 'lFGLru',
    },
    icon: 'i-mdi:plus-box-outline',
  },
  [ActionName.EditSeries]: actionEdit,
  [ActionName.EditBook]: actionEdit,
  [ActionName.EditCollection]: actionEdit,
  [ActionName.EditReadList]: actionEdit,
  [ActionName.Delete]: {
    message: {
      description: 'Generic action: delete',
      defaultMessage: 'Delete',
      id: 'lFGLru',
    },
    icon: 'i-mdi:delete',
  },
  [ActionName.AddToCollection]: {
    message: {
      description: 'Generic action: add to collection',
      defaultMessage: 'Add to collection',
      id: 'lFGLru',
    },
    icon: 'i-mdi:playlist-plus',
  },
  [ActionName.AddToReadList]: {
    message: {
      description: 'Generic action: add to read list',
      defaultMessage: 'Add to read list',
      id: 'lFGLru',
    },
    icon: 'i-mdi:book-plus-multiple',
  },
  [ActionName.MarkRead]: {
    message: {
      description: 'Generic action: mark as read',
      defaultMessage: 'Mark as read',
      id: 'lFGLru',
    },
    icon: 'i-mdi:book-check-outline',
  },
  [ActionName.MarkUnread]: {
    message: {
      description: 'Generic action: mark as unread',
      defaultMessage: 'Mark as unread',
      id: 'lFGLru',
    },
    icon: 'i-mdi:book-remove-outline',
  },
  [ActionName.Download]: {
    message: {
      description: 'Generic action: download',
      defaultMessage: 'Download',
      id: 'jn8Lib',
    },
    icon: undefined,
  },
  [ActionName.RefreshMetadata]: {
    message: {
      description: 'Generic action: refresh metadata',
      defaultMessage: 'Refresh metadata',
      id: 'lFGLru',
    },
    icon: 'i-mdi:table-refresh',
  },
  [ActionName.Analyze]: {
    message: {
      description: 'Generic action: analyze',
      defaultMessage: 'Analyze',
      id: 'lFGLru',
    },
    icon: 'i-mdi:file-refresh',
  },
  [ActionName.OpenReader]: {
    message: {
      description: 'Generic action: read',
      defaultMessage: 'Read',
      id: 'Y7Y2T0',
    },
    icon: 'i-mdi:play',
  },
  [ActionName.OpenReaderIncognito]: {
    message: {
      description: 'Generic action: read incognito',
      defaultMessage: 'Private reading session',
      id: 'DLUIbm',
    },
    icon: 'i-mdi:incognito',
  },
}
