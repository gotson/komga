<template>
  <v-btn-toggle
    v-model="model"
    variant="outlined"
    v-bind="props"
  >
    <v-btn
      v-for="b in buttons"
      :key="b.value"
      v-tooltip:bottom="!text ? b.text : ''"
      :icon="!text ? b.icon : undefined"
      :prepend-icon="text && icons ? b.icon : undefined"
      size="small"
      :value="b.value"
      ><template
        v-if="text"
        #default
        >{{ b.text }}</template
      ></v-btn
    >
  </v-btn-toggle>
</template>

<script setup lang="ts">
import type { AnyAll } from '@/types/filter'
import { useIntl } from 'vue-intl'

/**
 * Selection mode.
 */
const model = defineModel<AnyAll>({ default: 'anyOf' })

const {
  text = false,
  icons = true,
  props,
} = defineProps<{
  text?: boolean
  icons?: boolean
  props?: object
}>()

const intl = useIntl()

const buttons = [
  {
    text: intl.formatMessage({
      description: 'Any/All button: anyOf value',
      defaultMessage: 'Any of',
      id: 'RMHfEo',
    }),
    icon: 'i-mdi:filter-outline',
    value: 'anyOf',
  },
  {
    text: intl.formatMessage({
      description: 'Any/All button: allOf value',
      defaultMessage: 'All of',
      id: '0UFr4y',
    }),
    icon: 'i-mdi:filter-multiple-outline',
    value: 'allOf',
  },
]
</script>

<script lang="ts"></script>

<style scoped></style>
