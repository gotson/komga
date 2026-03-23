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

function sortToString(sortItem: Sort): string {
  let sort = sortItem.key
  if (sortItem.order) sort += `,${sortItem.order}`
  return sort
}

export class PageRequest {
  readonly unpaged?: boolean
  readonly page?: number
  readonly size?: number
  private readonly _sort?: Sort[]

  get sort(): string[] | undefined {
    return this._sort?.map((it) => sortToString(it))
  }

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

  constructor(page?: number, size?: number, sort?: Sort[], unpaged?: boolean) {
    if (page && page < 0) throw new Error('page cannot be negative')
    if (size && size < 0) throw new Error('size cannot be negative')

    this.page = page
    this.size = size
    this._sort = sort
    this.unpaged = unpaged
  }
}
