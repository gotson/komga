import {defineQuery, useQuery} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'
import {useActuatorInfo} from '@/colada/queries/actuator-info.ts'

export const useAppReleases = defineQuery(() => {
  const {data, ...rest} = useQuery({
    key: () => ['app-releases'],
    query: () => komgaClient.GET('/api/v1/releases')
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const {buildVersion} = useActuatorInfo()
  const latestRelease = computed(() => data.value?.find(x => x.latest))

  const isLatestVersion = computed(() => {
    if(buildVersion.value && latestRelease.value)
      return buildVersion.value == latestRelease.value?.version
    else return undefined
  })

  return {
    data,
    ...rest,
    buildVersion,
    latestRelease,
    isLatestVersion,
  }
})
