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
          v-tooltip:bottom="closeMessage"
          icon="i-mdi:close"
          variant="text"
          :aria-label="closeMessage"
          @click="appStore.reorderLibraries = false"
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
import { useAppStore } from '@/stores/app'
import { useIntl } from 'vue-intl'

const intl = useIntl()
const appStore = useAppStore()

const { unpinned, pinned, refresh } = useLibraries()
const { mutate } = useUpdateClientSettingsUser()

const localPinned = ref<components['schemas']['LibraryDto'][]>([])
const localUnpinned = ref<components['schemas']['LibraryDto'][]>([])

// one time copy to local refs
void refresh().then(() => {
  localPinned.value = pinned.value
  localUnpinned.value = unpinned.value

  // start watching after initial values are set
  watch([localPinned, localUnpinned], ([newPinned, newUnpinned]) => {
    const newSettings: Record<string, ClientSettingUserLibrary> = {}
    newPinned.forEach((it, index) => (newSettings[it.id] = { order: index, unpinned: false }))
    newUnpinned.forEach(
      (it, index) => (newSettings[it.id] = { order: newPinned.length + index, unpinned: true }),
    )
    mutate({ [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: { value: JSON.stringify(newSettings) } })
  })
})

const draggableConfig = {
  group: 'libs',
  itemKey: 'id',
  ghostClass: 'ghost',
  chosenClass: 'chosen',
  animation: 150,
}

const closeMessage = intl.formatMessage({
  description: 'Reorder library drawer: close button',
  defaultMessage: 'Close',
  id: 'lPZ5hy',
})
</script>

<style lang="scss">
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
