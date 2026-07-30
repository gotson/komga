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

watch([noLibraries, anyPinned, anyUnpinned], ([newNoLibraries, hasPinned, hasUnpinned]) => {
  if (newNoLibraries) {
    void router.push({ name: '/libraries/create' })
  } else if (!hasPinned || !hasUnpinned) {
    void router.replace({
      name: '/libraries/[viewId]/overview',
      params: { viewId: 'all' },
    })
  }
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
