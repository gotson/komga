<template>
  <LibraryNavigation
    :key="libraryViewId"
    :library-view-id="libraryViewId"
  />
</template>

<script lang="ts" setup>
import { filterKeys } from '@/types/filter'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useLibraries } from '@/colada/libraries'

const route = useRoute('/libraries/[viewId]')
const router = useRouter()
const { noLibraries, anyPinned, anyUnpinned } = useLibraries()
const libraryViewId = computed(() => route.params.viewId)
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)

provide(
  filterKeys.context,
  computed(() => ({ library_id: libraryIds.value })),
)

watch(
  [noLibraries, () => route, anyPinned, anyUnpinned],
  async ([newNoLibraries, newRoute, hasPinned, hasUnpinned]) => {
    if (newNoLibraries) {
      await nextTick()
      void router.push({ name: '/libraries/create' })
    } else {
      let redirectToAll = false
      let redirectToOverview = false

      if (
        (newRoute?.params.viewId === 'pinned' && !hasPinned) ||
        (newRoute?.params.viewId === 'unpinned' && !hasUnpinned)
      ) {
        redirectToAll = true
      }

      if (newRoute.name === '/libraries/[viewId]') {
        //TODO: for now we always redirect to 'overview', this should be persisted per viewId or pinned somehow
        redirectToOverview = true
      }

      if (redirectToAll || redirectToOverview) {
        await nextTick()
        void router.replace({
          name: redirectToOverview ? '/libraries/[viewId]/overview' : newRoute.name,
          params: { viewId: redirectToAll ? 'all' : newRoute.params.viewId },
        } as Parameters<typeof router.replace>[0])
      }
    }
  },
  { deep: true, immediate: true },
)
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
