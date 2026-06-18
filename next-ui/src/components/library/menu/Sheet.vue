<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
    :sheet-title="library.name"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useLibraryActions } from '@/composables/library/useLibraryActions'
import { createOrderCompareFn } from '@/functions/sort'

import { LibraryAction } from '@/types/action/library'
const isShown = defineModel<boolean>({ default: false })

const { library } = defineProps<{
  activator: string | Element
  library: components['schemas']['LibraryDto']
}>()

const main = [LibraryAction.SCAN]
const management = [
  LibraryAction.EDIT,
  LibraryAction.SCAN_DEEP,
  LibraryAction.REFRESH_METADATA,
  LibraryAction.EMPTY_TRASH,
  LibraryAction.ANALYZE,
  LibraryAction.DELETE,
]

function afterClick() {
  isShown.value = false
}
const { actions } = useLibraryActions(() => library, afterClick)
const actionsDefault = computed(() =>
  actions.value
    .filter((it) => main.includes(it.action))
    .toSorted(createOrderCompareFn(main, (it) => it.action.toString())),
)
const actionsManagement = computed(() =>
  actions.value
    .filter((it) => management.includes(it.action))
    .toSorted(createOrderCompareFn(management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
