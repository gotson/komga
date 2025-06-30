import { httpTyped } from '@/mocks/api/httpTyped'

export const sharingLabels = ['kids', 'teens']

export const referentialHandlers = [
  httpTyped.get('/api/v1/sharing-labels', ({ response }) => response(200).json(sharingLabels)),
]
