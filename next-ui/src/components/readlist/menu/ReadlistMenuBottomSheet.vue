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
import type { components } from '@/generated/openapi/komga'
import { useReadListActions } from '@/composables/readlist/useReadListActions'

const isShown = defineModel<boolean>({ default: false })

const props = defineProps<{
  readList: components['schemas']['ReadListDto']
}>()

function afterClick() {
  isShown.value = false
}
const { actions, manageActions } = useReadListActions(() => props.readList, afterClick)
</script>

<script lang="ts"></script>

<style scoped></style>
