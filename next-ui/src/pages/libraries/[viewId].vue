<template>
  <LibraryNavigation
    :key="libraryViewId"
    :library-view-id="libraryViewId"
  />
</template>

<script lang="ts" setup>
import { watchImmediate } from '@vueuse/core'
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

watchImmediate([noLibraries, route.name], async ([newNoLibraries]) => {
  if (newNoLibraries) {
    await nextTick()
    void router.push({ name: '/libraries/create' })
  }
})

//TODO: for now we always redirect to 'overview', this should be persisted per viewId or pinned somehow
watchImmediate(
  () => route,
  async (newRoute) => {
    if (newRoute.name === '/libraries/[viewId]') {
      await nextTick()
      void router.replace({
        name: '/libraries/[viewId]/overview',
        params: { viewId: libraryViewId.value },
      })
    }
  },
)

watchImmediate([libraryViewId, anyPinned, anyUnpinned], async ([id, hasPinned, hasUnpinned]) => {
  if ((id === 'pinned' && !hasPinned) || (id === 'unpinned' && !hasUnpinned)) {
    await nextTick()
    void router.replace({
      params: { ...route.params, viewId: 'all' },
      query: { ...route.query },
    })
  }
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
