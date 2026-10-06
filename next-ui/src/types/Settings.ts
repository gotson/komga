import type { SettingsUpdateDto } from '@/generated/openapi'
import type { ClientSettingOauth2Login } from '@/types/ClientSettingsGlobal'

export type ThumbnailRegenerate = 'no' | 'bigger' | 'all'

export type SettingsUpdateDtoExtended = SettingsUpdateDto & {
  thumbnailRegenerate: ThumbnailRegenerate
} & {
  oauth2: ClientSettingOauth2Login
}
