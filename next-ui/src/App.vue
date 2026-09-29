<template>
  <v-app>
    <RouterView />

    <SnackQueue />
    <DialogConfirmEditInstance />
    <DialogConfirmInstance />
    <DialogSimpleInstance />
  </v-app>
</template>

<script lang="ts" setup>
import { useThemeWatcher } from '@/composables/themeWatcher'
import { useSSE } from '@/composables/sse'
import { useAuthWatcher } from '@/composables/auth'

// global composables
useThemeWatcher()
useSSE()
useAuthWatcher()
</script>

<style>
@import 'styles/global.scss';
</style>
