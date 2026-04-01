<template>
  <v-chip
    v-if="count > 0"
    color="primary"
    rounded
    closable
    variant="elevated"
    size="small"
    @click:close="emit('clear')"
  >
    {{ count }}
  </v-chip>
</template>

<script setup lang="ts">
const {} = defineProps<{
  count: number
}>()

const emit = defineEmits<{
  clear: []
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
