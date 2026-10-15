<template>
  <div>
    <ReorderHistoryActions
      v-if="listRef?.canUndo || listRef?.canRedo"
      :can-undo="listRef?.canUndo"
      :can-redo="listRef?.canRedo"
      class="position-absolute top-0 pt-2"
      :class="{
        ['end-0 me-2']: display.xs.value,
        ['left-0 right-0 mx-auto']: display.smAndUp.value,
      }"
      style="width: fit-content"
      @undo="listRef?.undo()"
      @redo="listRef?.redo()"
    />

    <v-list lines="two">
      <ReorderHistoryScroller
        ref="listRef"
        v-model="ids"
        @update:model-value="(val) => (model.seriesIds = val)"
      >
        <template #item="{ item: id, handleClass, deleteItem }">
          <v-skeleton-loader
            v-if="!itemsMap.has(id)"
            type="list-item-avatar-two-line"
          />
          <ReorderHistoryListItem
            v-else
            :disable-reorder="!model.ordered"
            :handle-class="handleClass"
            :title="itemsMap.get(id)?.title"
            :avatar="seriesPosterUrl(id, cacheStore.getVersion(id))"
            @delete="deleteItem()"
          />
        </template>
      </ReorderHistoryScroller>
    </v-list>

    <v-empty-state
      v-if="model.seriesIds.length === 0"
      icon="i-mdi:tray-alert"
      color="warning"
      :title="
        $formatMessage({
          description: 'Organize collection: empty state title - empty list',
          defaultMessage: 'List cannot be empty',
          id: '+C/+3o',
        })
      "
      :text="
        $formatMessage({
          description: 'Organize collection: empty state text - empty list',
          defaultMessage: 'Discard the changes or undo your last deletion',
          id: 'tqrq7S',
        })
      "
    />

    <!--  Hidden input that will trigger the form validation in ConfirmEdit  -->
    <v-input
      :rules="[() => model.seriesIds.length > 0]"
      class="=d-none"
    />
  </div>
</template>

<script setup lang="ts">
import type { CollectionDto } from '@/generated/openapi'
import { seriesPosterUrl } from '@/api/images'
import { useImageCacheStore } from '@/stores/image-cache'
import { watchImmediate } from '@vueuse/core'
import { useDisplay } from 'vuetify'
import { useQuery } from '@pinia/colada'
import { seriesListQuery } from '@/colada/series'
import { PageRequest, type Sort } from '@/types/PageRequest'
import type ReorderHistoryScroller from '@/components/ReorderHistory/Scroller.vue'

const cacheStore = useImageCacheStore()
const display = useDisplay()

const listRef = useTemplateRef<InstanceType<typeof ReorderHistoryScroller>>('listRef')

const model = defineModel<CollectionDto>({ required: true })

const sort = computed<Sort[]>(() => {
  if (model.value.ordered) return [{ key: 'collection.number', order: 'asc' }]
  else return [{ key: 'metadata.titleSort', order: 'asc' }]
})
const { data } = useQuery(() => ({
  ...seriesListQuery({
    search: {
      condition: {
        collectionId: {
          operator: 'Is',
          value: model.value.id,
        },
      },
    },
    pageRequest: PageRequest.Unpaged(sort.value),
  }),
}))

// for fast access in the slot
const itemsMap = computed(
  () => new Map(data.value?.content?.map((it) => [it.id, { title: it.metadata.title }])),
)

// we need to use IDs from the API instead of model.seriesIds
// because model.seriesIds is not sorted for automatic ordering collections
const ids = ref(model.value.seriesIds)
watchImmediate(data, (newData) => {
  if (newData) {
    ids.value = newData.content?.map((it) => it.id) || []
  }
})
</script>
