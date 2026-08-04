<template>
  <v-app>
    <RouterView />

    <SnackQueue />
    <DialogConfirmEditInstance />
    <DialogConfirmInstance />
    <DialogSimpleInstance />
  </v-app>

  <PiniaColadaDevtools />
</template>

<script lang="ts" setup>
import { PiniaColadaDevtools } from '@pinia/colada-devtools'
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
