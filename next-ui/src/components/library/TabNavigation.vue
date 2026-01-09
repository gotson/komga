<template>
  <v-app-bar>
    <v-tabs
      :items="routes"
      center-active
      grow
    >
      <template #tab="{ item: route }">
        <v-tab
          :text="route.title"
          :to="route.to"
        />
      </template>
    </v-tabs>
  </v-app-bar>
</template>

<script setup lang="ts">
import type { Route } from '@/types/route'

const { routes } = defineProps<{
  routes: Route[]
}>()
</script>

<style scoped></style>
