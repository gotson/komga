import type { EntityKind } from '@/functions/entity'
import { getIntersection } from '@/functions/array'
import { ActionName, selectionActions } from '@/types/action/action'

export function getCommonActions(kinds: (EntityKind | undefined)[]): ActionName[] {
  return getIntersection(...kinds.map((it) => (it === undefined ? [] : selectionActions[it])))
}
