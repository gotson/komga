import { defineQuery, useQuery } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { ActuatorInfo } from '@/types/Actuator'

export const useActuatorInfo = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => ['actuator-info'],
    query: () =>
      komgaClient
        .GET('/actuator/info')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data as ActuatorInfo),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })

  const buildVersion = computed(() => data.value?.build?.version)
  const commitId = computed(() => data.value?.git?.commit?.id)

  return {
    data,
    ...rest,
    buildVersion,
    commitId,
  }
})
