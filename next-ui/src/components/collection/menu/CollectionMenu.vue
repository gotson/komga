<template>
  <v-menu :activator="activator">
    <v-list density="compact">
      <v-list-item
        v-for="(action, i) in manageActions"
        :key="i"
        v-bind="action"
      />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useCollectionActions } from '@/composables/collection/useCollectionActions'

const props = defineProps<{
  activator: string | Element
  collection: components['schemas']['CollectionDto']
}>()

const { manageActions } = useCollectionActions(() => props.collection)
</script>

<script lang="ts"></script>

<style scoped></style>
