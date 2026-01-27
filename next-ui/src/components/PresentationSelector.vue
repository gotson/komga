<template>
  <v-btn
    :id="id"
    v-tooltip:bottom="allModes[currentMode].title"
    :icon="allModes[currentMode].icon"
    :aria-label="
      $formatMessage({
        description: 'Presentation selector button: aria-label',
        defaultMessage: 'presentation selector',
        id: 'sUl0GP',
      })
    "
    @click="toggle ? cycleMode() : undefined"
  />

  <v-menu
    :activator="`#${id}`"
    :disabled="toggle"
  >
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
const id = useId()

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

const { modes, toggle = false } = defineProps<{
  modes: PresentationMode[]
  toggle?: boolean
}>()

function cycleMode() {
  const index = modes.findIndex((x) => x === currentMode.value)
  const newIndex = (index + 1) % modes.length
  currentMode.value = modes[newIndex]!
}
</script>

<script lang="ts"></script>

<style scoped></style>
