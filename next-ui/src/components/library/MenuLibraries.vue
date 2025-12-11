<template>
  <v-menu
    :activator="activatorId"
    location="end"
  >
    <v-list density="compact">
      <v-list-item
        v-for="(action, i) in actions"
        :key="i"
        v-bind="action"
      />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { useAppStore } from '@/stores/app'

const { activatorId } = defineProps<{
  activatorId: string
}>()

const intl = useIntl()
const appStore = useAppStore()

const actions = [
  {
    title: intl.formatMessage({
      description: 'Libraries menu: reorder',
      defaultMessage: 'Reorder',
      id: 'EcIbyl',
    }),
    onClick: () => (appStore.reorderLibraries = true),
  },
]
</script>

<script lang="ts"></script>

<style scoped></style>
