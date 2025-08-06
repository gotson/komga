<template>
  <v-app>
    <router-view />

    <FragmentSnackQueue />
    <FragmentDialogConfirmEdit />
    <FragmentDialogConfirm />
  </v-app>

  <PiniaColadaDevtools />
</template>

<script lang="ts" setup>
import { PiniaColadaDevtools } from '@pinia/colada-devtools'
import { useThemeWatcher } from '@/composables/themeWatcher'

useThemeWatcher()
</script>

<style>
@import 'styles/global.scss';
</style>
