<template>
  <LibraryTabNavigation
    :routes="routes"
    :library-id="libraryId"
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
import { useIntl } from 'vue-intl'
import type { Route } from '@/types/route'

const intl = useIntl()

const props = defineProps<{
  libraryId: LibraryId
}>()

const { libraries } = useGetLibrariesById(props.libraryId)
const { data: collections } = useQuery(() => ({
  ...collectionsListQuery({
    libraryIds: libraries.value?.map((it) => it.id),
    pageRequest: PageRequest.Zero(),
  }),
  enabled: libraries.value !== undefined,
}))
const { data: readlists } = useQuery(() => ({
  ...readListsListQuery({
    libraryIds: libraries.value?.map((it) => it.id),
    pageRequest: PageRequest.Zero(),
  }),
  enabled: libraries.value !== undefined,
}))

const routesBase = [
  {
    title: intl.formatMessage({
      description: 'Library navigation: overview',
      defaultMessage: 'Overview',
      id: 'MKs9N+',
    }),
    icon: 'i-mdi:star',
    to: { name: '/libraries/[id]/overview', params: { id: props.libraryId } },
  } as Route,
  {
    title: intl.formatMessage({
      description: 'Library navigation: series',
      defaultMessage: 'Series',
      id: 'lt7Tru',
    }),
    icon: 'i-mdi:bookshelf',
    to: { name: '/libraries/[id]/series', params: { id: props.libraryId } },
  } as Route,
  {
    title: intl.formatMessage({
      description: 'Library navigation: books',
      defaultMessage: 'Books',
      id: 'pTsA/M',
    }),
    icon: 'i-mdi:book-multiple',
    to: { name: '/libraries/[id]/books', params: { id: props.libraryId } },
  } as Route,
]

const routes = computed(() => {
  const extra = []
  if ((collections.value?.totalElements ?? 0) > 0)
    extra.push({
      title: intl.formatMessage({
        description: 'Library navigation: collections',
        defaultMessage: 'Collections',
        id: 'cyQk6S',
      }),
      icon: 'i-mdi:layers-triple',
      to: { name: '/libraries/[id]/collections', params: { id: props.libraryId } },
    })
  if ((readlists.value?.totalElements ?? 0) > 0)
    extra.push({
      title: intl.formatMessage({
        description: 'Library navigation: read lists',
        defaultMessage: 'Read Lists',
        id: 'w2K5yu',
      }),
      icon: 'i-mdi:bookmark-multiple',
      to: { name: '/libraries/[id]/readlists', params: { id: props.libraryId } },
    })
  return [...routesBase, ...extra] as Route[]
})
</script>

<style scoped></style>
