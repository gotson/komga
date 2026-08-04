<template>
  <v-chip
    v-if="count"
    rounded
    >{{ count }}</v-chip
  >
</template>

<script setup lang="ts">
const {} = defineProps<{
  count?: number
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
