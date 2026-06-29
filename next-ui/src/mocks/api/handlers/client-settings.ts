import { CLIENT_SETTING_USER } from '@/types/ClientSettingsUser'
import type { ClientSettingUserUpdateDto } from '@/generated/openapi'
import { handleGetUserSettings } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

const settings: Record<string, ClientSettingUserUpdateDto> = {
  [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: {
    value: JSON.stringify({}),
  },
}

export const clientSettingsHandlers = [handleGetUserSettings(() => response200OK(settings))]
