import { HttpResponse } from 'msw'

export const baseUrl = import.meta.env.VITE_KOMGA_API_URL + '/'

export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })
