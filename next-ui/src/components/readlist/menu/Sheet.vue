<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
    :sheet-title="readList.name"
  />
</template>

<script setup lang="ts">
import { useReadListActions } from '@/composables/readlist/useReadListActions'
import { createOrderCompareFn } from '@/functions/sort'
import { ActionName } from '@/types/action/action'
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'
import { useSelectAction } from '@/composables/selection'
import type { ReadListDto } from '@/generated/openapi'

const isShown = defineModel<boolean>({ default: false })

const { readList, excludeActions = [] } = defineProps<{
  activator: string | Element
  readList: ReadListDto
  excludeActions?: ActionName[]
}>()

const main = [ActionName.DOWNLOAD]
const management = [ActionName.EDIT_READLIST, ActionName.DELETE]

function afterClick() {
  isShown.value = false
}

const selectionStore = useSelectionStore()
const { isTouchPrimary } = usePrimaryInput()
const { selectAction } = useSelectAction(() => readList, afterClick)

const { actions } = useReadListActions(() => readList, afterClick)
const actionsDefault = computed(() => [
  ...(selectionStore.isEmpty && isTouchPrimary.value ? [selectAction.value] : []),
  ...actions.value
    .filter((it) => main.includes(it.action) && !excludeActions.includes(it.action))
    .toSorted(createOrderCompareFn(main, (it) => it.action.toString())),
])
const actionsManagement = computed(() =>
  actions.value
    .filter((it) => management.includes(it.action) && !excludeActions.includes(it.action))
    .toSorted(createOrderCompareFn(management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
