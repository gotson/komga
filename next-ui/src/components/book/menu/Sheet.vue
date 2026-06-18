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
import type { components } from '@/generated/openapi/komga'
import { useBookActions } from '@/composables/book/useBookActions'
import { createOrderCompareFn } from '@/functions/sort'
import { ActionName } from '@/types/action/action'
import { useSelectionStore } from '@/stores/selection'
import { usePrimaryInput } from '@/composables/device'
import { useSelectAction } from '@/composables/selection'
const isShown = defineModel<boolean>({ default: false })

const { book, excludeActions = [] } = defineProps<{
  activator: string | Element
  book: components['schemas']['BookDto']
  excludeActions?: ActionName[]
}>()

const main = [
  ActionName.OPEN_READER,
  ActionName.OPEN_READER_INCOGNITO,
  ActionName.ADD_TO_COLLECTION,
  ActionName.ADD_TO_READLIST,
  ActionName.MARK_READ,
  ActionName.MARK_UNREAD,
  ActionName.DOWNLOAD,
]
const management = [
  ActionName.EDIT_BOOK,
  ActionName.REFRESH_METADATA,
  ActionName.ANALYZE,
  ActionName.DELETE,
]

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
