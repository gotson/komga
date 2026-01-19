<template>
  <v-bottom-sheet
    v-model="isShown"
    inset
  >
    <v-list>
      <v-list-item
        v-for="(action, i) in actions"
        :key="i"
        v-bind="action"
      />

      <v-divider v-if="manageActions.length > 0" />

      <v-list-item
        v-for="(action, i) in manageActions"
        :key="i"
        v-bind="action"
      />
    </v-list>
  </v-bottom-sheet>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useBookActions } from '@/composables/book/useBookActions'

const isShown = defineModel<boolean>({ default: false })

const { book } = defineProps<{
  book: components['schemas']['BookDto']
}>()

function afterClick() {
  isShown.value = false
}
const { actions, manageActions } = useBookActions(book, afterClick)
</script>

<script lang="ts"></script>

<style scoped></style>
