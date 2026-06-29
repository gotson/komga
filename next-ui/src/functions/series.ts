import type { SeriesMetadataDto, SeriesMetadataUpdateDto } from '@/generated/openapi'

export function seriesMetadataToDto(metadata: SeriesMetadataDto): SeriesMetadataUpdateDto {
  return Object.assign({}, metadata, {
    readingDirection: metadata.readingDirection as
      | 'LEFT_TO_RIGHT'
      | 'RIGHT_TO_LEFT'
      | 'VERTICAL'
      | 'WEBTOON',
    status: metadata.status as 'ENDED' | 'ONGOING' | 'ABANDONED' | 'HIATUS',
  })
}
