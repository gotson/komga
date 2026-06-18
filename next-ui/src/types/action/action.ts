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
export enum ActionName {
  SELECT,
  EDIT_SERIES,
  EDIT_BOOK,
  EDIT_COLLECTION,
  EDIT_READLIST,
  DELETE,
  ADD_TO_COLLECTION,
  ADD_TO_READLIST,
  MARK_READ,
  MARK_UNREAD,
  DOWNLOAD,
  REFRESH_METADATA,
  ANALYZE,
  OPEN_READER,
  OPEN_READER_INCOGNITO,
}

// available actions per entity kind for selection
export const selectionActions: Record<EntityKind, ActionName[]> = {
  book: [
    ActionName.MARK_READ,
    ActionName.MARK_UNREAD,
    ActionName.ADD_TO_READLIST,
    ActionName.REFRESH_METADATA,
    ActionName.ANALYZE,
    ActionName.EDIT_BOOK,
    ActionName.DELETE,
  ],
  series: [
    ActionName.MARK_READ,
    ActionName.MARK_UNREAD,
    ActionName.ADD_TO_READLIST,
    ActionName.ADD_TO_COLLECTION,
    ActionName.REFRESH_METADATA,
    ActionName.ANALYZE,
    ActionName.EDIT_SERIES,
    ActionName.DELETE,
  ],
  collection: [ActionName.DELETE],
  readlist: [ActionName.DELETE],
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
  [ActionName.SELECT]: {
    message: {
      description: 'Generic action: add to selection',
      defaultMessage: 'Select',
      id: 'lFGLru',
    },
    icon: 'i-mdi:plus-box-outline',
  },
  [ActionName.EDIT_SERIES]: actionEdit,
  [ActionName.EDIT_BOOK]: actionEdit,
  [ActionName.EDIT_COLLECTION]: actionEdit,
  [ActionName.EDIT_READLIST]: actionEdit,
  [ActionName.DELETE]: {
    message: {
      description: 'Generic action: delete',
      defaultMessage: 'Delete',
      id: 'lFGLru',
    },
    icon: 'i-mdi:delete',
  },
  [ActionName.ADD_TO_COLLECTION]: {
    message: {
      description: 'Generic action: add to collection',
      defaultMessage: 'Add to collection',
      id: 'lFGLru',
    },
    icon: 'i-mdi:playlist-plus',
  },
  [ActionName.ADD_TO_READLIST]: {
    message: {
      description: 'Generic action: add to read list',
      defaultMessage: 'Add to read list',
      id: 'lFGLru',
    },
    icon: 'i-mdi:book-plus-multiple',
  },
  [ActionName.MARK_READ]: {
    message: {
      description: 'Generic action: mark as read',
      defaultMessage: 'Mark as read',
      id: 'lFGLru',
    },
    icon: 'i-mdi:book-check-outline',
  },
  [ActionName.MARK_UNREAD]: {
    message: {
      description: 'Generic action: mark as unread',
      defaultMessage: 'Mark as unread',
      id: 'lFGLru',
    },
    icon: 'i-mdi:book-remove-outline',
  },
  [ActionName.DOWNLOAD]: {
    message: {
      description: 'Generic action: download',
      defaultMessage: 'Download',
      id: 'jn8Lib',
    },
    icon: undefined,
  },
  [ActionName.REFRESH_METADATA]: {
    message: {
      description: 'Generic action: refresh metadata',
      defaultMessage: 'Refresh metadata',
      id: 'lFGLru',
    },
    icon: 'i-mdi:table-refresh',
  },
  [ActionName.ANALYZE]: {
    message: {
      description: 'Generic action: analyze',
      defaultMessage: 'Analyze',
      id: 'lFGLru',
    },
    icon: 'i-mdi:file-refresh',
  },
  [ActionName.OPEN_READER]: {
    message: {
      description: 'Generic action: read',
      defaultMessage: 'Read',
      id: 'Y7Y2T0',
    },
    icon: 'i-mdi:play',
  },
  [ActionName.OPEN_READER_INCOGNITO]: {
    message: {
      description: 'Generic action: read incognito',
      defaultMessage: 'Private reading session',
      id: 'DLUIbm',
    },
    icon: 'i-mdi:incognito',
  },
}
