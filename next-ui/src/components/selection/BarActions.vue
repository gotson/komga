<template>
  <template
    v-for="action in actions"
    :key="action.action"
  >
    <v-menu :disabled="!action.menuItems">
      <template #activator="{ props: menuProps }">
        <v-btn
          v-if="action.min ? selectionStore.count >= action.min : true"
          v-ktooltip:bottom="action.title"
          v-bind="{ ...action, ...(!action.menuItems ? {} : menuProps) }"
        />
      </template>

      <v-list density="comfortable">
        <v-list-item
          v-for="(entry, index) in action.menuItems"
          :key="index"
          v-bind="entry"
          :disabled="entry.min ? selectionStore.count < entry.min : false"
        />
      </v-list>
    </v-menu>
  </template>
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'
import { useEntitiesActions } from '@/composables/useEntitiesActions'
import { storeToRefs } from 'pinia'

const selectionStore = useSelectionStore()
const { selectionAsArray } = storeToRefs(selectionStore)

function afterClick() {
  selectionStore.clear()
}

const { actions } = useEntitiesActions(selectionAsArray, afterClick)
</script>
