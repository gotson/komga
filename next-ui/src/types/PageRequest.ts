// from Vuetify
import type { PageSize } from '@/types/page'

export type SortItem = { key: string; order?: boolean | 'asc' | 'desc' }

function vSortItemToSort(sortItem: SortItem): string {
  let sort = sortItem.key
  if (sortItem.order && typeof sortItem.order === 'string') sort += `,${sortItem.order}`
  return sort
}

export class PageRequest {
  readonly unpaged?: boolean
  readonly page?: number
  readonly size?: number
  readonly sort?: string[]

  static FromPageSize(pageSize: PageSize, page?: number, sort?: string[]) {
    return new PageRequest(
      page,
      pageSize === 'unpaged' ? undefined : pageSize,
      sort,
      pageSize === 'unpaged',
    )
  }

  static Unpaged(): PageRequest {
    return new PageRequest(undefined, undefined, undefined, true)
  }

  static Zero(): PageRequest {
    return new PageRequest(undefined, 0, undefined, undefined)
  }

  /**
   * Can be used from v-data-table-server @update:options
   * @param page
   * @param size
   * @param sortItems
   * @constructor
   */
  static FromVuetify(page?: number, size?: number, sortItems?: SortItem[]): PageRequest {
    if (size && size < 0)
      return new PageRequest(
        undefined,
        undefined,
        sortItems?.map((x) => vSortItemToSort(x)),
        true,
      )
    return new PageRequest(
      page,
      size,
      sortItems?.map((x) => vSortItemToSort(x)),
      false,
    )
  }

  constructor(page?: number, size?: number, sort?: string[], unpaged?: boolean) {
    if (page && page < 0) throw new Error('page cannot be negative')
    if (size && size < 0) throw new Error('size cannot be negative')

    this.page = page
    this.size = size
    this.sort = sort
    this.unpaged = unpaged
  }
}
