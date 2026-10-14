<template>
  <div>
    <ItemReorderHistoryActions
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

    <ItemReorderHistoryList
      ref="listRef"
      v-model="ids"
      lines="two"
      @update:model-value="(val) => (model.bookIds = val)"
    >
      <template #item="{ id, handleClass, deleteItem }">
        <v-skeleton-loader
          v-if="!itemsMap.has(id)"
          type="list-item-avatar-two-line"
        />
        <ItemReorderHistoryListItem
          v-else
          :disable-reorder="!model.ordered"
          :handle-class="handleClass"
          :title="itemsMap.get(id)?.title"
          :subtitle="itemsMap.get(id)?.oneshot ? undefined : itemsMap.get(id)?.series"
          :avatar="bookPosterUrl(id, cacheStore.getVersion(id))"
          @delete="deleteItem()"
        />
      </template>
    </ItemReorderHistoryList>

    <v-empty-state
      v-if="model.bookIds.length === 0"
      icon="i-mdi:tray-alert"
      color="warning"
      :title="
        $formatMessage({
          description: 'Organize read list: empty state title - empty list',
          defaultMessage: 'List cannot be empty',
          id: '/4RRHN',
        })
      "
      :text="
        $formatMessage({
          description: 'Organize read list: empty state text - empty list',
          defaultMessage: 'Discard the changes or undo your last deletion',
          id: 'aeEb6w',
        })
      "
    />

    <!--  Hidden input that will trigger the form validation in ConfirmEdit  -->
    <v-input
      :rules="[() => model.bookIds.length > 0]"
      class="=d-none"
    />
  </div>
</template>

<script setup lang="ts">
import type { ReadListDto } from '@/generated/openapi'
import { bookPosterUrl } from '@/api/images'
import { useImageCacheStore } from '@/stores/image-cache'
import { useBookSiblings } from '@/composables/book/useBookSiblings'
import { watchImmediate } from '@vueuse/core'
import type ItemReorderHistoryList from '@/components/item/ReorderHistory/List.vue'
import { useDisplay } from 'vuetify'

const cacheStore = useImageCacheStore()
const display = useDisplay()

const listRef = useTemplateRef<InstanceType<typeof ItemReorderHistoryList>>('listRef')

const model = defineModel<ReadListDto>({ required: true })

const { data } = useBookSiblings(() => ({ ...model.value }), true)
// for fast access in the slot
const itemsMap = computed(
  () =>
    new Map(
      data.value?.content?.map((it) => [
        it.id,
        { title: it.metadata.title, series: it.seriesTitle, oneshot: it.oneshot },
      ]),
    ),
)

// we need to use IDs from the API instead of model.bookIds
// because model.bookIds is not sorted for automatic ordering read lists
const ids = ref(model.value.bookIds)
watchImmediate(data, (newData) => {
  if (newData) {
    ids.value = newData.content?.map((it) => it.id) || []
  }
})
</script>
