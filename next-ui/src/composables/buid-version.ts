  import {useActuatorInfo} from '@/colada/queries/actuator-info'

export function useBuildVersion() {
  const {data} = useActuatorInfo()

  const buildVersion = computed(() => data.value?.build?.version)

  return {buildVersion}
}
