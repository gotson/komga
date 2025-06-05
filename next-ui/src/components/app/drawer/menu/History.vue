<template>
  <v-list-item
    to="/history"
    :title="
      $formatMessage({
        description: 'Drawer menu for History',
        defaultMessage: 'History',
        id: 'l/To3S',
      })
    "
    :prepend-icon="mdiClockTimeFourOutline"
  />
</template>

<script setup lang="ts">
import mdiClockTimeFourOutline from '~icons/mdi/clock-time-four-outline'
</script>
