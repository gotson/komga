<template>
  <LibraryNavigation
    :key="libraryId"
    :library-id="libraryId"
  />
</template>

<script lang="ts" setup>
import { watchImmediate } from '@vueuse/core'
import { filterKeys } from '@/types/filter'
import { useGetLibrariesById } from '@/composables/libraries'
import { useLibraries } from '@/colada/libraries'

const route = useRoute('/libraries/[id]')
const router = useRouter()
const libraryId = computed(() => route.params.id)
const { libraryIds } = useGetLibrariesById(libraryId)

provide(
  filterKeys.context,
  computed(() => ({ library_id: libraryIds.value })),
)

//TODO: for now we always redirect to 'overview', this should be persisted per viewId or pinned somehow
watchImmediate(
  () => route?.name,
  (newRouteName) => {
    if (newRouteName === '/libraries/[id]')
      void router.replace({
        name: '/libraries/[id]/overview',
        params: { id: libraryId.value },
      })
  },
)

const { anyPinned, anyUnpinned } = useLibraries()

watch([libraryId, anyPinned, anyUnpinned], ([id, hasPinned, hasUnpinned]) => {
  if ((id === 'pinned' && !hasPinned) || (id === 'unpinned' && !hasUnpinned))
    void router.replace({
      params: { ...route.params, id: 'all' },
      query: { ...route.query },
    })
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
