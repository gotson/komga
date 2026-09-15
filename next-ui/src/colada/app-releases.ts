import { defineQuery, useQuery } from '@pinia/colada'
import { komgaGetReleases } from '@/generated/openapi'

export const useAppReleases = defineQuery(() =>
  useQuery({
    key: () => ['app-releases'],
    query: () => komgaGetReleases(),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  }),
)
