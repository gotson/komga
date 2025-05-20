import {useQuery} from '@pinia/colada'
import {komgaClient} from '@/api/komga-client'
import type {ActuatorInfo} from '@/types/Actuator'

export function useActuatorInfo() {
  return useQuery({
    key: () => ['actuator-info'],
    query: () => komgaClient.GET('/actuator/info')
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data as ActuatorInfo),
    // 1 hour
    staleTime: 60 * 60 * 1000,
    gcTime: false,
  })
}
