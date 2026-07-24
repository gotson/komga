<template>
  <v-app-bar>
    <ChipCount
      class="ms-4"
      :count="totalElements"
    />

    <v-spacer />

    <PosterSizeSlider />

    <PageSizeSelector
      v-if="isBrowsingPaged"
      v-model="appStore.browsingPageSize"
      allow-unpaged
    />

    <PagingSelector
      v-model="appStore.browsingPaging"
      class="px-2"
    />
  </v-app-bar>

  <ItemBrowser
    v-model:page1="page1"
    :items="dataItems"
    :presentation-mode="'grid'"
    :has-next-page="hasNextPage"
    :page-count="pageCount"
    @load-next-page="loadNextPage()"
  >
    <template #default="{ item, isSelected, preSelect, toggleSelect }">
      <CollectionCard
        stretch-poster
        :collection="item"
        :selected="isSelected"
        :pre-select="preSelect"
        :width="display.xs.value ? 'auto' : appStore.gridCardWidth"
        @selection="(_val, event) => toggleSelect(event as MouseEvent)"
      />
    </template>
  </ItemBrowser>
</template>

<script lang="ts" setup>
import { useInfiniteQuery, useQuery } from '@pinia/colada'
import { PageRequest } from '@/types/PageRequest'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useDisplay } from 'vuetify'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { storeToRefs } from 'pinia'
import ChipCount from '@/components/ChipCount.vue'
import { collectionsListQuery, collectionsListQueryInfinite } from '@/colada/collections'
import { useSelectionContextualActions } from '@/composables/selection'
import { watchImmediate } from '@vueuse/core'

const router = useRouter()
const route = useRoute('/libraries/[id]/collections')
const libraryViewId = route.params.id
const { libraryIds } = useGetLibrariesByViewId(libraryViewId)

const display = useDisplay()
const appStore = useAppStore()
const { isBrowsingScroll, isBrowsingPaged } = storeToRefs(appStore)

const { page0, page1, pageCount } = usePagination()

const selectionStore = useSelectionStore()

// clear selection if paging changes
watch(
  () => appStore.browsingPaging,
  () => selectionStore.clear(),
)

const { data: dataPaged } = useQuery(() => ({
  ...collectionsListQuery({
    libraryIds: libraryIds.value,
    pageRequest: PageRequest.FromPageSize(appStore.browsingPageSize, page0.value),
  }),
  enabled: isBrowsingPaged.value,
}))

watch(dataPaged, (newDataPaged) => {
  if (newDataPaged) pageCount.value = newDataPaged.totalPages ?? 0
})

const {
  data: dataInfinite,
  loadNextPage,
  hasNextPage,
} = useInfiniteQuery(() => ({
  ...collectionsListQueryInfinite({ libraryIds: libraryIds.value }),
  enabled: isBrowsingScroll.value,
}))
const dataInfiniteFlat = computed(() =>
  dataInfinite.value?.pages.flatMap((it) => it?.content ?? []),
)

const dataItems = computed(() =>
  isBrowsingPaged.value ? dataPaged.value?.content : dataInfiniteFlat.value,
)
const totalElements = computed(() =>
  isBrowsingPaged.value
    ? dataPaged.value?.totalElements
    : dataInfinite.value?.pages?.[0]?.totalElements,
)

watchImmediate(totalElements, async (newTotalElements) => {
  if (newTotalElements == 0) {
    // avoid router navigation failure
    await nextTick()
    void router.replace({ name: '/libraries/[id]', params: { id: libraryViewId } })
  }
})

useSelectionContextualActions(
  dataItems,
)
</script>

<style lang="scss"></style>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
