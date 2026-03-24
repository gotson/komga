import type { PageSize } from '@/types/page'

// from Vuetify
export type VSortItem = { key: string; order?: boolean | 'asc' | 'desc' }

export type Sort = {
  key: string
  order?: 'asc' | 'desc'
}

function vSortItemToSort(sortItem: VSortItem): Sort {
  return {
    key: sortItem.key,
    order: typeof sortItem.order === 'string' ? sortItem.order : undefined,
  }
}

function sortToString(sortItem: Sort | string): string {
  if (typeof sortItem === 'string') return sortItem

  let sort = sortItem.key
  if (sortItem.order) sort += `,${sortItem.order}`
  return sort
}

export class PageRequest {
  readonly unpaged?: boolean
  readonly page?: number
  readonly size?: number
  readonly sort?: string[]

  static FromPageSize(pageSize: PageSize, page?: number, sort?: Sort[]) {
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
  static FromVuetify(page?: number, size?: number, sortItems?: VSortItem[]): PageRequest {
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

  constructor(page?: number, size?: number, sort?: Sort[] | string[], unpaged?: boolean) {
    if (page && page < 0) throw new Error('page cannot be negative')
    if (size && size < 0) throw new Error('size cannot be negative')

    this.page = page
    this.size = size
    this.sort = sort?.map((it) => sortToString(it))
    this.unpaged = unpaged
  }
}
