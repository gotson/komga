import { defineQuery, useQuery } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { useActuatorInfo } from '@/colada/actuator-info'
import { combinePromises } from '@/colada/utils'

export const useAppReleases = defineQuery(() => {
  const {
    data,
    refresh: refreshReleases,
    refetch: refetchReleases,
    ...rest
  } = useQuery({
    key: () => ['app-releases'],
    query: () =>
      komgaClient
        .GET('/api/v1/releases')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const { buildVersion, refresh: refreshActuator, refetch: refetchActuator } = useActuatorInfo()

  const refresh = combinePromises(refreshReleases, [refreshActuator])
  const refetch = combinePromises(refetchReleases, [refetchActuator])

  const latestRelease = computed(() => data.value?.find((x) => x.latest))

  const isLatestVersion = computed(() => {
    if (buildVersion.value && data.value)
      return data.value?.some((x) => x.latest && x.version == buildVersion.value)
    else return undefined
  })

  return {
    data,
    refresh,
    refetch,
    ...rest,
    buildVersion,
    latestRelease,
    isLatestVersion,
  }
})
