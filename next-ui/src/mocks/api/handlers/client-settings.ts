import { httpTyped } from '@/mocks/api/httpTyped'
import { CLIENT_SETTING_USER } from '@/types/ClientSettingsUser'
import type { components } from '@/generated/openapi/komga'

const settings: Record<string, components['schemas']['ClientSettingUserUpdateDto']> = {
  [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: {
    value: JSON.stringify({}),
  },
}

export const clientSettingsHandlers = [
  httpTyped.get('/api/v1/client-settings/user/list', ({ response }) =>
    response(200).json(settings),
  ),
  httpTyped.patch('/api/v1/client-settings/user', ({ response }) => response(204).empty()),
]
