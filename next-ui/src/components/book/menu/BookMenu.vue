<template>
  <v-menu :activator="activator">
    <v-list density="compact">
      <v-list-item
        v-for="(action, i) in actions"
        :key="i"
        v-bind="action"
      />

      <v-list-item
        v-if="manageActions.length > 0"
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
              v-for="(action, i) in manageActions"
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

const { activator, book } = defineProps<{
  activator: string | Element
  book: components['schemas']['BookDto']
}>()

const { actions, manageActions } = useBookActions(book)
</script>

<script lang="ts"></script>

<style scoped></style>
