<template>
  <v-tabs :items="routes">
    <template #tab="{ item: route }">
      <v-tab
        :text="route.title"
        :to="route.to"
      />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import type { Route } from '@/types/route'

const { routes } = defineProps<{
  routes: Route[]
}>()
</script>

<style scoped></style>
