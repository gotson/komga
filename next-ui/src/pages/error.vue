<template>
  <v-empty-state
    icon="i-mdi:connection"
    :title="
      $formatMessage({
        description: 'Server unreachable error view: error message title',
        defaultMessage: 'Server unreachable',
        id: 'n0TnQ2',
      })
    "
    :text="
      $formatMessage({
        description: 'Server unreachable error view: error message subtitle',
        defaultMessage: 'Check that your Komga server is running',
        id: 'PTr5Wc',
      })
    "
  />
</template>

<script lang="ts" setup></script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
