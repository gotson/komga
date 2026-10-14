import { defineMessage, type MessageDescriptor } from 'vue-intl'
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
  EditCollectionElements: 'EDIT_COLLECTION_ELEMENTS',
  EditReadList: 'EDIT_READLIST',
  EditReadListElements: 'EDIT_READLIST_ELEMENTS',
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
  book_oneshot: [
    ActionName.MarkRead,
    ActionName.MarkUnread,
    ActionName.AddToReadList,
    ActionName.AddToCollection,
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
  message: defineMessage({
    description: 'Generic action: edit',
    defaultMessage: 'Edit',
    id: 'wk+b55',
  }),
  icon: 'i-mdi:pencil',
}
const actionEditElements = {
  message: defineMessage({
    description: 'Generic action: edit elements',
    defaultMessage: 'Organize',
    id: 'dto0D+',
  }),
  icon: 'i-mdi:playlist-edit',
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
  [ActionName.EditCollectionElements]: actionEditElements,
  [ActionName.EditReadList]: actionEdit,
  [ActionName.EditReadListElements]: actionEditElements,
  [ActionName.Delete]: {
    message: defineMessage({
      description: 'Generic action: delete',
      defaultMessage: 'Delete',
      id: 'q6fppQ',
    }),
    icon: 'i-mdi:delete',
  },
  [ActionName.AddToCollection]: {
    message: defineMessage({
      description: 'Generic action: add to collection',
      defaultMessage: 'Add to collection',
      id: 'tj5v0L',
    }),
    icon: 'i-mdi:playlist-plus',
  },
  [ActionName.AddToReadList]: {
    message: defineMessage({
      description: 'Generic action: add to read list',
      defaultMessage: 'Add to read list',
      id: 'LBuB6T',
    }),
    icon: 'i-mdi:book-plus-multiple',
  },
  [ActionName.MarkRead]: {
    message: defineMessage({
      description: 'Generic action: mark as read',
      defaultMessage: 'Mark as read',
      id: 'LOcuZ2',
    }),
    icon: 'i-mdi:book-check-outline',
  },
  [ActionName.MarkUnread]: {
    message: defineMessage({
      description: 'Generic action: mark as unread',
      defaultMessage: 'Mark as unread',
      id: 'Ulohnz',
    }),
    icon: 'i-mdi:book-remove-outline',
  },
  [ActionName.Download]: {
    message: defineMessage({
      description: 'Generic action: download',
      defaultMessage: 'Download',
      id: 'ruWj+g',
    }),
    icon: undefined,
  },
  [ActionName.RefreshMetadata]: {
    message: defineMessage({
      description: 'Generic action: refresh metadata',
      defaultMessage: 'Refresh metadata',
      id: 'nYtEQI',
    }),
    icon: 'i-mdi:table-refresh',
  },
  [ActionName.Analyze]: {
    message: defineMessage({
      description: 'Generic action: analyze',
      defaultMessage: 'Analyze',
      id: '1dgTdi',
    }),
    icon: 'i-mdi:file-refresh',
  },
  [ActionName.OpenReader]: {
    message: defineMessage({
      description: 'Generic action: read',
      defaultMessage: 'Read',
      id: 'Ju97UA',
    }),
    icon: 'i-mdi:play',
  },
  [ActionName.OpenReaderIncognito]: {
    message: defineMessage({
      description: 'Generic action: read incognito',
      defaultMessage: 'Private reading session',
      id: 'ST34jK',
    }),
    icon: 'i-mdi:incognito',
  },
}
