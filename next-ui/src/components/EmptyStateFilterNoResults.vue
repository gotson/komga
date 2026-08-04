<template>
  <v-empty-state
    icon="i-mdi:filter-off"
    :title="
      $formatMessage({
        description: 'Filter empty state: title',
        defaultMessage: 'No results',
        id: 'Uuu6fQ',
      })
    "
    :text="
      $formatMessage({
        description: 'Filter empty state: text',
        defaultMessage: 'Try to adjust the filters',
        id: 'HZ/e6n',
      })
    "
    :action-text="
      $formatMessage({
        description: 'Filter empty state: button',
        defaultMessage: 'Clear filters',
        id: '4yDvPA',
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
