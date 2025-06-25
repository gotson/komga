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
    prepend-icon="i-mdi:clock-time-four-outline"
  />
</template>

<script setup lang="ts"></script>
