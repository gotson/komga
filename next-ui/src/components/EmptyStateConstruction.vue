<template>
  <v-empty-state
    icon="i-mdi:construction"
    color="warning"
    :title="
      $formatMessage({
        description: 'Under construction empty state: title',
        defaultMessage: 'Under construction',
        id: 'b4J0RU',
      })
    "
    :text="
      $formatMessage({
        description: 'Under construction empty state: text',
        defaultMessage: 'This page is under construction.',
        id: 'RR+XGR',
      })
    "
  />
</template>

<script setup lang="ts"></script>
