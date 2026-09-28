<template>
  <v-icon-btn
    v-ktooltip:bottom="
      $formatMessage({
        description: 'Sort panel: restore icon tootlip',
        defaultMessage: 'Restore to default',
        id: 'qlImvg',
      })
    "
    icon="i-mdi:restore"
    size="small"
    variant="elevated"
    color="primary"
    @click="emit('restore')"
  />
</template>

<script setup lang="ts">
const emit = defineEmits<{
  restore: []
}>()
</script>
