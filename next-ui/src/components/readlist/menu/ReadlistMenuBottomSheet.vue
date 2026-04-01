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
import { useReadListActions } from '@/composables/readlist/useReadListActions'

const isShown = defineModel<boolean>({ default: false })

const { readList } = defineProps<{
  readList: components['schemas']['ReadListDto']
}>()

function afterClick() {
  isShown.value = false
}
const { manageActions } = useReadListActions(readList, afterClick)
</script>

<script lang="ts"></script>

<style scoped></style>
