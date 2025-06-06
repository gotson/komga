<template>
  <v-app>
    <router-view />

    <SnackQueue />
    <DialogInstanceConfirmEdit />
    <DialogInstanceConfirm />
  </v-app>
</template>

<script lang="ts" setup>
import { useTheme } from 'vuetify'
import { useAppStore } from '@/stores/app'
import { usePreferredDark } from '@vueuse/core'

const appStore = useAppStore()
const theme = useTheme()
const prefersDark = usePreferredDark()

function updateTheme(selectedTheme: string, prefersDark: boolean) {
  if (selectedTheme === 'system') {
    theme.global.name.value = prefersDark ? 'dark' : 'light'
  } else {
    theme.global.name.value = selectedTheme
  }
}

watch([() => appStore.theme, prefersDark], ([selectedTheme, prefersDark]) =>
  updateTheme(selectedTheme, prefersDark),
)

// trigger an update on startup to get the proper theme loaded
updateTheme(appStore.theme, prefersDark.value)
</script>

<style>
@import 'styles/global.scss';
</style>
