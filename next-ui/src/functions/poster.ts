import * as v from 'valibot'
import type {
  ThumbnailBookDto,
  ThumbnailSeriesDto,
  ThumbnailSeriesCollectionDto,
  ThumbnailReadListDto,
} from '@/generated/openapi'
import {
  bookPosterUrl,
  collectionPosterUrl,
  readListPosterUrl,
  seriesPosterUrl,
} from '@/api/images'
import type { EntityDto } from '@/functions/entity'

// fallback in case we can't get it from the server info API
export const POSTER_SIZE_LIMIT = 1_048_576

export type PosterDto =
  ThumbnailBookDto | ThumbnailSeriesDto | ThumbnailReadListDto | ThumbnailSeriesCollectionDto

export type PosterUpload = {
  file: File
  selected: boolean
}

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

// Using looseObject ensures it doesn't fail due to the other fields in the DTO
const BookPosterDiscriminator = v.looseObject({
  bookId: v.string(),
})

const SeriesPosterDiscriminator = v.looseObject({
  seriesId: v.string(),
})

const CollectionPosterDiscriminator = v.looseObject({
  collectionId: v.string(),
})

const ReadListPosterDiscriminator = v.looseObject({
  readListId: v.string(),
})

// Type Guards
export function isBookPoster(poster: unknown): poster is ThumbnailBookDto {
  return v.is(BookPosterDiscriminator, poster)
}

export function isSeriesPoster(poster: unknown): poster is ThumbnailSeriesDto {
  return v.is(SeriesPosterDiscriminator, poster)
}

export function isCollectionPoster(poster: unknown): poster is ThumbnailSeriesCollectionDto {
  return v.is(CollectionPosterDiscriminator, poster)
}

export function isReadListPoster(poster: unknown): poster is ThumbnailReadListDto {
  return v.is(ReadListPosterDiscriminator, poster)
}

export function isPosterDto(poster: unknown): poster is PosterDto {
  return (
    isBookPoster(poster) ||
    isSeriesPoster(poster) ||
    isCollectionPoster(poster) ||
    isReadListPoster(poster)
  )
}

export function resolvePosterUrl(poster: unknown): string | undefined {
  if (isBookPoster(poster)) return bookPosterUrl(poster.bookId, undefined, poster.id)
  if (isSeriesPoster(poster)) return seriesPosterUrl(poster.seriesId, undefined, poster.id)
  if (isCollectionPoster(poster))
    return collectionPosterUrl(poster.collectionId, undefined, poster.id)
  if (isReadListPoster(poster)) return readListPosterUrl(poster.readListId, undefined, poster.id)
  return undefined
}

export function fileIdentifier(file: File) {
  return `${file.name}-${file.size}-${file.lastModified}`
}
