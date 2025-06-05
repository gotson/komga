<template>
  <v-btn
    :icon="themeIcon"
    @click="cycleTheme()"
  />
</template>

<script setup lang="ts">
import mdiWeatherSunny from '~icons/mdi/weather-sunny'
import mdiWeatherNight from '~icons/mdi/weather-night'
import mdiThemeLightDark from '~icons/mdi/theme-light-dark'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const themes = [
  {
    value: 'light',
    icon: mdiWeatherSunny,
  },
  {
    value: 'dark',
    icon: mdiWeatherNight,
  },
  {
    value: 'system',
    icon: mdiThemeLightDark,
  },
]

const themeIcon = computed(
  () => themes.find((x) => x.value === appStore.theme)?.icon || mdiThemeLightDark,
)

function cycleTheme() {
  const index = themes.findIndex((x) => x.value === appStore.theme)
  const newIndex = (index + 1) % themes.length
  appStore.theme = themes[newIndex]!.value
}
</script>

<script lang="ts"></script>

<style scoped></style>
