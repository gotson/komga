<template>
  <LayoutAppBar v-if="selectionStore.isEmpty" />
  <v-fade-transition>
    <SelectionBar v-if="!selectionStore.isEmpty" />
  </v-fade-transition>
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'

const selectionStore = useSelectionStore()
</script>
