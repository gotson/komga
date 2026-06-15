import type { BookAction } from '@/types/book'
import type { SeriesAction } from '@/types/series'
import type { CollectionAction } from '@/types/collection'
import type { ReadListAction } from '@/types/readlist'

export type Action<T extends BookAction | SeriesAction | CollectionAction | ReadListAction> = {
  title: string
  icon?: string
  action: T
  onClick?: () => void
  onMouseenter?: (event: Event) => unknown
  href?: string
  disabled?: boolean
}
