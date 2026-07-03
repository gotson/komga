<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
  />
</template>

<script setup lang="ts">
import { createOrderCompareFn } from '@/functions/sort'
import { useLibrariesActions } from '@/composables/library/useLibrariesActions'
import { LibrariesAction } from '@/types/action/libraries'
import { ActionName } from '@/types/action/action'
const isShown = defineModel<boolean>({ default: false })

defineProps<{
  activator: string | Element
}>()

const main = [LibrariesAction.Reorder] as LibrariesAction[]
const management = [LibrariesAction.ScanAll, LibrariesAction.EmptyTrashAll] as LibrariesAction[]

function afterClick() {
  isShown.value = false
}
const { actions } = useLibrariesActions(afterClick)
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
