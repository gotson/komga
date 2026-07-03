export const LibraryAction = {
  Scan: 'SCAN',
  ScanDeep: 'SCAN_DEEP',
  Edit: 'EDIT',
  RefreshMetadata: 'REFRESH_METADATA',
  Analyze: 'ANALYZE',
  Delete: 'DELETE',
  EmptyTrash: 'EMPTY_TRASH',
} as const

export type LibraryAction = (typeof LibraryAction)[keyof typeof LibraryAction]
