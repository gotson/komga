<template>
  <v-navigation-drawer v-model="appStore.drawer">
    <AppDrawerMenu />

    <template #append>
      <AppDrawerFooter />
    </template>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>
