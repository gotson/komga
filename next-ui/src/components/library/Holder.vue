<template>
  <LibraryBottomNavigation
    v-if="display.xs.value"
    :routes="routes"
  />

  <LibraryTabNavigation
    v-else
    :routes="routes"
  />

  <RouterView />
</template>

<script setup lang="ts">
import type { LibraryId } from '@/types/libraries'
import { useGetLibrariesById } from '@/composables/libraries'
import { useQuery } from '@pinia/colada'
import { collectionsListQuery } from '@/colada/collections'
import { PageRequest } from '@/types/PageRequest'
import { readListsListQuery } from '@/colada/readlists'
import { useDisplay } from 'vuetify'

const { libraryId } = defineProps<{
  libraryId: LibraryId
}>()

const display = useDisplay()

const { libraries } = useGetLibrariesById(libraryId)
const { data: collections } = useQuery(collectionsListQuery, () => ({
  libraryIds: libraries.value?.map((it) => it.id),
  pageRequest: PageRequest.Zero(),
  pause: libraries.value === undefined,
}))
const { data: readlists } = useQuery(readListsListQuery, () => ({
  libraryIds: libraries.value?.map((it) => it.id),
  pageRequest: PageRequest.Zero(),
  pause: libraries.value === undefined,
}))

const routesBase = [
  { title: 'Recommended', icon: 'i-mdi:star', to: `/libraries/${libraryId}/recommended` },
  { title: 'Series', icon: 'i-mdi:bookshelf', to: `/libraries/${libraryId}/series` },
  { title: 'Books', icon: 'i-mdi:book-multiple', to: `/libraries/${libraryId}/books` },
]

const routes = computed(() => {
  const extra = []
  if ((collections.value?.totalElements ?? 0) > 0)
    extra.push({
      title: 'Collections',
      icon: 'i-mdi:layers-triple',
      to: `/libraries/${libraryId}/collections`,
    })
  if ((readlists.value?.totalElements ?? 0) > 0)
    extra.push({
      title: 'Read Lists',
      icon: 'i-mdi:bookmark-multiple',
      to: `/libraries/${libraryId}/readlists`,
    })
  return [...routesBase, ...extra]
})
</script>

<style scoped></style>
