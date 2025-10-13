<template>
  <v-btn
    :icon="themeIcon"
    :aria-label="
      $formatMessage({
        description: 'Theme selector button: aria-label',
        defaultMessage: 'theme selector',
        id: '0XDhyR',
      })
    "
    @click="cycleTheme()"
  />
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const themes = [
  {
    value: 'light',
    icon: 'i-mdi:weather-sunny',
  },
  {
    value: 'dark',
    icon: 'i-mdi:weather-night',
  },
  {
    value: 'system',
    icon: 'i-mdi:theme-light-dark',
  },
]

const themeIcon = computed(
  () => themes.find((x) => x.value === appStore.theme)?.icon || 'i-mdi:theme-light-dark',
)

function cycleTheme() {
  const index = themes.findIndex((x) => x.value === appStore.theme)
  const newIndex = (index + 1) % themes.length
  appStore.theme = themes[newIndex]!.value
}
</script>

<script lang="ts"></script>

<style scoped></style>
