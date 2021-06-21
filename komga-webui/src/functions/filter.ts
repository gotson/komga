export function sortOrFilterActive (sortActive: SortActive, sortDefault: SortActive, filters: FiltersActive): boolean {
  const sortCustom = sortActive.key !== sortDefault.key || sortActive.order !== sortDefault.order
  const filterCustom = Object.keys(filters).some(x => filters[x].length !== 0)
  return sortCustom || filterCustom
}

export function mergeFilterParams (filter: FiltersActive, query: any) {
  for (const f of Object.keys(filter)) {
    if (filter[f].length !== 0) query[f] = filter[f]
  }
}

export function toNameValue (list: string[]): NameValue[] {
  return list.map(x => ({ name: x, value: x } as NameValue))
}
