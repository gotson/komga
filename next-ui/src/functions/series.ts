import type { SeriesMetadataDto, SeriesMetadataUpdateDto } from '@/generated/openapi'

export function seriesMetadataToUpdateDto(
  metadata: Partial<SeriesMetadataDto>,
): SeriesMetadataUpdateDto {
  return Object.assign({}, metadata, {
    readingDirection: metadata.readingDirection
      ? (metadata.readingDirection as 'LEFT_TO_RIGHT' | 'RIGHT_TO_LEFT' | 'VERTICAL' | 'WEBTOON')
      : null,
    status: metadata.status as 'ENDED' | 'ONGOING' | 'ABANDONED' | 'HIATUS',
  })
}
