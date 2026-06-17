export enum BookAction {
  ADD_TO_COLLECTION,
  ADD_TO_READLIST,
  MARK_READ,
  MARK_UNREAD,
  DOWNLOAD,
  EDIT_METADATA,
  REFRESH_METADATA,
  ANALYZE,
  DELETE_FILES,
  OPEN_READER,
  OPEN_READER_INCOGNITO,
}

// convenience groups for action filtering and ordering
export const bookActionGroups = {
  default: [
    BookAction.ADD_TO_COLLECTION,
    BookAction.ADD_TO_READLIST,
    BookAction.MARK_READ,
    BookAction.MARK_UNREAD,
    BookAction.DOWNLOAD,
  ],
  management: [
    BookAction.EDIT_METADATA,
    BookAction.REFRESH_METADATA,
    BookAction.ANALYZE,
    BookAction.DELETE_FILES,
  ],
  bookView: [
    BookAction.OPEN_READER_INCOGNITO,
    BookAction.MARK_READ,
    BookAction.MARK_UNREAD,
    BookAction.EDIT_METADATA,
  ],
}
