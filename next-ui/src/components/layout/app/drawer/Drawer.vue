<template>
  <!--  disable-route-watcher is needed, else the drawer closes when the route query params are updated when the filters change -->
  <v-navigation-drawer
    v-model="appStore.drawer"
    disable-route-watcher
  >
    <v-slide-x-transition hide-on-leave>
      <ReorderLibraries v-if="appStore.reorderLibraries" />
    </v-slide-x-transition>

    <template #prepend>
      <LayoutAppDrawerHeader />
    </template>

    <LayoutAppDrawerMenu v-if="!appStore.reorderLibraries" />

    <template #append>
      <LayoutAppDrawerFooter />
    </template>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import ReorderLibraries from '@/components/layout/app/drawer/ReorderLibraries.vue'

const appStore = useAppStore()
</script>
