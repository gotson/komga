<template>
  <LibraryTabNavigation
    :routes="routes"
    :library-view-id="libraryViewId"
  />

  <RouterView />
</template>

<script setup lang="ts">
import type { LibraryViewId } from '@/types/libraries'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useQuery } from '@pinia/colada'
import { collectionsListQuery } from '@/colada/collections'
import { PageRequest } from '@/types/PageRequest'
import { readListsListQuery } from '@/colada/readlists'
import { useIntl } from 'vue-intl'
import type { Route } from '@/types/route'

const intl = useIntl()

const props = defineProps<{
  libraryViewId: LibraryViewId
}>()

const { libraries } = useGetLibrariesByViewId(props.libraryViewId)
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

const routesBase: Route[] = [
  {
    title: intl.formatMessage({
      description: 'Library navigation: overview',
      defaultMessage: 'Overview',
      id: 'MKs9N+',
    }),
    icon: 'i-mdi:star',
    to: { name: '/libraries/[viewId]/overview', params: { viewId: props.libraryViewId } },
  },
  {
    title: intl.formatMessage({
      description: 'Library navigation: series',
      defaultMessage: 'Series',
      id: 'lt7Tru',
    }),
    icon: 'i-mdi:bookshelf',
    to: { name: '/libraries/[viewId]/series', params: { viewId: props.libraryViewId } },
  },
  {
    title: intl.formatMessage({
      description: 'Library navigation: books',
      defaultMessage: 'Books',
      id: 'pTsA/M',
    }),
    icon: 'i-mdi:book-multiple',
    to: { name: '/libraries/[viewId]/books', params: { viewId: props.libraryViewId } },
  },
]

const routes = computed(() => {
  const extra: Route[] = []
  if ((collections.value?.totalElements ?? 0) > 0)
    extra.push({
      title: intl.formatMessage({
        description: 'Library navigation: collections',
        defaultMessage: 'Collections',
        id: 'cyQk6S',
      }),
      icon: 'i-mdi:layers-triple',
      to: { name: '/libraries/[viewId]/collections', params: { viewId: props.libraryViewId } },
    })
  if ((readlists.value?.totalElements ?? 0) > 0)
    extra.push({
      title: intl.formatMessage({
        description: 'Library navigation: read lists',
        defaultMessage: 'Read Lists',
        id: 'w2K5yu',
      }),
      icon: 'i-mdi:bookmark-multiple',
      to: { name: '/libraries/[viewId]/readlists', params: { viewId: props.libraryViewId } },
    })
  return [...routesBase, ...extra] as Route[]
})
</script>

<style scoped></style>
