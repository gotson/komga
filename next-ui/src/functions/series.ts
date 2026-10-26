import type { SeriesMetadataDto, SeriesMetadataUpdateDto } from '@/generated/openapi'
import type { ReadingDirection } from '@/types/ReadingDirection'
import type { SeriesStatus } from '@/types/SeriesStatus'

export function seriesMetadataToUpdateDto(
  metadata: Partial<SeriesMetadataDto>,
): SeriesMetadataUpdateDto {
  return Object.assign({}, metadata, {
    readingDirection: metadata.readingDirection
      ? (metadata.readingDirection as ReadingDirection)
      : null,
    status: metadata.status as SeriesStatus,
    publisher: metadata.publisher ?? '',
    language: metadata.language ?? '',
  })
}
