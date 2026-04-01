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
import { useReadListActions } from '@/composables/readlist/useReadListActions'

const { activator, readList } = defineProps<{
  activator: string | Element
  readList: components['schemas']['ReadListDto']
}>()

const { manageActions } = useReadListActions(readList)
</script>

<script lang="ts"></script>

<style scoped></style>
