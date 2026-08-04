import { ClientSettingUser } from '@/types/ClientSettingsUser'
import type { ClientSettingUserUpdateDto } from '@/generated/openapi'
import { handleGetUserSettings } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

const settings: Partial<Record<ClientSettingUser, ClientSettingUserUpdateDto>> = {
  [ClientSettingUser.NextUILibraries]: {
    value: JSON.stringify({}),
  },
}

export const clientSettingsHandlers = [handleGetUserSettings(() => response200OK(settings))]
