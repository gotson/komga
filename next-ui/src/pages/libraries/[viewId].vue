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
const libraryViewId = computed(() => route.params.viewId)
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)

provide(
  filterKeys.context,
  computed(() => ({ library_id: libraryIds.value })),
)

//TODO: for now we always redirect to 'overview', this should be persisted per viewId or pinned somehow
watchImmediate(
  () => route?.name,
  (newRouteName) => {
    if (newRouteName === '/libraries/[viewId]')
      void router.replace({
        name: '/libraries/[viewId]/overview',
        params: { viewId: libraryViewId.value },
      })
  },
)

const { anyPinned, anyUnpinned } = useLibraries()

watch([libraryViewId, anyPinned, anyUnpinned], ([id, hasPinned, hasUnpinned]) => {
  if ((id === 'pinned' && !hasPinned) || (id === 'unpinned' && !hasUnpinned))
    void router.replace({
      params: { ...route.params, viewId: 'all' },
      query: { ...route.query },
    })
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
