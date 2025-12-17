<template>
  <LibraryHolder
    :key="libraryId"
    :library-id="libraryId"
  />
</template>

<script lang="ts" setup>
const route = useRoute()
const router = useRouter()
const libraryId = computed(() => route.params.id)

//TODO: for now we always redirect to 'recommended', this should be persisted per libraryId
watch(libraryId, () => {
  void router.push({ name: '/libraries/[id]/recommended', params: { id: libraryId.value } })
})
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
