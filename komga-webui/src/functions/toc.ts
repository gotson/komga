import {TocEntry} from '@/types/epub'

export function flattenToc(toc: TocEntry[], maxLevel: number = 0, currentLevel: number = 0, active?: string): TocEntry[] {
  const r: TocEntry[] = []

  for (const item of toc) {
    const flat = Object.assign({}, item, {level: currentLevel}) as TocEntry
    if (flat.href === active) flat.current = true
    const children: TocEntry[] = []
    if (item.children) {
      children.push(...flattenToc(item.children, maxLevel, currentLevel + 1, active))
    }
    if (currentLevel >= maxLevel) {
      delete flat.children
      r.push(flat)
      r.push(...children)
    } else {
      flat.children = children.length > 0 ? children : undefined
      r.push(flat)
    }
  }

  return r
}
