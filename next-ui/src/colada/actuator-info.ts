import { defineQuery, useQuery } from '@pinia/colada'
import type { ActuatorInfo } from '@/types/actuator'
import { komgaGetActuatorInfo } from '@/generated/openapi'

export const useActuatorInfo = defineQuery(() => {
  const { data, ...rest } = useQuery({
    key: () => ['actuator-info'],
    query: () => komgaGetActuatorInfo().then((data) => data as ActuatorInfo),
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
