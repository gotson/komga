export enum SeriesAction {
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
export const seriesActionGroups = {
  default: [
    SeriesAction.ADD_TO_COLLECTION,
    SeriesAction.ADD_TO_READLIST,
    SeriesAction.MARK_READ,
    SeriesAction.MARK_UNREAD,
    SeriesAction.DOWNLOAD,
  ],
  management: [
    SeriesAction.EDIT_METADATA,
    SeriesAction.REFRESH_METADATA,
    SeriesAction.ANALYZE,
    SeriesAction.DELETE_FILES,
  ],
  seriesView: [
    SeriesAction.OPEN_READER_INCOGNITO,
    SeriesAction.MARK_READ,
    SeriesAction.MARK_UNREAD,
    SeriesAction.EDIT_METADATA,
  ],
}
