import type { components } from '@/generated/openapi/komga'

export function seriesMetadataToDto(
  metadata: components['schemas']['SeriesMetadataDto'],
): components['schemas']['SeriesMetadataUpdateDto'] {
  return Object.assign({}, metadata, {
    readingDirection: metadata.readingDirection as
      | 'LEFT_TO_RIGHT'
      | 'RIGHT_TO_LEFT'
      | 'VERTICAL'
      | 'WEBTOON',
    status: metadata.status as 'ENDED' | 'ONGOING' | 'ABANDONED' | 'HIATUS',
  })
}
