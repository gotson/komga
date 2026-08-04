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
import { useLibraryActions } from '@/composables/library/useLibraryActions'
import { createOrderCompareFn } from '@/functions/sort'

import { LibraryAction } from '@/types/action/library'
import type { LibraryDto } from '@/generated/openapi'
const isShown = defineModel<boolean>({ default: false })

const { library } = defineProps<{
  activator: string | Element
  library: LibraryDto
}>()

const main = [LibraryAction.Scan] as LibraryAction[]
const management = [
  LibraryAction.Edit,
  LibraryAction.ScanDeep,
  LibraryAction.RefreshMetadata,
  LibraryAction.EmptyTrash,
  LibraryAction.Analyze,
  LibraryAction.Delete,
] as LibraryAction[]

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
