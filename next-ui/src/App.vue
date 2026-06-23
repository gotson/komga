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
import { useAuthWatcher } from '@/composables/auth'

useThemeWatcher()
useAuthWatcher()
</script>

<style>
@import 'styles/global.scss';
</style>
