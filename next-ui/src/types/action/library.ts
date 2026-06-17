export enum LibraryAction {
  SCAN,
  SCAN_DEEP,
  EDIT,
  REFRESH_METADATA,
  ANALYZE,
  DELETE,
  EMPTY_TRASH,
}

export const libraryActionGroups = {
  default: [LibraryAction.SCAN],
  management: [
    LibraryAction.EDIT,
    LibraryAction.SCAN_DEEP,
    LibraryAction.REFRESH_METADATA,
    LibraryAction.EMPTY_TRASH,
    LibraryAction.ANALYZE,
    LibraryAction.DELETE,
  ],
}
