<template>
  <v-btn-toggle
    v-model="paging"
    mandatory
    variant="outlined"
    density="comfortable"
    color="primary"
  >
    <v-btn
      icon="i-mdi:infinity"
      value="scroll"
    />
    <v-btn
      icon="i-mdi:view-grid"
      value="paged"
    />
  </v-btn-toggle>
</template>

<script setup lang="ts">
import type { Paging } from '@/types/page'

const paging = defineModel<Paging>({ required: true })
</script>

<script lang="ts"></script>

<style scoped></style>
