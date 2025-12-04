<template>
  <v-list>
    <v-list-item variant="tonal">
      <v-list-item-title class="text-uppercase">{{
        $formatMessage({
          description: 'Reorder library drawer: title',
          defaultMessage: 'reorder',
          id: 'g+OQSw',
        })
      }}</v-list-item-title>
      <template #append>
        <v-icon-btn
          icon="i-mdi:close"
          variant="text"
          :aria-label="
            $formatMessage({
              description: 'Reorder library drawer: aria label',
              defaultMessage: 'close',
              id: 'Ept33T',
            })
          "
        />
      </template>
    </v-list-item>

    <v-list-subheader
      :title="
        $formatMessage({
          description: 'Reorder library drawer: pinned section header',
          defaultMessage: 'Pinned',
          id: 'OZSDCE',
        })
      "
    />

    <draggable
      v-model="localPinned"
      v-bind="draggableConfig"
    >
      <template #[`header`]>
        <v-list-item
          v-if="localPinned.length == 0"
          :title="
            $formatMessage({
              description: 'Reorder library drawer: placeholder if no libraries are pinned',
              defaultMessage: 'Drag here to pin a library',
              id: '0MePSx',
            })
          "
          class="text-grey"
        />
      </template>
      <template #[`item`]="{ element: library }">
        <v-list-item
          :title="library.name"
          prepend-icon="i-mdi:drag-horizontal"
          class="cursor-grab"
        />
      </template>
    </draggable>

    <v-divider />

    <v-list-subheader
      :title="
        $formatMessage({
          description: 'Reorder library drawer: unpinned section header',
          defaultMessage: 'Unpinned',
          id: 'sj2JGj',
        })
      "
    />

    <draggable
      v-model="localUnpinned"
      v-bind="draggableConfig"
    >
      <template #[`header`]>
        <v-list-item
          v-if="localUnpinned.length == 0"
          :title="
            $formatMessage({
              description: 'Reorder library drawer: placeholder if no libraries are unpinned',
              defaultMessage: 'Drag here to unpin a library',
              id: 'H+LXXE',
            })
          "
          class="text-grey"
        />
      </template>
      <template #[`item`]="{ element: library }">
        <v-list-item
          :title="library.name"
          prepend-icon="i-mdi:drag-horizontal"
          class="cursor-grab"
        />
      </template>
    </draggable>
  </v-list>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import { useLibraries } from '@/colada/libraries'
import type { components } from '@/generated/openapi/komga'
import { CLIENT_SETTING_USER, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'
import { useUpdateClientSettingsUser } from '@/colada/client-settings'

const { unpinned, pinned, refresh } = useLibraries()
const localPinned = ref<components['schemas']['LibraryDto'][]>([])
const localUnpinned = ref<components['schemas']['LibraryDto'][]>([])

// one time copy to local refs
void refresh().then(() => {
  localPinned.value = pinned.value
  localUnpinned.value = unpinned.value
})

const draggableConfig = {
  group: 'libs',
  itemKey: 'id',
  ghostClass: 'ghost',
  chosenClass: 'chosen',
  animation: 150,
}

const { mutate } = useUpdateClientSettingsUser()

watch([localPinned, localUnpinned], ([newPinned, newUnpinned]) => {
  const newSettings: Record<string, ClientSettingUserLibrary> = {}
  newPinned.forEach((it, index) => (newSettings[it.id] = { order: index, unpinned: false }))
  newUnpinned.forEach(
    (it, index) => (newSettings[it.id] = { order: newPinned.length + index, unpinned: true }),
  )
  mutate({ [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: { value: JSON.stringify(newSettings) } })
})
</script>

<style lang="scss">
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
