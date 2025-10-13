<template>
  <v-app-bar elevation="2">
    <template #prepend>
      <v-app-bar-nav-icon @click="appStore.drawer = !appStore.drawer">
        <template #default>
          <v-badge
            :model-value="unreadCount > 0 && !appStore.drawer"
            dot
            floating
            color="info"
          >
            <v-icon icon="i-mdi:menu"></v-icon>
          </v-badge>
        </template>
      </v-app-bar-nav-icon>
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
import { useAnnouncements } from '@/colada/announcements'

const appStore = useAppStore()
const { unreadCount } = useAnnouncements()
</script>

<script lang="ts"></script>

<style scoped></style>
