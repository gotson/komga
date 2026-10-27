import type { EntityDto } from '@/functions/entity'
import type { PosterDto, PosterUpload } from '@/functions/poster'

export type PosterUpdate = {
  uploadQueue: PosterUpload[]
  deleteQueue: PosterDto[]
  selected?: PosterDto
}

export type EntityUpdate<T extends EntityDto, E> = {
  entity: T
  extra?: E
} & PosterUpdate

export function createEntityUpdate<T extends EntityDto, E>(
  entity: T,
  extra?: E,
): EntityUpdate<T, E> {
  return {
    entity: entity,
    uploadQueue: [],
    deleteQueue: [],
    selected: undefined,
    extra: extra,
  }
}
