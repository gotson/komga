import { PageRequest } from '@/types/PageRequest'

export function mockPage<T>(data: T[], pageRequest: PageRequest) {
  const page = Number(pageRequest.page) || 0
  const size = Number(pageRequest.size) || 20
  const unpaged = pageRequest.unpaged || false

  const start = page * size
  const slice = unpaged ? data : data.slice(start, start + size)

  return {
    content: slice,
    pageable: {
      pageNumber: page,
      pageSize: size,
      sort: {
        empty: false,
        unsorted: false,
        sorted: true,
      },
      offset: size * page,
      unpaged: unpaged,
      paged: !unpaged,
    },
    last: false,
    totalPages: Math.ceil(data.length / size),
    totalElements: data.length,
    first: false,
    size: size,
    number: 1,
    sort: {
      empty: false,
      unsorted: false,
      sorted: true,
    },
    numberOfElements: slice.length,
    empty: slice.length > 0,
  }
}
