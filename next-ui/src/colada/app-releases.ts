import { defineQuery, useQuery } from '@pinia/colada'
import { komgaGetReleases } from '@/generated/openapi'
import { STALE_TIME } from '@/types/time'

export const useAppReleases = defineQuery(() =>
  useQuery({
    key: () => ['app-releases'],
    query: () => komgaGetReleases(),
    staleTime: STALE_TIME.LONG,
    gcTime: false,
  }),
)
