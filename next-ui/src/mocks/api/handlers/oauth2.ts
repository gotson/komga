import { handleGetOAuth2Providers } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

export const oauth2Handlers = [handleGetOAuth2Providers(() => response200OK([]))]
