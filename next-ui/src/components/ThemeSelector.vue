<template>
  <v-icon-btn
    v-ktooltip:bottom="
      $formatMessage({
        description: 'Theme selector button: tooltip',
        defaultMessage: 'Theme',
        id: 'e8DJzj',
      })
    "
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
import { themes } from '@/types/theme'

const appStore = useAppStore()

const themeIcon = computed(
  () => themes.find((x) => x.value === appStore.theme)?.icon || 'i-mdi:theme-light-dark',
)

function cycleTheme() {
  const index = themes.findIndex((x) => x.value === appStore.theme)
  const newIndex = (index + 1) % themes.length
  appStore.theme = themes[newIndex]!.value
}
</script>
