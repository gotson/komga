import { handleGetClaimStatus } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

export const claimHandlers = [handleGetClaimStatus(() => response200OK({ isClaimed: true }))]
