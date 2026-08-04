<template>
  <v-app-bar v-if="!dismiss && appStore.sseUnavailable">
    <v-alert
      icon="i-mdi:lan-disconnect"
      :text="
        $formatMessage({
          description: 'Banner: realtime connection could not be established ',
          defaultMessage:
            'Realtime connection to the server is not available. UI elements may not update correctly.',
          id: 'sbwSuO',
        })
      "
      closable
      color="warning"
      @click:close="dismiss = true"
    />
  </v-app-bar>

  <LayoutAppBar v-if="selectionStore.isEmpty || isTouchPrimary" />
  <v-fade-transition>
    <SelectionBar v-if="!selectionStore.isEmpty && !isTouchPrimary" />
  </v-fade-transition>

  <SelectionFooter v-if="!selectionStore.isEmpty && isTouchPrimary" />
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'
import { useAppStore } from '@/stores/app'

const selectionStore = useSelectionStore()
const { isTouchPrimary } = usePrimaryInput()

const appStore = useAppStore()
const dismiss = ref(false)
</script>
