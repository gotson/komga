<template>
  <v-empty-state
    icon="i-mdi:map-marker-path"
    color="primary"
    :title="
      $formatMessage({
        description: 'Missing posters: title',
        defaultMessage: 'Missing posters has moved',
        id: 'ZiTv+F',
      })
    "
    :text="
      $formatMessage({
        description: 'Missing posters: text',
        defaultMessage:
          'Books with missing posters can now be filtered directly in the main library view.',
        id: 'ELl0N9',
      })
    "
  >
    <template #actions>
      <v-btn :to="link">View filtered books</v-btn>
    </template>
  </v-empty-state>
</template>

<script lang="ts" setup>
import { filterToQuery } from '@/functions/filter'
import { enrichRouteQuery } from '@/functions/router'

const route = {
  name: '/libraries/[viewId]/books',
  params: { viewId: 'all' },
  replace: true,
} as const
const link = enrichRouteQuery(
  route,
  filterToQuery('poster', { v: [{ v: ['ANYTYPE', true], i: 'e' }] }),
  filterToQuery('mediaStatus', { v: ['READY'] }),
)!
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
