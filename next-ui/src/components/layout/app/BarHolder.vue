<template>
  <LayoutAppBar v-if="selectionStore.isEmpty || isTouchPrimary" />
  <v-fade-transition>
    <SelectionBar v-if="!selectionStore.isEmpty && !isTouchPrimary" />
  </v-fade-transition>

  <SelectionFooter v-if="!selectionStore.isEmpty && isTouchPrimary" />
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'

const selectionStore = useSelectionStore()
const { isTouchPrimary } = usePrimaryInput()
</script>
