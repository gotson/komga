import * as v from 'valibot'
import type { components } from '@/generated/openapi/komga'

type BookDto = components['schemas']['BookDto']
type SeriesDto = components['schemas']['SeriesDto']
type CollectionDto = components['schemas']['CollectionDto']
type ReadListDto = components['schemas']['ReadListDto']

// Using looseObject ensures it doesn't fail due to the other fields in the DTO
const BookDiscriminator = v.looseObject({
  seriesTitle: v.string(),
})

const SeriesDiscriminator = v.looseObject({
  booksInProgressCount: v.number(),
})

const CollectionDiscriminator = v.looseObject({
  seriesIds: v.array(v.string()),
})

const ReadListDiscriminator = v.looseObject({
  bookIds: v.array(v.string()),
})

// Type Guards
export function isBook(item: unknown): item is BookDto {
  return v.is(BookDiscriminator, item)
}

export function isSeries(item: unknown): item is SeriesDto {
  return v.is(SeriesDiscriminator, item)
}

export function isCollection(item: unknown): item is CollectionDto {
  return v.is(CollectionDiscriminator, item)
}

export function isReadList(item: unknown): item is ReadListDto {
  return v.is(ReadListDiscriminator, item)
}

export type EntityKind = 'book' | 'series' | 'collection' | 'readlist'
export function resolveEntityKind(item: unknown): EntityKind | undefined {
  if (isBook(item)) return 'book'
  if (isSeries(item)) return 'series'
  if (isCollection(item)) return 'collection'
  if (isReadList(item)) return 'readlist'
  return undefined
}
