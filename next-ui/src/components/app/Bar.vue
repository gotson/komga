<template>
  <v-app-bar elevation="2">
    <template #prepend>
      <v-app-bar-nav-icon @click="appStore.drawer = !appStore.drawer" />
    </template>
    <v-app-bar-title>
      <RouterLink to="/">
        <v-avatar start>
          <v-img src="@/assets/logo.svg" />
        </v-avatar>
      </RouterLink>
      Komga
    </v-app-bar-title>
    <LocaleSelector />
    <ThemeSelector />
  </v-app-bar>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<script lang="ts"></script>

<style scoped></style>
