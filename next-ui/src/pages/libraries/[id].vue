<template>
  <LibraryNavigation
    :key="libraryId"
    :library-id="libraryId"
  />
</template>

<script lang="ts" setup>
import { watchImmediate } from '@vueuse/core'

const route = useRoute('/libraries/[id]')
const router = useRouter()
const libraryId = computed(() => route.params.id)

//TODO: for now we always redirect to 'recommended', this should be persisted per libraryId
watchImmediate(libraryId, () => {
  void router.replace({
    name: '/libraries/[id]/series',
    params: { id: libraryId.value },
    query: route.query,
  })
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
