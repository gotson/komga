<template>
  <v-app>
    <router-view />

    <FragmentSnackQueue />
    <FragmentDialogConfirmEdit />
    <FragmentDialogConfirm />
  </v-app>
</template>

<script lang="ts" setup>
import { useThemeWatcher } from '@/composables/themeWatcher'

useThemeWatcher()
</script>

<style>
@import 'styles/global.scss';
</style>
