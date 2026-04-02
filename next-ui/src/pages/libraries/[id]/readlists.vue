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
      :sizes="[1, 10, 20]"
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
      <ReadlistCard
        stretch-poster
        :read-list="item"
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
import { useGetLibrariesById } from '@/composables/libraries'
import { useAppStore } from '@/stores/app'
import { usePagination } from '@/composables/pagination'
import { useSelectionStore } from '@/stores/selection'
import { useDisplay } from 'vuetify'
import { komgaClient } from '@/api/komga-client'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { storeToRefs } from 'pinia'
import ChipCount from '@/components/ChipCount.vue'
import { readListsListQuery } from '@/colada/readlists'

const route = useRoute('/libraries/[id]/readlists')
const libraryId = route.params.id
const { libraryIds } = useGetLibrariesById(libraryId)

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
  ...readListsListQuery({
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
} = useInfiniteQuery({
  key: () => ['infinite_readlists', { libraryIds: libraryIds.value }],
  initialPageParam: new PageRequest(0, 50),
  query: ({ pageParam }) =>
    komgaClient
      .GET('/api/v1/readlists', {
        params: {
          query: {
            page: pageParam.page,
            size: pageParam.size,
            libraryIds: libraryIds.value,
          },
        },
      })
      // unwrap the openapi-fetch structure on success
      .then((res) => res.data),
  getNextPageParam: (lastPage, _, lastPageParam) => (!lastPage?.last ? lastPageParam.next() : null),
  enabled: isBrowsingScroll,
})
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
</script>

<style lang="scss"></style>

<route lang="yaml">
meta:
  requiresRole: USER
  scrollable: true
</route>
