<template>
  <v-expansion-panel :value="value ?? id">
    <template #title>
      <span>{{ title }}</span>
      <v-chip
        v-if="count > 0"
        color="primary"
        rounded
        closable
        class="ms-2"
        variant="elevated"
        size="small"
        @click:close="emit('clear')"
      >
        {{ count }}
      </v-chip>
    </template>
    <template #text>
      <slot></slot>
    </template>
  </v-expansion-panel>
</template>

<script setup lang="ts">
const id = useId()

const { value, count = 0 } = defineProps<{
  /**
   * Controls the opened/closed state of content.
   * Defaults to a unique ID if not set.
   */
  value?: string
  /**
   * Count of active filters.
   */
  count?: number
  /**
   * Component's title.
   */
  title?: string
}>()

const emit = defineEmits<{
  /**
   * Event that is emitted when the chip's close button is clicked.
   */
  clear: []
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
