import * as v from 'valibot'
import {
  ClientSettingUser,
  ClientSettingUserSchemas,
  type ClientSettingUserSettings,
} from '@/types/ClientSettingsUser'
import type { ClientSettingDto } from '@/generated/openapi'

export function parseUserSettings(
  raw: Record<string, Pick<ClientSettingDto, 'value'>> | undefined,
): ClientSettingUserSettings {
  const result: Partial<Record<ClientSettingUser, unknown>> = {}

  for (const settingKey of Object.values(ClientSettingUser)) {
    const schema = ClientSettingUserSchemas[settingKey]
    if (!schema) continue

    const setting = raw?.[settingKey]
    result[settingKey] = setting?.value
      ? v.parse(schema, JSON.parse(setting.value))
      : v.getDefaults(schema)
  }

  return result as ClientSettingUserSettings
}
