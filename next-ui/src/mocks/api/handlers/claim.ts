import { httpTyped } from '@/mocks/api/httpTyped'

export const claimHandlers = [
  httpTyped.get('/api/v1/claim', ({ response }) => response(200).json({ isClaimed: true })),
]
