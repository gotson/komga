<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4 h-100 h-sm-auto"
  >
    <fragment-history-table />
  </v-container>
</template>

<script lang="ts" setup></script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
