<template>
  <v-app-bar>
    <v-app-bar-nav-icon
      icon="i-mdi:arrow-left"
      :to="parentRoute"
      exact
    />
    <v-app-bar-title :text="section" />
  </v-app-bar>
  {{ section }}
</template>

<script lang="ts" setup>
import { OverviewSectionValues } from '@/types/OverviewSection'
import type { RouteLocationRaw } from 'vue-router'

const route = useRoute('/libraries/[viewId]/overview/[section]')
const libraryViewId = route.params.viewId
const section = route.params.section

definePage({
  beforeEnter: (to) => {
    const params = to.params as { viewId: string; section: string }
    const section = params.section

    if (!(OverviewSectionValues as unknown as string[]).includes(section)) {
      return { name: '/libraries/[viewId]/overview', params: { viewId: params.viewId } }
    }
  },
})

const parentRoute = computed<RouteLocationRaw>(() => ({
  name: '/libraries/[viewId]/overview',
  params: { viewId: libraryViewId },
}))
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
