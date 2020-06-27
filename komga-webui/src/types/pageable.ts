interface Page<T> {
  content: T[],
  pageable: Pageable,
  size: number,
  number: number,
  totalPages: number,
  first: boolean,
  last: boolean,
  numberOfElements: number,
  totalElements: number,
  empty: boolean
  sort: Sort,
}

interface Pageable {
  sort: Sort,
  offset: number,
  pageNumber: number,
  pageSize: number,
  unpaged: boolean,
  paged: boolean
}

interface Sort {
  sorted: boolean,
  unsorted: boolean,
  empty: boolean
}

interface PageRequest {
  size?: number,
  page?: number,
  sort?: string[],
  unpaged?: boolean
}

interface SortOption {
  name: string,
  key: string
}

interface SortActive {
  key: string,
  order: string
}
