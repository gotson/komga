export enum CollectionAction {
  EDIT,
  DELETE,
}

// convenience groups for action filtering and ordering
export const collectionActionGroups = {
  management: [CollectionAction.EDIT, CollectionAction.DELETE],
}
