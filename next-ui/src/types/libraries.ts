/**
 * Represents either a specific library ID, or a group of libraries
 */
// eslint-disable-next-line @typescript-eslint/no-redundant-type-constituents
export type LibraryViewId = string | 'all' | 'pinned' | 'unpinned'
export type PresentationMode = 'grid' | 'list' | 'table'
