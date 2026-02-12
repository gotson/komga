import { PageRequest } from '@/types/PageRequest'

export function mockPage<T>(data: T[], pageRequest: PageRequest) {
  const page = Number(pageRequest.page) || 0
  const size = Number(pageRequest.size) || 20
  const unpaged = pageRequest.unpaged || false
  const sort = pageRequest.sort
  const totalPages = Math.ceil(data.length / size)

  const start = page * size
  const slice = unpaged ? data : data.slice(start, start + size)

  let sortedSlice = slice
  if (sort) {
    sortedSlice = slice.sort(orderBy(parseSort(sort)))
  }

  return {
    content: sortedSlice,
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
    last: page === totalPages - 1,
    totalPages: totalPages,
    totalElements: data.length,
    first: page === 0,
    size: size,
    number: page,
    sort: {
      empty: false,
      unsorted: false,
      sorted: true,
    },
    numberOfElements: slice.length,
    empty: slice.length > 0,
  }
}

function parseSort(sorts: string[]): OrderBy[] {
  return sorts.map((sort) => {
    const components = sort.split(',')
    return {
      property: components[0]!,
      direction: components[1] === 'desc' ? 'desc' : 'asc',
    }
  })
}

type OrderBy = {
  property: string
  direction?: 'desc' | 'asc'
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const orderBy = (items: OrderBy[]) => (a: any, b: any) => {
  const sortDirection: Record<string, number> = { asc: 1, desc: -1 }
  const sortCollator = new Intl.Collator(undefined, { numeric: true, sensitivity: 'base' })
  const totalOrders = items.length

  for (let index = 0; index < totalOrders; index++) {
    const { property, direction = 'desc' } = items[index]!
    const directionInt = sortDirection[direction]!
    const compare = sortCollator.compare(a[property], b[property])

    if (compare < 0) return directionInt
    if (compare > 0) return -directionInt
  }

  return 0
}
