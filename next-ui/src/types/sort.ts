export type SortOption = {
  // for display
  label: string
  // sorting key sent to API
  key: string
  // default ordering
  defaultOrder: 'asc' | 'desc'
}
