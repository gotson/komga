export enum ReadListAction {
  DOWNLOAD,
  EDIT,
  DELETE,
}

// convenience groups for action filtering and ordering
export const readListActionGroups = {
  default: [ReadListAction.DOWNLOAD],
  management: [ReadListAction.EDIT, ReadListAction.DELETE],
}
