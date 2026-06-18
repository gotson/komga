<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsManagement"
    :activator="activator"
    :sheet-title="collection.name"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useCollectionActions } from '@/composables/collection/useCollectionActions'
import { createOrderCompareFn } from '@/functions/sort'
import { ActionName } from '@/types/action/action'
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'
import { useSelectAction } from '@/composables/selection'
const isShown = defineModel<boolean>({ default: false })

const { collection, excludeActions = [] } = defineProps<{
  activator: string | Element
  collection: components['schemas']['CollectionDto']
  excludeActions?: ActionName[]
}>()

const management = [ActionName.EDIT_COLLECTION, ActionName.DELETE]

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
