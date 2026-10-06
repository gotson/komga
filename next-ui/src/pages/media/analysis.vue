<template>
  <v-empty-state
    icon="i-mdi:map-marker-path"
    color="primary"
    :title="
      $formatMessage({
        description: 'Media analysis: title',
        defaultMessage: 'Media analysis has moved',
        id: 'YaCWYs',
      })
    "
    :text="
      $formatMessage({
        description: 'Media analysis: text',
        defaultMessage: 'Books in error can now be filtered directly in the main library view.',
        id: 'YeFgyc',
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
const link = enrichRouteQuery(route, filterToQuery('mediaStatus', { v: ['ERROR', 'UNSUPPORTED'] }))!
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
