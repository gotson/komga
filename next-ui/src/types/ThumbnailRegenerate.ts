import type { SettingsUpdateDto } from '@/generated/openapi'

export type ThumbnailRegenerate = 'no' | 'bigger' | 'all'
export type SettingsUpdateDtoExtended = SettingsUpdateDto & {
  thumbnailRegenerate: ThumbnailRegenerate
}
