<template>
  <v-navigation-drawer v-model="appStore.drawer">
    <v-slide-x-transition hide-on-leave>
      <ReorderLibraries v-if="appStore.reorderLibraries" />
    </v-slide-x-transition>

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
