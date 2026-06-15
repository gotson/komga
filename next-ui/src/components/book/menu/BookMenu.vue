<template>
  <v-menu :activator="activator">
    <v-list density="compact">
      <v-list-item
        v-for="(action, i) in actionsDefault"
        :key="i"
        v-bind="action"
      />

      <v-list-item
        v-if="actionsManagement.length > 0"
        :title="
          $formatMessage({
            description: 'Book menu: manage',
            defaultMessage: 'Manage book',
            id: 'E8yw5g',
          })
        "
        append-icon="i-mdi:menu-right"
      >
        <v-menu
          activator="parent"
          open-on-click
          open-on-hover
          location="end"
          submenu
        >
          <v-list density="compact">
            <v-list-item
              v-for="(action, i) in actionsManagement"
              :key="i"
              v-bind="action"
            />
          </v-list>
        </v-menu>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useBookActions } from '@/composables/book/useBookActions'
import { createOrderCompareFn } from '@/functions/sort'
import { BookAction, bookActionGroups } from '@/types/book'

const {
  activator,
  book,
  excludeActions = [],
} = defineProps<{
  activator: string | Element
  book: components['schemas']['BookDto']
  excludeActions?: BookAction[]
}>()

const { actions } = useBookActions(() => book)
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
