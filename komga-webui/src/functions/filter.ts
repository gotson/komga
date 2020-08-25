export function sortOrFilterActive (sortActive: SortActive, sortDefault: SortActive, filters: FiltersActive): boolean {
  const sortCustom = sortActive.key !== sortDefault.key || sortActive.order !== sortDefault.order
  const filterCustom = Object.keys(filters).some(x => filters[x].length !== 0)
  return sortCustom || filterCustom
}
