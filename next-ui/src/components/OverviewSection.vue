<template>
  <ItemScroller
    v-if="items.length > 0"
    :items="items"
    :title="$formatMessage(overviewSectionMessages[section.section])"
    :title-to="routeTo"
    :has-next-page="hasNextPage"
    class="with-gap"
    @load-next-page="loadNextPage()"
  >
    <template #default="{ item, isSelected, toggleSelect, preSelect }">
      <div class="py-2 px-1">
        <SeriesCard
          v-if="kind === 'series'"
          :series="item as SeriesDto"
          stretch-poster
          :selected="isSelected"
          :pre-select="preSelect"
          :width="cardWidth"
          @selection="toggleSelect"
        />
        <BookCard
          v-else
          :book="item as BookDto"
          stretch-poster
          show-series
          :selected="isSelected"
          :pre-select="preSelect"
          :width="cardWidth"
          @selection="toggleSelect"
        />
      </div>
    </template>
  </ItemScroller>
</template>

<script setup lang="ts">
import { type LibraryViewId } from '@/types/libraries'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useOverviewSection } from '@/composables/section'
import type { BookDto, PageBookDto, PageSeriesDto, SeriesDto } from '@/generated/openapi'
import { useInfiniteQuery } from '@pinia/colada'
import { overviewSectionMessages } from '@/types/OverviewSection'
import { useAppStore } from '@/stores/app'
import { useDisplay } from 'vuetify'
import type { ClientSettingUserOverviewSection } from '@/types/ClientSettingsUser'
import type { RouteLocationRaw } from 'vue-router'

const props = defineProps<{
  section: ClientSettingUserOverviewSection
  libraryViewId: LibraryViewId
}>()

const display = useDisplay()
const appStore = useAppStore()
const { libraryIds } = useGetLibrariesByViewId(props.libraryViewId)

const cardWidth = computed(() => (display.smAndUp.value ? appStore.gridCardWidth : 130))

const { queryOptions, kind } = useOverviewSection(props.section.section, libraryIds)

const { data, hasNextPage, loadNextPage } = useInfiniteQuery(() => queryOptions.value as never)

const items = computed(() => {
  const pages = data.value?.pages as (PageBookDto | PageSeriesDto)[] | undefined
  return pages?.flatMap((it) => (it?.content as (BookDto | SeriesDto)[]) ?? []) ?? []
})

const routeTo = computed<RouteLocationRaw>(() => ({
  name: '/libraries/[id]/overview/[section]',
  params: { id: props.libraryViewId, section: props.section.section },
}))
</script>

<style scoped>
.with-gap :deep(.v-slide-group__content) {
  gap: 8px;
}
</style>
