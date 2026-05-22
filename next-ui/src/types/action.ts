import type { BookAction } from '@/types/book'
import type { SeriesAction } from '@/types/series'
import type { CollectionAction } from '@/types/collection'
import type { ReadListAction } from '@/types/readlist'

export type ActionName = BookAction | SeriesAction | CollectionAction | ReadListAction

export type Action = {
  title: string
  action: ActionName
  onClick?: () => void
  onMouseenter?: (event: Event) => unknown
  href?: string
}
