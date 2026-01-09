<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        :icon="allModes[currentMode].icon"
        :aria-label="
          $formatMessage({
            description: 'Presentation selector button: aria-label',
            defaultMessage: 'presentation selector',
            id: 'sUl0GP',
          })
        "
      />
    </template>

    <v-list
      :selected="[currentMode]"
      color="primary"
    >
      <v-list-item
        v-for="mode in modes"
        :key="mode"
        :title="allModes[mode].title"
        :value="mode"
        :prepend-icon="allModes[mode].icon"
        @click="currentMode = mode"
      />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import type { PresentationMode } from '@/types/libraries'

const intl = useIntl()

const allModes: Record<PresentationMode, { title: string; icon: string }> = {
  grid: {
    title: intl.formatMessage({
      description: 'Presentation mode: grid',
      defaultMessage: 'Grid',
      id: 'Hv7lqq',
    }),
    icon: 'i-mdi:view-grid',
  },
  list: {
    title: intl.formatMessage({
      description: 'Presentation mode: list',
      defaultMessage: 'List',
      id: 'JNBONk',
    }),
    icon: 'i-mdi:view-list',
  },
  table: {
    title: intl.formatMessage({
      description: 'Presentation mode: table',
      defaultMessage: 'Table',
      id: 'efspoY',
    }),
    icon: 'i-mdi:table',
  },
}

const currentMode = defineModel<PresentationMode>({ required: true, default: 'grid' })

const { modes } = defineProps<{
  modes: PresentationMode[]
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
