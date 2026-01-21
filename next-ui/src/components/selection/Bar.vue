<template>
  <v-app-bar
    elevation="2"
    color="surface-light"
  >
    <template #prepend>
      <v-app-bar-nav-icon
        icon="i-mdi:close"
        @click="selectionStore.clear()"
      />
    </template>

    <v-app-bar-title>
      {{
        $formatMessage(
          {
            description: 'Selection bar: count of items',
            defaultMessage: '{count} selected',
            id: 'lqyIjk',
          },
          {
            count: selectionStore.count,
          },
        )
      }}
    </v-app-bar-title>
  </v-app-bar>
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'

const selectionStore = useSelectionStore()
</script>

<script lang="ts"></script>

<style scoped></style>
