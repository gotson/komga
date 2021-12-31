const sortDirs = ['asc', 'desc']

export function parseQuerySort(querySort: any, sortOptions: SortOption[]): SortActive | null {
  let customSort = null
  if (querySort) {
    const split = querySort.split(',')
    if (split.length === 2 && sortOptions.map(x => x.key).includes(split[0]) && sortDirs.includes(split[1])) {
      customSort = {key: split[0], order: split[1]}
    }
  }
  return customSort
}

export function parseBooleanFilter(values?: string[]): boolean | undefined {
  if (!values || values.length === 0) return undefined
  if (values[0].trim().toLowerCase() === 'true') return true
  if (values[0].trim().toLowerCase() === 'false') return false
  return undefined
}
