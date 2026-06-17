<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useBookActions } from '@/composables/book/useBookActions'
import { createOrderCompareFn } from '@/functions/sort'
import { BookAction, bookActionGroups } from '@types/action/book'
const isShown = defineModel<boolean>({ default: false })

const { book, excludeActions = [] } = defineProps<{
  activator: string | Element
  book: components['schemas']['BookDto']
  excludeActions?: BookAction[]
}>()

function afterClick() {
  isShown.value = false
}
const { actions } = useBookActions(() => book, afterClick)
const actionsDefault = computed(() =>
  actions.value
    .filter(
      (it) => bookActionGroups.default.includes(it.action) && !excludeActions.includes(it.action),
    )
    .toSorted(createOrderCompareFn(bookActionGroups.default, (it) => it.action.toString())),
)
const actionsManagement = computed(() =>
  actions.value
    .filter(
      (it) =>
        bookActionGroups.management.includes(it.action) && !excludeActions.includes(it.action),
    )
    .toSorted(createOrderCompareFn(bookActionGroups.management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
