import { PageDto } from '@/types/komga-books'

export function isPageLandscape (p: PageDto): boolean {
  return (p?.width ?? 0) > (p?.height ?? 0)
}
