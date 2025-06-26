<template>
  <v-navigation-drawer v-model="appStore.drawer">
    <LayoutAppDrawerMenu />

    <template #append>
      <LayoutAppDrawerFooter />
    </template>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>
