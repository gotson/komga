<template>
  <v-bottom-sheet
    v-model="isShown"
    inset
  >
    <v-list>
      <template v-if="title">
        <v-list-item :title="title" />
        <v-divider />
      </template>
      <v-list-item
        v-for="(action, i) in actions"
        :key="i"
        v-bind="action"
      />

      <v-divider v-if="actions.length > 0 && manageActions.length > 0" />

      <v-list-item
        v-for="(action, i) in manageActions"
        :key="i"
        v-bind="action"
      />
    </v-list>
  </v-bottom-sheet>
</template>

<script setup lang="ts">
import type { Action } from '@/types/action/action'

const isShown = defineModel<boolean>({ default: false })

const { actions = [], manageActions = [] } = defineProps<{
  actions?: Action<unknown>[]
  manageActions?: Action<unknown>[]
  title?: string
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
