const sortDirs = ['asc', 'desc']

export function parseQuerySort (querySort: any, sortOptions: SortOption[]): SortActive | null {
  let customSort = null
  if (querySort) {
    const split = querySort.split(',')
    if (split.length === 2 && sortOptions.map(x => x.key).includes(split[0]) && sortDirs.includes(split[1])) {
      customSort = { key: split[0], order: split[1] }
    }
  }
  return customSort
}

export function parseQueryFilter (queryStatus: any, enumeration: any): string[] {
  return queryStatus ? queryStatus.toString().split(',').filter((x: string) => Object.keys(enumeration).includes(x)) : []
}
