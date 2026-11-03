import type { RouteLocationObject } from '@/types/route'

export type LibrarySubRoute = {
  title: string
  icon?: string
  to: RouteLocationObject
}

export type LibrarySubType = 'series' | 'books' | 'readlists' | 'collections' | 'overview'
