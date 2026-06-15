<template>
  <v-bottom-sheet
    v-model="isShown"
    inset
  >
    <v-list>
      <v-list-item
        v-for="(action, i) in actionsDefault"
        :key="i"
        v-bind="action"
      />

      <v-divider v-if="actionsManagement.length > 0" />

      <v-list-item
        v-for="(action, i) in actionsManagement"
        :key="i"
        v-bind="action"
      />
    </v-list>
  </v-bottom-sheet>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useBookActions } from '@/composables/book/useBookActions'
import { BookAction, bookActionGroups } from '@/types/book'
import { createOrderCompareFn } from '@/functions/sort'

const isShown = defineModel<boolean>({ default: false })

const { book, excludeActions = [] } = defineProps<{
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
