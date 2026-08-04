<template>
  <v-app-bar>
    <v-app-bar-nav-icon
      icon="i-mdi:arrow-left"
      :to="parentRoute"
      exact
    />
    <v-app-bar-title :text="$formatMessage(overviewSectionMessages[section])" />
  </v-app-bar>

  <ItemBrowser
    paging="scroll"
    :items="items"
    :presentation-mode="'grid'"
    :has-next-page="hasNextPage"
    @load-next-page="loadNextPage()"
  >
    <template #default="{ item, isSelected, preSelect, toggleSelect }">
      <SeriesCard
        v-if="kind === 'series'"
        :series="item as SeriesDto"
        stretch-poster
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />
      <BookCard
        v-else
        :book="item as BookDto"
        stretch-poster
        show-series
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />
    </template>
  </ItemBrowser>
</template>

<script lang="ts" setup>
import {
  type OverviewSection,
  overviewSectionMessages,
  OverviewSectionValues,
} from '@/types/OverviewSection'
import { useDisplay } from 'vuetify/framework'
import { useAppStore } from '@/stores/app'
import { useOverviewSection } from '@/composables/section'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useInfiniteQuery } from '@pinia/colada'
import type { BookDto, PageBookDto, PageSeriesDto, SeriesDto } from '@/generated/openapi'
import type { RouteLocationRaw } from 'vue-router'

definePage({
  beforeEnter: (to) => {
    const params = to.params as { viewId: string; section: string }
    const section = params.section

    if (!(OverviewSectionValues as unknown as string[]).includes(section)) {
      return { name: '/libraries/[viewId]/overview', params: { viewId: params.viewId } }
    }
  },
})

const display = useDisplay()
const appStore = useAppStore()

const route = useRoute('/libraries/[viewId]/overview/[section]')
const libraryViewId = route.params.viewId
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)
const section = route.params.section as OverviewSection

const { queryOptions, kind } = useOverviewSection(section, libraryIds)

const { data, hasNextPage, loadNextPage } = useInfiniteQuery(() => queryOptions.value as never)

const items = computed(() => {
  const pages = data.value?.pages as (PageBookDto | PageSeriesDto)[] | undefined
  return pages?.flatMap((it) => (it?.content as (BookDto | SeriesDto)[]) ?? []) ?? []
})

const parentRoute = computed<RouteLocationRaw>(() => ({
  name: '/libraries/[viewId]/overview',
  params: { viewId: libraryViewId },
}))
</script>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
