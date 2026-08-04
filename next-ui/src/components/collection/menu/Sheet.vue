<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsManagement"
    :activator="activator"
    :sheet-title="collection.name"
  />
</template>

<script setup lang="ts">
import { useCollectionActions } from '@/composables/collection/useCollectionActions'
import { createOrderCompareFn } from '@/functions/sort'
import { ActionName } from '@/types/action/action'
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'
import { useSelectAction } from '@/composables/selection'
import type { CollectionDto } from '@/generated/openapi'
const isShown = defineModel<boolean>({ default: false })

const { collection, excludeActions = [] } = defineProps<{
  activator: string | Element
  collection: CollectionDto
  excludeActions?: ActionName[]
}>()

const management = [ActionName.EditCollection, ActionName.Delete] as ActionName[]

function afterClick() {
  isShown.value = false
}

const selectionStore = useSelectionStore()
const { isTouchPrimary } = usePrimaryInput()
const { selectAction } = useSelectAction(() => collection, afterClick)

const { actions } = useCollectionActions(() => collection, afterClick)

const actionsManagement = computed(() => [
  ...(selectionStore.isEmpty && isTouchPrimary.value ? [selectAction.value] : []),
  ...actions.value
    .filter((it) => management.includes(it.action) && !excludeActions.includes(it.action))
    .toSorted(createOrderCompareFn(management, (it) => it.action.toString())),
])
</script>

<script lang="ts"></script>

<style scoped></style>
