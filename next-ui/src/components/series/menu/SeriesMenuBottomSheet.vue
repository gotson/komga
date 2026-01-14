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
import { useSeriesActions } from '@/composables/series'

const isShown = defineModel<boolean>({ default: false })

const { series } = defineProps<{
  series: components['schemas']['SeriesDto']
}>()

function afterClick() {
  isShown.value = false
}
const { actions, manageActions } = useSeriesActions(series, afterClick)
</script>

<script lang="ts"></script>

<style scoped></style>
