<template>
  <v-app-bar
    elevation="1"
    color="primary"
  >
    <template #prepend>
      <v-app-bar-nav-icon
        icon="i-mdi:close"
        @click="selectionStore.clear()"
      />
      <v-btn
        v-for="action in selectionStore.contextualActions"
        :key="action.text"
        v-tooltip:bottom="action.text"
        :icon="action.icon"
        @click="action.callback()"
      />
    </template>

    <v-app-bar-title>
      {{
        $formatMessage(
          {
            description: 'Selection bar: count of items',
            defaultMessage: `{count, plural,
one {# selected}
other {# selected}
}`,
            id: 'tCm6kO',
          },
          {
            count: selectionStore.count,
          },
        )
      }}
    </v-app-bar-title>

    <template #append>
      <SelectionBarActions />
    </template>
  </v-app-bar>
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'

const selectionStore = useSelectionStore()
</script>

<script lang="ts"></script>

<style scoped></style>
