interface SeriesDto {
  id: string,
  libraryId: string,
  name: string,
  url: string,
  lastModified: string,
  booksCount: number,
  booksReadCount: number,
  booksUnreadCount: number,
  booksInProgressCount: number,
  metadata: SeriesMetadata
}

interface SeriesMetadata {
  status: string,
  statusLock: boolean,
  created: string,
  lastModified: string,
  title: string,
  titleLock: boolean,
  titleSort: string,
  titleSortLock: boolean
}

interface SeriesMetadataUpdateDto {
  status?: string,
  statusLock?: boolean,
  title?: string,
  titleLock?: boolean,
  titleSort?: string,
  titleSortLock?: boolean
}
