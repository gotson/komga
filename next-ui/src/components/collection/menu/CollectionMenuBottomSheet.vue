<template>
  <v-bottom-sheet
    v-model="isShown"
    inset
  >
    <v-list>
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
import { useCollectionActions } from '@/composables/collection/useCollectionActions'

const isShown = defineModel<boolean>({ default: false })

const { collection } = defineProps<{
  collection: components['schemas']['CollectionDto']
}>()

function afterClick() {
  isShown.value = false
}
const { manageActions } = useCollectionActions(collection, afterClick)
</script>

<script lang="ts"></script>

<style scoped></style>
