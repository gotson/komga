import * as v from 'valibot'
import { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'
import type { SeriesMetadataDto } from '@/generated/openapi'
import type { BulkUpdateBookOneShotFields } from '@/types/entityUpdate'

export const vOneShotAttributes = v.pick(vSeriesMetadataDto, [
  'publisher',
  'publisherLock',
  'ageRating',
  'ageRatingLock',
  'genres',
  'genresLock',
  'language',
  'languageLock',
  'readingDirection',
  'readingDirectionLock',
  'titleSort',
  'titleSortLock',
  'sharingLabels',
  'sharingLabelsLock',
])
export type OneShotAttributes = v.InferOutput<typeof vOneShotAttributes>

export type OneShotAttributesBulk = Pick<SeriesMetadataDto, BulkUpdateBookOneShotFields>
