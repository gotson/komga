<template>
  <v-btn
    v-for="action in actions"
    :key="action.action"
    v-tooltip:bottom="action.title"
    v-bind="action"
  />
</template>

<script setup lang="ts">
import { useSelectionStore } from '@/stores/selection'
import { useEntitiesActions } from '@/composables/useEntitiesActions'
import { storeToRefs } from 'pinia'

const selectionStore = useSelectionStore()
const { selection } = storeToRefs(selectionStore)

function afterClick() {
  selectionStore.clear()
}

const { actions } = useEntitiesActions(selection, afterClick)
</script>

<script lang="ts"></script>

<style scoped></style>
