<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
    :sheet-title="book.metadata.title"
  />
</template>

<script setup lang="ts">
import { useBookActions } from '@/composables/book/useBookActions'
import { createOrderCompareFn } from '@/functions/sort'
import { ActionName } from '@/types/action/action'
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'
import { useSelectAction } from '@/composables/selection'
import type { BookDto } from '@/generated/openapi'
const isShown = defineModel<boolean>({ default: false })

const { book, excludeActions = [] } = defineProps<{
  activator: string | Element
  book: BookDto
  excludeActions?: ActionName[]
}>()

const main = [
  ActionName.OpenReader,
  ActionName.OpenReaderIncognito,
  ActionName.AddToCollection,
  ActionName.AddToReadList,
  ActionName.MarkRead,
  ActionName.MarkUnread,
  ActionName.Download,
] as ActionName[]
const management = [
  ActionName.EditBook,
  ActionName.RefreshMetadata,
  ActionName.Analyze,
  ActionName.Delete,
] as ActionName[]

function afterClick() {
  isShown.value = false
}

const selectionStore = useSelectionStore()
const { isTouchPrimary } = usePrimaryInput()
const { selectAction } = useSelectAction(() => book, afterClick)

const { actions } = useBookActions(() => book, afterClick)
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
