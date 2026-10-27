import type { EntityDto } from '@/functions/entity'
import type { PosterDto, PosterUpload } from '@/functions/poster'
import type { BulkItemMultiField, BulkItemSingleField } from '@/types/bulk'
import * as v from 'valibot'
import type { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'

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

export type PosterUpdateBulk = {
  uploadQueue: PosterUpload[]
}

export type EntitiesUpdateBulk<T extends EntityDto, E, U> = {
  entities: {
    entity: T
    extra?: E
  }[]
  bulkUpdates: U
} & PosterUpdateBulk

// Generic mapper that inspects if T[K] extends any[]
export type NormalizeNullable<T> = undefined extends T ? Exclude<T, undefined> | null : T

type MapBulkUpdate<T, K extends keyof T> = {
  [P in K]:
    | (NonNullable<T[P]> extends (infer Element)[]
        ? BulkItemMultiField<Element>[]
        : BulkItemSingleField<NormalizeNullable<T[P]>>)
    | undefined
}

export const BulkUpdateSeriesFields = [
  'status',
  'statusLock',
  'language',
  'languageLock',
  'readingDirection',
  'readingDirectionLock',
  'publisher',
  'publisherLock',
  'ageRating',
  'ageRatingLock',
  'genres',
  'genresLock',
  'tags',
  'tagsLock',
  'sharingLabels',
  'sharingLabelsLock',
] as const
export type BulkUpdateSeriesFields = (typeof BulkUpdateSeriesFields)[number]

export type BulkUpdateSeries = MapBulkUpdate<
  v.InferOutput<typeof vSeriesMetadataDto>,
  BulkUpdateSeriesFields
>
