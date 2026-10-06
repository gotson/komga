import { defineQuery, useQuery } from '@pinia/colada'
import { komgaGetOAuth2Providers } from '@/generated/openapi'

export const useOAuth2Providers = defineQuery(() => {
  return useQuery({
    key: () => ['oauth2', 'providers'],
    query: () => komgaGetOAuth2Providers(),
  })
})
