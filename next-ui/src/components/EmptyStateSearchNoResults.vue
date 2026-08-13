<template>
  <v-empty-state
    icon="i-mdi:magnify"
    :title="
      $formatMessage({
        description: 'Search empty state: title',
        defaultMessage: 'No results',
        id: 'y1Fk/a',
      })
    "
    :text="
      $formatMessage({
        description: 'Search empty state: text',
        defaultMessage: 'Try to adjust the search',
        id: 'Ih4MH9',
      })
    "
    :action-text="
      $formatMessage({
        description: 'Search empty state: button',
        defaultMessage: 'Clear search',
        id: 'UPOAPc',
      })
    "
    @click:action="reset()"
  />
</template>

<script setup lang="ts">
const emit = defineEmits<{
  reset: []
}>()

function reset() {
  emit('reset')
}
</script>
