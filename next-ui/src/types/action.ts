export type Action<T> = {
  title: string
  icon?: string
  action: T
  onClick?: () => void
  onMouseenter?: (event: Event) => unknown
  href?: string
  disabled?: boolean
}

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

export enum LibrariesAction {
  REORDER,
  SCAN_ALL,
  EMPTY_TRASH_ALL,
}

export const librariesActionGroups = {
  default: [LibrariesAction.REORDER],
  management: [LibrariesAction.SCAN_ALL, LibrariesAction.EMPTY_TRASH_ALL],
}
