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
const { libraries } = useGetLibrariesById(libraryId)

provide(filterKeys.context, { library_id: libraries.value?.map((it) => it.id) })

//TODO: for now we always redirect to 'recommended', this should be persisted per libraryId
watchImmediate(libraryId, () => {
  void router.replace({
    name: '/libraries/[id]/series',
    params: { id: libraryId.value },
    query: route.query,
  })
})

const { anyPinned, anyUnpinned } = useLibraries()

watch([libraryId, anyPinned, anyUnpinned], ([id, hasPinned, hasUnpinned]) => {
  if (id === 'pinned' && !hasPinned)
    void router.push({ name: '/libraries/[id]', params: { id: 'all' } })
  if (id === 'unpinned' && !hasUnpinned)
    void router.push({ name: '/libraries/[id]', params: { id: 'all' } })
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
