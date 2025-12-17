<template>
  <v-bottom-navigation grow>
    <v-btn
      v-for="(route, i) in routes"
      :key="i"
      :to="route.to"
    >
      <v-icon
        v-if="route.icon"
        :icon="route.icon"
      />
      <span>{{ route.title }}</span>
    </v-btn>
  </v-bottom-navigation>
</template>

<script setup lang="ts">
import type { Route } from '@/types/route'

const { routes } = defineProps<{
  routes: Route[]
}>()
</script>

<style scoped></style>
