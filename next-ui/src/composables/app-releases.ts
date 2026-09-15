import { useAppReleases } from '@/colada/app-releases'
import { useActuatorInfo } from '@/colada/actuator-info'

/**
 * Composable that returns more detailed app releases information.
 */
export function useAppReleasesEnriched() {
  const { data, isLoading: isLoadingReleases, ...restReleases } = useAppReleases()
  const { buildVersion, isLoading: isLoadingActuator } = useActuatorInfo()

  const isLoading = computed(() => isLoadingReleases.value || isLoadingActuator.value)

  const latestRelease = computed(() => data.value?.find((x) => x.latest))

  const isLatestVersion = computed(() => {
    if (buildVersion.value && data.value)
      return data.value?.some((x) => x.latest && x.version == buildVersion.value)
    else return undefined
  })

  return {
    releases: data,
    buildVersion,
    latestRelease,
    isLatestVersion,
    isLoading,
    ...restReleases,
  }
}
