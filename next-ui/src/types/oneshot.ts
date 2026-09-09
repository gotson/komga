import * as v from 'valibot'
import { vSeriesMetadataDto } from '@/generated/openapi/valibot.gen'

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
])
export type OneShotAttributes = v.InferOutput<typeof vOneShotAttributes>
