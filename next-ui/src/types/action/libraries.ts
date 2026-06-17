export enum LibrariesAction {
  REORDER,
  SCAN_ALL,
  EMPTY_TRASH_ALL,
}

export const librariesActionGroups = {
  default: [LibrariesAction.REORDER],
  management: [LibrariesAction.SCAN_ALL, LibrariesAction.EMPTY_TRASH_ALL],
}
