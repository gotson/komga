import {useAppReleases} from '@/colada/queries/app-releases.ts'
import {useBuildVersion} from '@/composables/buid-version.ts'

export function useLatestVersion() {
  const {data } = useAppReleases()

  const {buildVersion} = useBuildVersion()
  const latestRelease = computed(() => data.value?.find(x => x.latest))

  const isLatestVersion = computed(() => {
    if(buildVersion.value && latestRelease.value)
      return buildVersion.value == latestRelease.value?.version
    else return undefined
  })

  return {isLatestVersion, latestRelease}
}
